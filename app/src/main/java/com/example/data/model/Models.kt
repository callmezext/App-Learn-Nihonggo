package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kana_mastery")
data class KanaMastery(
    @PrimaryKey val char: String,
    val romaji: String,
    val kanaType: String, // "HIRAGANA" or "KATAKANA"
    val category: String, // "Gojuon" (Karakter Utama), "Dakuten" (Tenten/Maru), "Youon" (Gabungan)
    val level: Int,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastTested: Long = 0L
) {
    val masteryPercentage: Float
        get() {
            val total = correctCount + wrongCount
            if (total == 0) return 0f
            return (correctCount.toFloat() / total.toFloat()) * 100f
        }
}

@Entity(tableName = "vocabulary")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jpn: String,       // "わたし" / "つくえ"
    val kanji: String,     // "私" / "机"
    val romaji: String,    // "watashi" / "tsukue"
    val meaning: String,   // "saya" / "meja"
    val book: String,      // "MINNA" or "HABIKI"
    val lesson: Int,       // 1, 2, 3...
    val isMastered: Boolean = false
)

@Entity(tableName = "level_status")
data class LevelStatus(
    @PrimaryKey val levelIndex: Int,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val highScore: Int = 0 // 0 to 100 percentage
)
