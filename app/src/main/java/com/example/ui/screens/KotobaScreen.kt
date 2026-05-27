package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

data class BabSyllabus(
    val babId: Int,
    val title: String,
    val material: String,
    val description: String,
    val icon: String
)

val minnaSyllabusList = listOf(
    BabSyllabus(1, "Perkenalan Diri", "Partikel WA, DESU, & KA", "Mempelajari tata cara menyapa pelafalan dasar, menyebutkan identitas diri, profesi, kewarganegaraan, dan kata tunjuk orang.", "👋"),
    BabSyllabus(2, "Benda & Kepemilikan", "Tunjuk KORE/SORE/ARE & Partikel NO", "Mengidentifikasi barang-barang pribadi di sekitar, menanyakan buku, kamus, tas, dompet, serta kepunyaan.", "🎒"),
    BabSyllabus(3, "Tempat & Lokasi", "Lokasi KOKO/SOKO/ASOKO & DOKO", "Menunjuk posisi koordinat ruangan di sekolah/kantor, toilet, rumah, letak negara asal, dan harga mata uang.", "🏢"),
    BabSyllabus(4, "Waktu & Jam", "Waktu JI/FUN, Jam & Kara-Made", "Mempelajari penyebutan jam, menit secara akurat, nama-nama hari, konsep pagi-siang-malam, serta jam kerja bank.", "⏰"),
    BabSyllabus(5, "Transportasi", "Tujuan IKUMASU/KIMASU & Kata Depan E", "Mengutarakan pergerakan mobilitas tempat dengan aneka armada transportasi (mobil, taksi, shinkansen, dll.) bersama relasi.", "🚄"),
    BabSyllabus(6, "Kegiatan & Makanan", "Verba Transitif + Partikel O (WO)", "Mengekspresikan tindakan makan-minum, hobi rekreasi harian, serta kalimat ajakan (issho ni) bersosialisasi.", "🍱"),
    BabSyllabus(7, "Alat & Hadiah", "Metode DE & Aksi AGEMASU/MORAIMASU", "Menyatakan tindakan pengerjaan memakai sumpit/pisau, serta interaksi sosial bertukar kado atau meminjamkan barang.", "🎁"),
    BabSyllabus(8, "Deskripsi Sifat", "Adjektiva-I & Adjektiva-Na", "Menerangkan karakteristik atau kualitas dari suatu tempat, objek, ataupun seseorang menggunakan bermacam ragam kata sifat.", "✨"),
    BabSyllabus(9, "Minat & Bakat", "Verba GA WAKARIMASU/ARIMASU & SUKI", "Menyatakan hal-hal kegemaran pribadi, kepemilikan benda konkret, tingkat kemahiran bidang seni/olahraga, serta alasan.", "🎨"),
    BabSyllabus(10, "Tata Ruang & Letak", "Eksistensi IMASU/ARIMASU & Atas/Bawah", "Mendeskripsikan keberadaan benda mati/hewan di area spasial secara rinci memakai terminologi arah (seperti ue, shita).", "🐾")
)

