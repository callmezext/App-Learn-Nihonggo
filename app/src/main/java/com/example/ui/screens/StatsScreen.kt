package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import com.example.data.model.KanaMastery
import com.example.data.model.Vocabulary
import com.example.ui.viewmodel.AppTheme
import com.example.ui.viewmodel.NihongoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    viewModel: NihongoViewModel,
    allKana: List<KanaMastery>,
    minnaVocab: List<Vocabulary>,
    habikiVocab: List<Vocabulary>,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    var selectedStatsTab by remember { mutableStateOf("KANA") } // "KANA" or "VOCAB"

    // Kana processing with caching
    val totalKana = remember(allKana) { allKana.size }
    val masteredKana = remember(allKana) { allKana.filter { it.correctCount > 0 && it.correctCount >= it.wrongCount } }
    val needsPracticeKana = remember(allKana) { allKana.filter { it.wrongCount > 0 && it.wrongCount > it.correctCount } }
    val untestedKana = remember(allKana) { allKana.filter { (it.correctCount + it.wrongCount) == 0 } }

    // Vocab processing with caching
    val allVocab = remember(minnaVocab, habikiVocab) { minnaVocab + habikiVocab }
    val totalVocab = remember(allVocab) { allVocab.size }
    val masteredVocab = remember(allVocab) { allVocab.filter { it.isMastered } }
    val learningVocab = remember(allVocab) { allVocab.filter { !it.isMastered } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Statistik & Tema",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Section 1: Dynamic Theme Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Tema",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih Tema Belajar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Grid of Themes
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val themeList = AppTheme.values()
                    // Display themes in 2x2 rows
                    for (i in themeList.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (j in i..i + 1) {
                                if (j < themeList.size) {
                                    val theme = themeList[j]
                                    val isSelected = currentTheme == theme
                                    ThemeItemCard(
                                        theme = theme,
                                        isSelected = isSelected,
                                        onClick = { viewModel.setTheme(theme) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Progress Summaries
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Kemajuan Belajar Anda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Progress Bar: Kana Mastered
                ProgressMetricItem(
                    label = "Hafalan Kana",
                    value = if (totalKana > 0) masteredKana.size.toFloat() / totalKana.toFloat() else 0f,
                    scoreText = "${masteredKana.size} / $totalKana Huruf",
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar: Vocabulary Mastered
                ProgressMetricItem(
                    label = "Hafalan Kata (Kotoba)",
                    value = if (totalVocab > 0) masteredVocab.size.toFloat() / totalVocab.toFloat() else 0f,
                    scoreText = "${masteredVocab.size} / $totalVocab Kata",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Section 3: Detailed Breakdown Tabs Selection Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf("KANA" to "Analisis Kana", "VOCAB" to "Hafalan Kata")
            tabs.forEach { (tabKey, tabLabel) ->
                val isActive = selectedStatsTab == tabKey
                Button(
                    onClick = { selectedStatsTab = tabKey },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .testTag("stats_tab_$tabKey"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = tabLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Details content matching selected detail tab
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedStatsTab == "KANA") {
                // KANA STATS BREAKDOWN
                Text(
                    text = "Karakter Perlu Latihan (${needsPracticeKana.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                if (needsPracticeKana.isEmpty()) {
                    Text(
                        text = "Luar biasa! Tidak ada huruf yang sering salah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        needsPracticeKana.forEach { kana ->
                            KanaStatBadge(kana = kana, isMistake = true)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Karakter Sudah Hafal (${masteredKana.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                if (masteredKana.isEmpty()) {
                    Text(
                        text = "Ayo ikuti kuis untuk menandai keahlian huruf Anda!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        masteredKana.forEach { kana ->
                            KanaStatBadge(kana = kana, isMistake = false)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Karakter Belum Diuji (${untestedKana.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    untestedKana.take(30).forEach { kana ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${kana.char} (${kana.romaji})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (untestedKana.size > 30) {
                        Text(
                            text = "...dan ${untestedKana.size - 30} lagi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

            } else {
                // VOCAB STATS BREAKDOWN
                var showMasteredVocab by remember { mutableStateOf(false) }
                val activeVocabSegment = if (showMasteredVocab) masteredVocab else learningVocab

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showMasteredVocab) "Daftar Sudah Hafal (${masteredVocab.size})" else "Sedang Dipelajari / Belum Hafal (${learningVocab.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showMasteredVocab = !showMasteredVocab }) {
                        Text(if (showMasteredVocab) "Lihat Belum Hafal" else "Lihat Sudah Hafal")
                    }
                }

                if (activeVocabSegment.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showMasteredVocab) "Belum ada kosakata yang ditandai hafal." else "Hore! Semua kata sudah dihafal!",
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Render a list of up to 15 vocab for review
                            val displayVocab = activeVocabSegment.take(15)
                            displayVocab.forEach { vocab ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${vocab.jpn} " + if (vocab.kanji.isNotEmpty()) "[${vocab.kanji}]" else "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = vocab.meaning,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Buku: ${vocab.book} - Bab ${vocab.lesson}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleVocabularyMastered(vocab) }
                                    ) {
                                        Icon(
                                            imageVector = if (vocab.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Tandai Sudah Hafal",
                                            tint = if (vocab.isMastered) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            if (activeVocabSegment.size > 15) {
                                Text(
                                    text = "Menampilkan 15 dari ${activeVocabSegment.size} kata.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeItemCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerCol = when (theme) {
        AppTheme.CLASSIC_INDIGO -> Color(0xFFEFF6FF)
        AppTheme.SAKURA_PINK -> Color(0xFFFFF1F2)
        AppTheme.ZEN_MATCHA -> Color(0xFFF1F8E9)
        AppTheme.SAMURAI_DARK -> Color(0xFF1E1E1E)
    }

    val contentCol = when (theme) {
        AppTheme.SAMURAI_DARK -> Color(0xFFECEFF1)
        else -> Color(0xFF0F172A)
    }

    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
            .testTag("theme_card_${theme.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerCol),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentCol,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressMetricItem(
    label: String,
    value: Float,
    scoreText: String,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = scoreText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun KanaStatBadge(
    kana: KanaMastery,
    isMistake: Boolean
) {
    val borderCol = if (isMistake) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else Color(0xFF4CAF50).copy(alpha = 0.6f)
    val backgroundCol = if (isMistake) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else Color(0xFFE8F5E9).copy(alpha = 0.3f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundCol),
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = kana.char,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMistake) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )
            Text(
                text = kana.romaji,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "B:${kana.correctCount} S:${kana.wrongCount}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
