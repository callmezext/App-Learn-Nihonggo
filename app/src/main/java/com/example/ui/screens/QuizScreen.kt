package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ActiveQuiz
import com.example.ui.viewmodel.NihongoViewModel
import com.example.ui.viewmodel.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: NihongoViewModel,
    quizState: ActiveQuiz,
    modifier: Modifier = Modifier
) {
    if (quizState.isFinished) {
        QuizResultView(
            quizState = quizState,
            onClose = { viewModel.exitKanaQuiz() }
        )
    } else {
        val currentQuestion = quizState.questions[quizState.currentIndex]
        val progress = (quizState.currentIndex).toFloat() / quizState.questions.size.toFloat()
        val progressAnimated by animateFloatAsState(targetValue = progress)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(quizState.levelTitle, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitKanaQuiz() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Batal Kuis")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${quizState.currentIndex + 1}/${quizState.questions.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question Character Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentQuestion.character,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQuestion.displayPrompt.ifEmpty { "Apa Romaji dari huruf ${currentQuestion.kanaType} di atas?" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Answers area based on question type
                if (currentQuestion.isTypeInQuiz) {
                    TypeInAnswerBlock(
                        quizState = quizState,
                        currentQuestion = currentQuestion,
                        onWordChange = { viewModel.updateTypedInput(it) },
                        onSubmit = { viewModel.submitTypedAnswer() }
                    )
                } else {
                    MultipleChoiceBlock(
                        options = currentQuestion.options,
                        selectedOption = quizState.selectedAnswer,
                        isAnswered = quizState.isAnswered,
                        correctAnswer = currentQuestion.correctRomaji,
                        onOptionSelected = { viewModel.answerKanaQuiz(it) }
                    )
                }

                // Beautiful alert notification feedback if answer is wrong
                val isWrongAnswer = quizState.isAnswered && quizState.selectedAnswer?.trim()?.lowercase() != currentQuestion.correctRomaji.lowercase()
                AnimatedVisibility(visible = isWrongAnswer) {
                    val isDark = MaterialTheme.colorScheme.background.red < 0.25f
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFFEF4444) else Color(0xFFF44336))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Salah",
                                tint = if (isDark) Color(0xFFF87171) else Color(0xFFF44336),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Jawaban Kurang Tepat! ❌",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Kamu memilih: '${quizState.selectedAnswer ?: ""}'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                )
                                Text(
                                    text = "Jawaban yang benar: '${currentQuestion.correctRomaji}'",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color(0xFF86EFAC) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next Button
                AnimatedVisibility(visible = quizState.isAnswered) {
                    Button(
                        onClick = { viewModel.nextKanaQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (quizState.currentIndex == quizState.questions.size - 1) "Selesai" else "Pertanyaan Berikutnya",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceBlock(
    options: List<String>,
    selectedOption: String?,
    isAnswered: Boolean,
    correctAnswer: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val isCorrect = option == correctAnswer
            val isDark = MaterialTheme.colorScheme.background.red < 0.25f

            val cardColor = when {
                !isAnswered -> MaterialTheme.colorScheme.surface
                isCorrect -> if (isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
                isSelected -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }

            val borderColor = when {
                !isAnswered -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                isCorrect -> if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50)
                isSelected -> if (isDark) Color(0xFFE57373) else Color(0xFFF44336)
                else -> Color.Transparent
            }

            val textColor = when {
                !isAnswered -> MaterialTheme.colorScheme.onSurface
                isCorrect -> if (isDark) Color.White else Color(0xFF2E7D32)
                isSelected -> if (isDark) Color.White else Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_option_$option")
                    .clickable(enabled = !isAnswered) { onOptionSelected(option) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    if (isAnswered) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Benar",
                                tint = if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Salah",
                                tint = if (isDark) Color(0xFFE57373) else Color(0xFFF44336),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeInAnswerBlock(
    quizState: ActiveQuiz,
    currentQuestion: QuizQuestion,
    onWordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = quizState.userInput,
            onValueChange = { if (!quizState.isAnswered) onWordChange(it) },
            label = { Text("Tulis Romaji-nya") },
            placeholder = { Text("Contoh: sa") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("type_in_field"),
            singleLine = true,
            enabled = !quizState.isAnswered,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() })
        )

        if (!quizState.isAnswered) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kirim Jawaban")
            }
        } else {
            // Evaluated feedback
            val isCorrect = quizState.userInput.trim().lowercase() == currentQuestion.correctRomaji.lowercase()
            val boxColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val textColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
            val icon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = boxColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCorrect) "Luar biasa! Benar." else "Kurang tepat. Jawaban yang benar adalah '${currentQuestion.correctRomaji}'",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun QuizResultView(
    quizState: ActiveQuiz,
    onClose: () -> Unit
) {
    val total = quizState.questions.size
    val scorePercentage = ((quizState.correctAnswers.toFloat() / total.toFloat()) * 100f).toInt()
    val isPassed = scorePercentage >= 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Celebration/Motivational Illustration
            Text(
                text = if (isPassed) "🎉 Selamat!" else "💪 Coba Lagi",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text info
            Text(
                text = "Kuis ${quizState.levelTitle} Selesai",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Score Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPassed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$scorePercentage%",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold),
                        color = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "${quizState.correctAnswers} / $total Benar",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isPassed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Level unlocking feedback statement
            Text(
                text = if (isPassed) {
                    "Hebat! Anda berhasil melulusi kuis ini dengan nilai di atas KKM (>= 60%). Level berikutnya berhasil terbuka!"
                } else {
                    "Meskipun belum lulus, Anda dapat belajar di Kana Chart dan mencoba memutar kembali kuis ini kapan saja!"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_finish_back_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Kembali ke Dashboard",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