val habikiSyllabusList = listOf(
    BabSyllabus(1, "Kesehatan & K3 Kerja", "Anzen, Chuui, Jishin, Hinan", "Kumpulan istilah keselamatan kerja di pabrik Jepang, kesiagaan gempa bumi, persiapan darurat, dan instruksi evakuasi.", "⚠️"),
    BabSyllabus(2, "Lolos Wawancara", "Mensetsu, Shitsumon, CV, Shoukai", "Kosakata pamungkas memenangkan rekrutmen kerja, persiapan draf resume CV Jepang, motivasi, dan perihal wawancara resmi.", "💼"),
    BabSyllabus(3, "Pabrik & Bengkel", "Shigoto, Zangyou, Kyuuryou", "Kamus istilah jam kerja pabrik jepang, perhitungan gaji upah kerja, lembur darurat, seragam pengaman, serta perkakas mekanik.", "⚙️")
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KotobaScreen(
    viewModel: NihongoViewModel,
    minnaVocab: List<Vocabulary>,
    habikiVocab: List<Vocabulary>,
    modifier: Modifier = Modifier
) {
    var selectedBook by remember { mutableStateOf("MINNA") } // "MINNA" or "HABIKI"
    var selectedLessonFilter by remember { mutableStateOf<Int?>(1) } // Default to Bab 1 for better focus!
    var isFlashcardMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSubTab by remember { mutableStateOf(0) } // 0 = Hafalan Bab & Silabus, 1 = 10 Tingkat Kuis

    val currentVocabList = if (selectedBook == "MINNA") minnaVocab else habikiVocab
    val currentSyllabusList = if (selectedBook == "MINNA") minnaSyllabusList else habikiSyllabusList

    // Filter vocab list by lesson and search query
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
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Segment 1: Main Sub-Tabs
        TabRow(
            selectedTabIndex = currentSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentSubTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = currentSubTab == 0,
                onClick = { currentSubTab = 0 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Materi Bab", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("10 Level Kuis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        if (currentSubTab == 0) {
            // ================= SUBTAB 0: BUKU HAFALAN & CAROUSEL BAB =================
            
            // Book Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                            selectedLessonFilter = 1 // Default to Chapter 1 on switch
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .testTag("book_tab_$bookKey"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = bookLabel, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Carousel Pilihan Bab
            Text(
                text = "Pilih Bab Pelajaran:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                textAlign = TextAlign.Start
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // Option to view "All Bab"
                item {
                    val isActive = selectedLessonFilter == null
                    Card(
                        modifier = Modifier
                            .width(80.dp)
                            .height(64.dp)
                            .clickable { selectedLessonFilter = null },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isActive) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📚", fontSize = 18.sp)
                            Text("Semua", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(currentSyllabusList) { syllabus ->
                    val isActive = selectedLessonFilter == syllabus.babId
                    Card(
                        modifier = Modifier
                            .width(96.dp)
                            .height(64.dp)
                            .clickable { selectedLessonFilter = syllabus.babId },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isActive) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(syllabus.icon, fontSize = 18.sp)
                            Text("Bab ${syllabus.babId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(syllabus.title, maxLines = 1, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Silabus Pelajaran Penjelasan Card
            val activeSyllabus = currentSyllabusList.find { it.babId == selectedLessonFilter }
            if (activeSyllabus != null) {
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(vertical = 4.dp),
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activeSyllabus.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Materi Bab ${activeSyllabus.babId}: ${activeSyllabus.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fokus Pola: ${activeSyllabus.material}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeSyllabus.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                            lineHeight = 14.sp
                        )
                    }
                }
            } else {
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(vertical = 4.dp),
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Membuka seluruh materi dan silabus perbendaharaan kata $selectedBook.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Search Bar & Flashcard Mode controllers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar in row if List mode
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari Kosakata...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // Flashcard toggle
                Button(
                    onClick = { isFlashcardMode = !isFlashcardMode },
                    modifier = Modifier.width(116.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isFlashcardMode) Icons.Default.List else Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFlashcardMode) "Daftar" else "Kartu", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Quick Quiz Button
            Button(
                onClick = { viewModel.startVocabQuiz(selectedBook, selectedLessonFilter) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("start_vocab_quiz_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mulai Kuis Cepat Bab Ini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Main learning list or deck
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
                    FlashcardDeck(
                        vocabList = filteredVocab,
                        onToggleMastered = { viewModel.toggleVocabularyMastered(it) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

        } else {
            // ================= SUBTAB 1: 10 KUIS BERJENJANG =================
            Text(
                text = "Tantangan 10 Tingkat Kuis Kosakata",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Pilih level ujian di bawah. Ujian dirancang bergradasi progresif dari mudah ke sulit disesuaikan dengan kurikulum bab.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 14.sp
            )

            val quizzes = listOf(
                Triple(1, "Kuis 1: Perkenalan Diri (Sangat Mudah)", "Cakupan: Bab 1 (Kosakata Salam & Pekerjaan)"),
                Triple(2, "Kuis 2: Benda Sekitar (Mudah)", "Cakupan: Bab 1-2 (Benda dasar & Kepemilikan)"),
                Triple(3, "Kuis 3: Tempat & Lokasi (Mudah)", "Cakupan: Bab 1-3 (Lobi, Kantor, & Kelas)"),
                Triple(4, "Kuis 4: Mengucapkan Waktu (Menengah)", "Cakupan: Bab 4 (Pengucapan jam, menit, & hari)"),
                Triple(5, "Kuis 5: Review Awal Bab (Menengah)", "Cakupan: Bab 1-4 (Ujian Akurasi Menengah)"),
                Triple(6, "Kuis 6: Bepergian (Menengah)", "Cakupan: Bab 5 (Armada transportasi, rute, & transit)"),
                Triple(7, "Kuis 7: Kerja Transitif (Menantang)", "Cakupan: Bab 6 (Partikel WO, makanan, hobi biasa)"),
                Triple(8, "Kuis 8: Hadiah & Bahasa (Menantang)", "Cakupan: Bab 7 (Memberi ageru, menerima morau, alat)"),
                Triple(9, "Kuis 9: Berbagai Kata Sifat (Sulit)", "Cakupan: Bab 8 (Sifat i-adjective & na-adjective)"),
                Triple(10, "Kuis 10: Kompetensi & Letak (Sangat Sulit)", "Cakupan: Bab 9-10 (Eksistensi arimasu/imasu, posisi, hobi)")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quizzes) { (quizId, quizTitle, quizCoverage) ->
                    val difficultyColor = when (quizId) {
                        1 -> Color(0xFF2E7D32) // green
                        2, 3 -> Color(0xFF00796B) // teal
                        4, 5, 6 -> Color(0xFF1976D2) // blue
                        7, 8 -> Color(0xFFF57C00) // orange
                        else -> Color(0xFFD32F2F) // red
                    }

                    val difficultyLabel = when (quizId) {
                        1 -> "Sangat Mudah ⭐"
                        2, 3 -> "Mudah ⭐⭐"
                        4, 5, 6 -> "Menengah ⭐⭐⭐"
                        7, 8 -> "Menantang ⭐⭐⭐⭐"
                        else -> "Sulit/Sangat Sulit ⭐⭐⭐⭐⭐"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.startGradedVocabQuiz(selectedBook, quizId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(2.dp, difficultyColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(difficultyColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "$quizId", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = quizTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = quizCoverage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(difficultyColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = difficultyLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = difficultyColor
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.startGradedVocabQuiz(selectedBook, quizId) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = difficultyColor.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Mulai Kuis",
                                    tint = difficultyColor
                                )
                            }
                        }
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        q.options.forEach { option ->
                            val isSelected = option == quizState.selectedAnswer
                            val isCorrectAnswer = option == correctAnswer
                            val isDark = MaterialTheme.colorScheme.background.red < 0.25f

                            val btnColor = when {
                                !quizState.isAnswered -> MaterialTheme.colorScheme.surface
                                isCorrectAnswer -> if (isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
                                isSelected -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }

                            val borderCol = when {
                                !quizState.isAnswered -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                isCorrectAnswer -> if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50)
                                isSelected -> if (isDark) Color(0xFFE57373) else Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            val txtColor = when {
                                !quizState.isAnswered -> MaterialTheme.colorScheme.onSurface
                                isCorrectAnswer -> if (isDark) Color.White else Color(0xFF2E7D32)
                                isSelected -> if (isDark) Color.White else Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !quizState.isAnswered) { onAnswerSelected(option) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = btnColor),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderCol))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = txtColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Incorrect answer feedback for vocab
                    val showFeedback = quizState.isAnswered && quizState.selectedAnswer != correctAnswer
                    AnimatedVisibility(visible = showFeedback) {
                        val isDark = MaterialTheme.colorScheme.background.red < 0.25f
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFFEF4444) else Color(0xFFF44336))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Kurang tepat",
                                    tint = if (isDark) Color(0xFFF87171) else Color(0xFFF44336),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Jawaban Kurang Tepat! ❌",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFC62828)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Kamu memilih: '${quizState.selectedAnswer}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    )
                                    Text(
                                        text = "Jawaban yang benar: '$correctAnswer'",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isDark) Color(0xFF86EFAC) else Color(0xFF2E7D32)
                                    )
                                }
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
