package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KanaMastery
import com.example.data.model.LevelStatus
import com.example.data.model.Vocabulary
import com.example.ui.viewmodel.NihongoViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun DashboardScreen(
    viewModel: NihongoViewModel,
    levels: List<LevelStatus>,
    allKana: List<KanaMastery>,
    minnaVocab: List<Vocabulary>,
    habikiVocab: List<Vocabulary>,
    modifier: Modifier = Modifier
) {
    // Calculations for overall progress with caching
    val totalKana = remember(allKana) { allKana.size.coerceAtLeast(1) }
    val masteredKana = remember(allKana) { allKana.count { it.correctCount > 0 && it.correctCount >= it.wrongCount } }
    val kanaProgress = remember(masteredKana, totalKana) { masteredKana.toFloat() / totalKana.toFloat() }

    val totalVocab = remember(minnaVocab, habikiVocab) { (minnaVocab.size + habikiVocab.size).coerceAtLeast(1) }
    val masteredVocab = remember(minnaVocab, habikiVocab) { minnaVocab.count { it.isMastered } + habikiVocab.count { it.isMastered } }
    val vocabProgress = remember(masteredVocab, totalVocab) { masteredVocab.toFloat() / totalVocab.toFloat() }

    val completedLevels = remember(levels) { levels.count { it.highScore >= 60 } }
    val totalLevels = remember(levels) { levels.size.coerceAtLeast(1) }
    val levelProgress = remember(completedLevels, totalLevels) { completedLevels.toFloat() / totalLevels.toFloat() }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Welcome and Header banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "こんにちは！ 👋",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ayo Kuasai Bahasa Jepang!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pelajari Hiragana, Katakana, dan Kosakata (Kotoba) praktis harian dengan kuis interaktif.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        // Quick Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Kemajuan Kana",
                progressValue = kanaProgress,
                progressText = "$masteredKana/$totalKana",
                icon = Icons.Default.MenuBook,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Hafalan Kotoba",
                progressValue = vocabProgress,
                progressText = "$masteredVocab/$totalVocab",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Level Selesai",
                progressValue = levelProgress,
                progressText = "$completedLevels/$totalLevels",
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Title Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Daftar Level Belajar",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Kuis KKM >= 60%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Levels list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(levels, key = { it.levelIndex }) { level ->
                LevelItemRow(
                    level = level,
                    onClick = {
                        if (level.isUnlocked) {
                            viewModel.startKanaQuiz(level.levelIndex)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    progressValue: Float,
    progressText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val progressAnimated by animateFloatAsState(targetValue = progressValue)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LevelItemRow(
    level: LevelStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGolden = level.highScore >= 80
    val cardColor = when {
        !level.isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        isGolden -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        level.highScore >= 60 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_card_${level.levelIndex}")
            .clickable(enabled = level.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(if (level.isUnlocked) 2.dp else 0.dp),
        border = if (level.isUnlocked) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (level.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!level.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${level.levelIndex}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (level.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (level.isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Badge / Locking indicator
            if (level.isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (level.highScore > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isGolden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "${level.highScore}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = if (level.highScore >= 60) "Selesai" else "Belum Mulai",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (level.highScore >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Mulai kuis",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
