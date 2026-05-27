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
    val kanaType: String
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
    val isFinished: Boolean = false
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
            AppTheme.valueOf(prefs.getString("app_theme", AppTheme.CLASSIC_INDIGO.name) ?: AppTheme.CLASSIC_INDIGO.name)
        } catch (e: Exception) {
            AppTheme.CLASSIC_INDIGO
        }
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    // Current navigating screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
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
            // If level 15 (Grand exam), take from all levels
            val levelKana = if (levelIndex == 15) {
                allKanaList.shuffled().take(15)
            } else {
                allKanaList.filter { it.level == levelIndex }
            }

            if (levelKana.isEmpty()) return@launch

            // Generate questions
            // Let's create 10 questions (or amount of available kana if < 10)
            val questionPool = mutableListOf<QuizQuestion>()
            val maxQuestions = if (levelIndex == 15) 15 else 10
            
            for (i in 0 until maxQuestions) {
                val baseKana = levelKana[i % levelKana.size]
                // 30% chance for keyboard type-in romaji test, 70% multiple choice
                val isTypeIn = i % 3 == 0 && levelIndex != 15 // Avoid typing in combined finals unless desired, making it friendly but challenging

                // Options with distractors
                val correctRomaji = baseKana.romaji
                val distractors = allKanaList
                    .filter { it.romaji != correctRomaji }
                    .map { it.romaji }
                    .distinct()
                    .shuffled()
                    .take(3)

                val options = (distractors + correctRomaji).shuffled()

                questionPool.add(
                    QuizQuestion(
                        character = baseKana.char,
                        correctRomaji = correctRomaji,
                        options = options,
                        isTypeInQuiz = isTypeIn,
                        kanaType = baseKana.kanaType
                    )
                )
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
            // Save correct/wrong to DB to update individual character masteries
            repository.recordTestResult(currentQuestion.character, isCorrect)
        }

        val updatedCorrect = if (isCorrect) quiz.correctAnswers + 1 else quiz.correctAnswers

        _activeQuiz.value = quiz.copy(
            selectedAnswer = selected,
            isAnswered = true,
            correctAnswers = updatedCorrect
        )
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

        _activeVocabQuiz.value = quiz.copy(
            selectedAnswer = selected,
            isAnswered = true,
            correctAnswers = updatedCorrect
        )
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
