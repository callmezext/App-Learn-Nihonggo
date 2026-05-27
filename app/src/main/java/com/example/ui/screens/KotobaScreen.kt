package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Vocabulary
import com.example.ui.viewmodel.ActiveVocabQuiz
import com.example.ui.viewmodel.NihongoViewModel
import com.example.ui.viewmodel.VocabQuestion

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KotobaScreen(
    viewModel: NihongoViewModel,
    minnaVocab: List<Vocabulary>,
    habikiVocab: List<Vocabulary>,
    modifier: Modifier = Modifier
) {
    var selectedBook by remember { mutableStateOf("MINNA") } // "MINNA" or "HABIKI"
    var selectedLessonFilter by remember { mutableStateOf<Int?>(null) } // null means "All"
    var isFlashcardMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val currentVocabList = if (selectedBook == "MINNA") minnaVocab else habikiVocab

    // Get available lessons for the selected book
    val lessons = remember(currentVocabList) {
        currentVocabList.map { it.lesson }.distinct().sorted()
    }

    // Filter vocab list by lesson and search query with caching
    val filteredVocab = remember(currentVocabList, selectedLessonFilter, searchQuery) {
        currentVocabList.filter { vocab ->
            val matchesLesson = selectedLessonFilter == null || vocab.lesson == selectedLessonFilter
            val matchesSearch = searchQuery.isEmpty() || 
                    vocab.jpn.contains(searchQuery, ignoreCase = true) ||
                    vocab.kanji.contains(searchQuery, ignoreCase = true) ||
                    vocab.romaji.contains(searchQuery, ignoreCase = true) ||
                    vocab.meaning.contains(searchQuery, ignoreCase = true)
            matchesLesson && matchesSearch
        }
    }

    // Active Vocab Quiz
    val activeVocabQuiz by viewModel.activeVocabQuiz.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Book Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val books = listOf(
                "MINNA" to "Minna No Nihongo 1",
                "HABIKI" to "LPK Habiki (Hibiki)"
            )
            books.forEach { (bookKey, bookLabel) ->
                val isActive = selectedBook == bookKey
                Button(
                    onClick = {
                        selectedBook = bookKey
                        selectedLessonFilter = null // reset lesson filter
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .testTag("book_tab_$bookKey"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = bookLabel, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Filters: Lesson + Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lesson dropdown simulation or scrollable row
            Column(modifier = Modifier.weight(1.5f)) {
                var expandedLessonMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expandedLessonMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedLessonFilter == null) "Semua Bab" else "Bab $selectedLessonFilter",
                            maxLines = 1
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedLessonMenu,
                        onDismissRequest = { expandedLessonMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Bab") },
                            onClick = {
                                selectedLessonFilter = null
                                expandedLessonMenu = false
                            }
                        )
                        lessons.forEach { lesson ->
                            DropdownMenuItem(
                                text = { Text("Bab $lesson") },
                                onClick = {
                                    selectedLessonFilter = lesson
                                    expandedLessonMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Mode Selector: List vs Flashcard
            Button(
                onClick = { isFlashcardMode = !isFlashcardMode },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isFlashcardMode) Icons.Default.List else Icons.Default.Style,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFlashcardMode) "Daftar Kata" else "Flashcard", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Search Bar (if List Mode)
        if (!isFlashcardMode) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Kosakata...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Quick Kuis Trig Button
        Button(
            onClick = { viewModel.startVocabQuiz(selectedBook, selectedLessonFilter) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("start_vocab_quiz_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Uji Hafalan (Mulai Kuis Kata)", fontWeight = FontWeight.Bold)
        }

        // Main content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredVocab.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada kosakata yang cocok.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else if (isFlashcardMode) {
                // Interactive Flashcard deck container
                FlashcardDeck(
                    vocabList = filteredVocab,
                    onToggleMastered = { viewModel.toggleVocabularyMastered(it) }
                )
            } else {
                // Simple Vocab List View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredVocab, key = { it.id }) { vocab ->
                        VocabListItemRow(
                            vocab = vocab,
                            onToggleMastered = { viewModel.toggleVocabularyMastered(vocab) }
                        )
                    }
                }
            }
        }
    }

    // Active Vocab Quiz overlay dialog
    activeVocabQuiz?.let { vocabQuiz ->
        VocabQuizDialog(
            quizState = vocabQuiz,
            onAnswerSelected = { viewModel.answerVocabQuiz(it) },
            onNext = { viewModel.nextVocabQuestion() },
            onClose = { viewModel.exitVocabQuiz() }
        )
    }
}

@Composable
fun VocabListItemRow(
    vocab: Vocabulary,
    onToggleMastered: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vocab_item_${vocab.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vocab.isMastered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vocab.jpn,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (vocab.kanji.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${vocab.kanji}]",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vocab.romaji,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = vocab.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Mastery checkbox
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onToggleMastered) {
                    Icon(
                        imageVector = if (vocab.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Tandai Sudah Hafal",
                        tint = if (vocab.isMastered) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = if (vocab.isMastered) "Hafal!" else "Pelajari",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (vocab.isMastered) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun FlashcardDeck(
    vocabList: List<Vocabulary>,
    onToggleMastered: (Vocabulary) -> Unit
) {
    if (vocabList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada kosakata")
        }
        return
    }
    var currentIndex by remember { mutableStateOf(0) }
    // Safe index bounds
    val safeIndex = currentIndex.coerceIn(0, (vocabList.size - 1).coerceAtLeast(0))
    val vocab = vocabList.getOrNull(safeIndex) ?: return

    var isFlipped by remember { mutableStateOf(false) }

    // Flip transition animation parameters
    val rotationAnimated by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    // Reset flips when changing cards
    LaunchedEffect(safeIndex) {
        isFlipped = false
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Counter
        Text(
            text = "Flashcard ${safeIndex + 1} dari ${vocabList.size}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3D Visual Flippable Card container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .graphicsLayer {
                    rotationY = rotationAnimated
                    cameraDistance = 12 * density
                }
                .clickable { isFlipped = !isFlipped }
                .testTag("flashcard_element"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Front / Back text content based on card rotation stage
                if (rotationAnimated <= 90f) {
                    // FRONT: JPN / Kanji
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = vocab.jpn,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        if (vocab.kanji.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Kanji: ${vocab.kanji}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Ketuk untuk Membalik 🔄",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // BACK (Mirrored so it looks horizontal on flip)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = vocab.meaning,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\" ${vocab.romaji} \"",
                            style = MaterialTheme.typography.titleLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Bab ${vocab.lesson}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle mastery from card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onToggleMastered(vocab) }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (vocab.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (vocab.isMastered) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (vocab.isMastered) "Sudah Hafal!" else "Tandai Sudah Hafal",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (currentIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Sebelumnya",
                    tint = if (currentIndex > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = "${currentIndex + 1} / ${vocabList.size}",
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = { if (currentIndex < vocabList.size - 1) currentIndex++ },
                enabled = currentIndex < vocabList.size - 1,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (currentIndex < vocabList.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Selanjutnya",
                    tint = if (currentIndex < vocabList.size - 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun VocabQuizDialog(
    quizState: ActiveVocabQuiz,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (quizState.isFinished) {
                    // Result Section
                    Text(
                        text = "Kuis Selesai! 🎉",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Skor Anda:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${quizState.correctAnswers} / ${quizState.questions.size} Benar",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali & Lanjutkan Belajar")
                    }
                } else {
                    val q = quizState.questions[quizState.currentIndex]
                    val total = quizState.questions.size

                    // Header progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pertanyaan ${quizState.currentIndex + 1} / $total",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        IconButton(onClick = onClose) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Terjemah dari:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (q.questionType == 0) q.vocabulary.jpn else q.vocabulary.meaning,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                            if (q.questionType == 0 && q.vocabulary.kanji.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "[ ${q.vocabulary.kanji} ]",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Choice options
                    val correctAnswer = if (q.questionType == 0) q.vocabulary.meaning else q.vocabulary.jpn
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        q.options.forEach { option ->
                            val isSelected = option == quizState.selectedAnswer
                            val isCorrectAnswer = option == correctAnswer

                            val btnColor = when {
                                !quizState.isAnswered -> MaterialTheme.colorScheme.surface
                                isCorrectAnswer -> Color(0xFFE8F5E9)
                                isSelected -> Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            val borderCol = when {
                                !quizState.isAnswered -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                isCorrectAnswer -> Color(0xFF4CAF50)
                                isSelected -> Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            val txtColor = when {
                                !quizState.isAnswered -> MaterialTheme.colorScheme.onSurface
                                isCorrectAnswer -> Color(0xFF2E7D32)
                                isSelected -> Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !quizState.isAnswered) { onAnswerSelected(option) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = btnColor),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderCol))
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = txtColor,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Next button
                    AnimatedVisibility(visible = quizState.isAnswered) {
                        Button(
                            onClick = onNext,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Berikutnya")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
