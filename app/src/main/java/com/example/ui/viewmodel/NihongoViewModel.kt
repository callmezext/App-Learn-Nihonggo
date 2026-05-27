package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DatabaseProvider
import com.example.data.model.KanaMastery
import com.example.data.model.LevelStatus
import com.example.data.model.Vocabulary
import com.example.data.repository.InitialData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Cover : Screen()
    object Dashboard : Screen()
    object KanaChart : Screen()
    data class Quiz(val levelIndex: Int) : Screen()
    object Kotoba : Screen()
    object Stats : Screen()
}

data class QuizQuestion(
    val character: String,
    val correctRomaji: String,
    val options: List<String>,
    val isTypeInQuiz: Boolean = false,
    val kanaType: String,
    val isRomajiToKana: Boolean = false,
    val displayPrompt: String = "",
    val originChar: String = "",
    val originRomaji: String = ""
)

data class ActiveQuiz(
    val levelIndex: Int,
    val levelTitle: String,
    val questions: List<QuizQuestion>,
    val currentIndex: Int = 0,
    val correctAnswers: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false,
    val userInput: String = "",
    val isFinished: Boolean = false
)

data class VocabQuestion(
    val vocabulary: Vocabulary,
    val questionType: Int, // 0 = Jpn -> Meaning, 1 = Meaning -> Jpn
    val options: List<String>
)

data class ActiveVocabQuiz(
    val book: String,
    val questions: List<VocabQuestion>,
    val currentIndex: Int = 0,
    val correctAnswers: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false,
    val isFinished: Boolean = false,
    val quizTier: Int? = null
)

enum class AppTheme(val displayName: String) {
    CLASSIC_INDIGO("Classic Indigo 🔵"),
    SAKURA_PINK("Sakura Pink 🌸"),
    ZEN_MATCHA("Zen Matcha 🍵"),
    SAMURAI_DARK("Samurai Dark 🗡️")
}

class NihongoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseProvider.getRepository(application)
    private val prefs = application.getSharedPreferences("nihongo_prefs", android.content.Context.MODE_PRIVATE)

    // Persistent Theme selection
    private val _currentTheme = MutableStateFlow(
        try {
            AppTheme.valueOf(prefs.getString("app_theme", AppTheme.SAMURAI_DARK.name) ?: AppTheme.SAMURAI_DARK.name)
        } catch (e: Exception) {
            AppTheme.SAMURAI_DARK
        }
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    // Current navigating screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Cover)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Database Flows
    val levels: StateFlow<List<LevelStatus>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKana: StateFlow<List<KanaMastery>> = repository.allKana
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vocab flows
    val minnaVocab: StateFlow<List<Vocabulary>> = repository.getVocabByBook("MINNA")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habikiVocab: StateFlow<List<Vocabulary>> = repository.getVocabByBook("HABIKI")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Quiz Management
    private val _activeQuiz = MutableStateFlow<ActiveQuiz?>(null)
    val activeQuiz: StateFlow<ActiveQuiz?> = _activeQuiz.asStateFlow()

    // Active Vocab Quiz Management
    private val _activeVocabQuiz = MutableStateFlow<ActiveVocabQuiz?>(null)
    val activeVocabQuiz: StateFlow<ActiveVocabQuiz?> = _activeVocabQuiz.asStateFlow()

    // Selected Hiragana/Katakana filter
    private val _selectedTab = MutableStateFlow("HIRAGANA") // "HIRAGANA" or "KATAKANA"
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Gojuon") // "Gojuon", "Dakuten", "Youon"
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch {
            // Guarantee local data pre-populations are safely initialized
            repository.ensureDataPopulated()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Toggle mastery of vocabulary
    fun toggleVocabularyMastered(vocab: Vocabulary) {
        viewModelScope.launch {
            repository.updateVocabMastery(vocab.id, !vocab.isMastered)
        }
    }

    // === KANA QUIZ LOGIC ===
    fun startKanaQuiz(levelIndex: Int) {
        viewModelScope.launch {
            val allKanaList = allKana.value
            val levelItem = levels.value.find { it.levelIndex == levelIndex }
            val levelTitle = levelItem?.title ?: "Kuis Level $levelIndex"

            // Filter kana for this level
            // levels 3, 5, 7, 9 are cumulative review levels; level 19 is the ultimate exam
            val levelKana = when (levelIndex) {
                19 -> allKanaList.shuffled().take(15)
                3 -> allKanaList.filter { it.level == 1 || it.level == 2 }.shuffled().take(10)
                5 -> allKanaList.filter { it.level == 1 || it.level == 2 || it.level == 4 }.shuffled().take(10)
                7 -> allKanaList.filter { it.level == 1 || it.level == 2 || it.level == 4 || it.level == 6 }.shuffled().take(10)
                9 -> allKanaList.filter { it.level == 1 || it.level == 2 || it.level == 4 || it.level == 6 || it.level == 8 }.shuffled().take(10)
                else -> allKanaList.filter { it.level == levelIndex }
            }

            if (levelKana.isEmpty()) return@launch

            // Generate questions
            val questionPool = mutableListOf<QuizQuestion>()
            val maxQuestions = if (levelIndex == 19) 15 else 10
            
            for (i in 0 until maxQuestions) {
                val baseKana = levelKana[i % levelKana.size]
                // Always multiple choice, no keyboard type-in romaji test
                val isTypeIn = false
                val isRomajiToKana = (i % 2 == 1)

                if (!isRomajiToKana) {
                    // 1. KANA -> ROMAJI (tanya romaji dari huruf jepang)
                    val correctRomaji = baseKana.romaji
                    
                    // Prioritize other characters from the exact same level being tested
                    val localDistractors = levelKana
                        .filter { it.romaji != correctRomaji }
                        .map { it.romaji }
                        .distinct()
                        .shuffled()

                    val distractors = if (localDistractors.size >= 3) {
                        localDistractors.take(3)
                    } else {
                        // Filter candidates: same hiragana/katakana type and level <= current index to keep choices level-appropriate!
                        val allowedDistractorKana = allKanaList.filter { 
                            it.kanaType == baseKana.kanaType && 
                            it.romaji != correctRomaji && 
                            it.level <= levelIndex 
                        }
                        val genericDistractors = allowedDistractorKana
                            .map { it.romaji }
                            .distinct()
                            .shuffled()
                            
                        (localDistractors + genericDistractors).distinct().take(3)
                    }

                    val options = (distractors + correctRomaji).distinct().shuffled()

                    questionPool.add(
                        QuizQuestion(
                            character = baseKana.char,
                            correctRomaji = correctRomaji,
                            options = options,
                            isTypeInQuiz = isTypeIn,
                            kanaType = baseKana.kanaType,
                            isRomajiToKana = false,
                            displayPrompt = "Apa Romaji dari huruf ${baseKana.kanaType} di atas?",
                            originChar = baseKana.char,
                            originRomaji = baseKana.romaji
                        )
                    )
                } else {
                    // 2. ROMAJI -> KANA (tanya huruf jepang untuk romaji tersebut)
                    val correctChar = baseKana.char
                    val correctRomaji = baseKana.romaji

                    val localDistractors = levelKana
                        .filter { it.char != correctChar }
                        .map { it.char }
                        .distinct()
                        .shuffled()

                    val distractors = if (localDistractors.size >= 3) {
                        localDistractors.take(3)
                    } else {
                        // Filter candidates: same hiragana/katakana type and level <= current index
                        val allowedDistractorKana = allKanaList.filter { 
                            it.kanaType == baseKana.kanaType && 
                            it.char != correctChar && 
                            it.level <= levelIndex 
                        }
                        val genericDistractors = allowedDistractorKana
                            .map { it.char }
                            .distinct()
                            .shuffled()
                            
                        (localDistractors + genericDistractors).distinct().take(3)
                    }

                    val options = (distractors + correctChar).distinct().shuffled()

                    questionPool.add(
                        QuizQuestion(
                            character = correctRomaji, // Tunjukkan Romaji sebagai soal utama (e.g. "ka")
                            correctRomaji = correctChar, // User harus klik tombol dengan tulisan Hiragana/Katakana yang tepat
                            options = options,
                            isTypeInQuiz = isTypeIn,
                            kanaType = baseKana.kanaType,
                            isRomajiToKana = true,
                            displayPrompt = "Mana huruf ${baseKana.kanaType} untuk Romaji di atas?",
                            originChar = baseKana.char,
                            originRomaji = baseKana.romaji
                        )
                    )
                }
            }

            _activeQuiz.value = ActiveQuiz(
                levelIndex = levelIndex,
                levelTitle = levelTitle,
                questions = questionPool.shuffled()
            )
            navigateTo(Screen.Quiz(levelIndex))
        }
    }

    fun answerKanaQuiz(selected: String) {
        val quiz = _activeQuiz.value ?: return
        if (quiz.isAnswered) return

        val currentQuestion = quiz.questions[quiz.currentIndex]
        val isCorrect = selected.trim().lowercase() == currentQuestion.correctRomaji.lowercase()

        viewModelScope.launch {
            // Save correct/wrong to DB to update individual character masteries using originChar
            repository.recordTestResult(currentQuestion.originChar, isCorrect)
        }

        val updatedCorrect = if (isCorrect) quiz.correctAnswers + 1 else quiz.correctAnswers

        if (isCorrect) {
            _activeQuiz.value = quiz.copy(
                selectedAnswer = selected,
                isAnswered = true,
                correctAnswers = updatedCorrect
            )
            nextKanaQuestion()
        } else {
            _activeQuiz.value = quiz.copy(
                selectedAnswer = selected,
                isAnswered = true,
                correctAnswers = updatedCorrect
            )
        }
    }

    fun updateTypedInput(input: String) {
        val quiz = _activeQuiz.value ?: return
        _activeQuiz.value = quiz.copy(userInput = input)
    }

    fun submitTypedAnswer() {
        val quiz = _activeQuiz.value ?: return
        if (quiz.isAnswered) return
        answerKanaQuiz(quiz.userInput)
    }

    fun nextKanaQuestion() {
        val quiz = _activeQuiz.value ?: return
        val nextIndex = quiz.currentIndex + 1

        if (nextIndex >= quiz.questions.size) {
            // End of Quiz! Calculate the final score
            val percentage = ((quiz.correctAnswers.toFloat() / quiz.questions.size.toFloat()) * 100f).toInt()
            
            viewModelScope.launch {
                repository.recordLevelCompleted(quiz.levelIndex, percentage)
            }

            _activeQuiz.value = quiz.copy(isFinished = true)
        } else {
            _activeQuiz.value = quiz.copy(
                currentIndex = nextIndex,
                isAnswered = false,
                selectedAnswer = null,
                userInput = ""
            )
        }
    }

    fun exitKanaQuiz() {
        _activeQuiz.value = null
        navigateTo(Screen.Dashboard)
    }

    // === VOCABULARY QUIZ LOGIC ===
    fun startVocabQuiz(book: String, selectedLesson: Int?) {
        val fullList = if (book == "MINNA") minnaVocab.value else habikiVocab.value
        val list = if (selectedLesson != null) {
            fullList.filter { it.lesson == selectedLesson }
        } else {
            fullList
        }

        if (list.size < 4) return // Needs enough words to make choices

        val questions = list.shuffled().take(10).mapIndexed { index, vocab ->
            // Mix: Odd matches Japanese -> Meaning, Even matches Meaning -> Japanese
            val qType = index % 2
            
            val distractors = if (qType == 0) {
                // Meaning options: find 3 meaning distractors
                fullList.filter { it.meaning != vocab.meaning }
                    .map { it.meaning }
                    .distinct()
                    .shuffled()
                    .take(3)
            } else {
                // Japanese options: find 3 japanese distractors
                fullList.filter { it.jpn != vocab.jpn }
                    .map { it.jpn }
                    .distinct()
                    .shuffled()
                    .take(3)
            }

            val correct = if (qType == 0) vocab.meaning else vocab.jpn
            val options = (distractors + correct).shuffled()

            VocabQuestion(
                vocabulary = vocab,
                questionType = qType,
                options = options
            )
        }

        _activeVocabQuiz.value = ActiveVocabQuiz(
            book = book,
            questions = questions
        )
    }

    fun startGradedVocabQuiz(book: String, quizTier: Int) {
        val fullList = if (book == "MINNA") minnaVocab.value else habikiVocab.value
        
        // Match specific sections/lessons for each Tier (1 = easiest, 10 = hardest)
        val targetLessons = when (quizTier) {
            1 -> listOf(1)
            2 -> listOf(1, 2)
            3 -> listOf(1, 2, 3)
            4 -> listOf(4)
            5 -> listOf(1, 2, 3, 4)
            6 -> listOf(5)
            7 -> listOf(6)
            8 -> listOf(7)
            9 -> listOf(8)
            10 -> listOf(9, 10)
            else -> listOf(1)
        }

        val list = fullList.filter { it.lesson in targetLessons }
        val finalSelection = if (list.size < 4) fullList else list

        val questions = finalSelection.shuffled().take(10).mapIndexed { index, vocab ->
            val qType = index % 2
            val distractors = if (qType == 0) {
                fullList.filter { it.meaning != vocab.meaning }
                    .map { it.meaning }
                    .distinct()
                    .shuffled()
                    .take(3)
            } else {
                fullList.filter { it.jpn != vocab.jpn }
                    .map { it.jpn }
                    .distinct()
                    .shuffled()
                    .take(3)
            }

            val correct = if (qType == 0) vocab.meaning else vocab.jpn
            val options = (distractors + correct).shuffled()

            VocabQuestion(
                vocabulary = vocab,
                questionType = qType,
                options = options
            )
        }

        _activeVocabQuiz.value = ActiveVocabQuiz(
            book = book,
            questions = questions,
            quizTier = quizTier
        )
    }

    fun answerVocabQuiz(selected: String) {
        val quiz = _activeVocabQuiz.value ?: return
        if (quiz.isAnswered) return

        val q = quiz.questions[quiz.currentIndex]
        val correctAnswer = if (q.questionType == 0) q.vocabulary.meaning else q.vocabulary.jpn
        val isCorrect = selected == correctAnswer

        // Auto mark as mastered in background if user answers vocabulary quiz correctly!
        if (isCorrect) {
            viewModelScope.launch {
                repository.updateVocabMastery(q.vocabulary.id, true)
            }
        }

        val updatedCorrect = if (isCorrect) quiz.correctAnswers + 1 else quiz.correctAnswers

        if (isCorrect) {
            _activeVocabQuiz.value = quiz.copy(
                selectedAnswer = selected,
                isAnswered = true,
                correctAnswers = updatedCorrect
            )
            nextVocabQuestion()
        } else {
            _activeVocabQuiz.value = quiz.copy(
                selectedAnswer = selected,
                isAnswered = true,
                correctAnswers = updatedCorrect
            )
        }
    }

    fun nextVocabQuestion() {
        val quiz = _activeVocabQuiz.value ?: return
        val nextIndex = quiz.currentIndex + 1

        if (nextIndex >= quiz.questions.size) {
            _activeVocabQuiz.value = quiz.copy(isFinished = true)
        } else {
            _activeVocabQuiz.value = quiz.copy(
                currentIndex = nextIndex,
                isAnswered = false,
                selectedAnswer = null
            )
        }
    }

    fun exitVocabQuiz() {
        _activeVocabQuiz.value = null
    }
}
