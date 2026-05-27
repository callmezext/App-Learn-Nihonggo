package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KanaMastery
import com.example.ui.viewmodel.NihongoViewModel
import java.util.Locale

@Composable
fun KanaChartScreen(
    viewModel: NihongoViewModel,
    allKana: List<KanaMastery>,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Filter kana lists based on selected state with caching
    val filteredKana = remember(allKana, selectedTab, selectedCategory) {
        allKana.filter {
            it.kanaType == selectedTab && it.category == selectedCategory
        }
    }

    var selectedDetailKana by remember { mutableStateOf<KanaMastery?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle Hiragana & Katakana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf("HIRAGANA", "KATAKANA")
            tabs.forEach { tab ->
                val isActive = selectedTab == tab
                Button(
                    onClick = { viewModel.selectTab(tab) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .testTag("kana_tab_$tab"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (tab == "HIRAGANA") "Hiragana (ひらがな)" else "Katakana (カタカナ)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Category Filter Pills: Gojuon, Dakuten, Youon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categories = listOf(
                "Gojuon" to "Karakter Utama",
                "Dakuten" to "Tenten & Maru",
                "Youon" to "Gabungan"
            )

            categories.forEach { (catKey, catLabel) ->
                val isActive = selectedCategory == catKey
                FilterChip(
                    selected = isActive,
                    onClick = { viewModel.selectCategory(catKey) },
                    label = { Text(catLabel) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        // Japanese Character Grid
        if (filteredKana.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Karakter tidak ditemukan.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredKana, key = { it.char }) { kana ->
                    KanaCard(
                        kanaObj = kana,
                        onClick = { selectedDetailKana = kana }
                    )
                }
            }
        }
    }

    // Detail Practice Dialogue with physical tracing pad
    selectedDetailKana?.let { kana ->
        KanaDetailPracticeDialog(
            kanaObj = kana,
            onClose = { selectedDetailKana = null }
        )
    }
}

@Composable
fun KanaCard(
    kanaObj: KanaMastery,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick)
            .testTag("kana_card_${kanaObj.char}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = kanaObj.char,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kanaObj.romaji,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )

            // Mastery bar
            val total = kanaObj.correctCount + kanaObj.wrongCount
            if (total > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                val correctRatio = kanaObj.correctCount.toFloat() / total.toFloat()
                LinearProgressIndicator(
                    progress = { correctRatio },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4CAF50), // Green for correct answers
                    trackColor = Color(0xFFF44336) // Red for wrong answers
                )
            }
        }
    }
}

@Composable
fun KanaDetailPracticeDialog(
    kanaObj: KanaMastery,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    // TextToSpeech setup
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            var tts: TextToSpeech? = null
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = tts?.setLanguage(Locale.JAPANESE)
                        if (result != null && result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            isTtsReady = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ttsInstance = tts
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

     Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail & Latihan Menulis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kana display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = kanaObj.char,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = "Romaji: ${kanaObj.romaji}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Kategori: ${kanaObj.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Speak button
                        Button(
                            onClick = {
                                if (isTtsReady) {
                                    ttsInstance?.speak(kanaObj.char, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Putar Bunyi",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suara", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = "Praktik Menulis (Gambar di Kotak Bawah):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optimized Isolated Writing Pad
                WritingPracticePad(
                    refChar = kanaObj.char,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WritingPracticePad(
    refChar: String,
    modifier: Modifier = Modifier
) {
    val path = remember { mutableStateOf(Path()) }
    var pointsCount by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply {
                                addPath(path.value)
                                moveTo(offset.x, offset.y)
                            }
                            path.value = newPath
                            pointsCount++
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = change.position
                            val newPath = Path().apply {
                                addPath(path.value)
                                lineTo(currentOffset.x, currentOffset.y)
                            }
                            path.value = newPath
                            pointsCount++
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw center horizontal guide
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, height / 2),
                    end = Offset(width, height / 2),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )

                // Draw center vertical guide
                drawLine(
                    color = Color.LightGray,
                    start = Offset(width / 2, 0f),
                    end = Offset(width / 2, height),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )

                // Draw User trace line path
                drawPath(
                    path = path.value,
                    color = Color(0xFF1E88E5), // Blue ink
                    style = Stroke(
                        width = 8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            if (pointsCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gunakan jari untuk meniru $refChar",
                        color = Color.Gray.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracing Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    path.value = Path()
                    pointsCount = 0
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Bersihkan")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bersihkan Coretan")
            }
        }
    }
}
