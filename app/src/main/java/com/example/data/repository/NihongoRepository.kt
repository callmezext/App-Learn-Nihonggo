package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.KanaMastery
import com.example.data.model.Vocabulary
import com.example.data.model.LevelStatus
import kotlinx.coroutines.flow.Flow
import android.util.Log

class NihongoRepository(private val database: AppDatabase) {

    private val kanaDao = database.kanaDao()
    private val vocabularyDao = database.vocabularyDao()
    private val levelStatusDao = database.levelStatusDao()

    val allLevels: Flow<List<LevelStatus>> = levelStatusDao.getAllLevels()
    val allKana: Flow<List<KanaMastery>> = kanaDao.getAllKana()

    fun getKanaByType(type: String): Flow<List<KanaMastery>> = kanaDao.getKanaByType(type)
    fun getKanaByLevel(level: Int): Flow<List<KanaMastery>> = kanaDao.getKanaByLevel(level)
    fun getVocabByBook(book: String): Flow<List<Vocabulary>> = vocabularyDao.getVocabByBook(book)

    suspend fun ensureDataPopulated() {
        try {
            if (levelStatusDao.getCount() == 0) {
                levelStatusDao.insertAll(InitialData.initialLevels)
                Log.d("NihongoRepository", "Levels prepopulated.")
            }
            if (kanaDao.getCount() == 0) {
                kanaDao.insertAll(InitialData.initialKana)
                Log.d("NihongoRepository", "Kana prepopulated.")
            }
            if (vocabularyDao.getCount() < 50) {
                vocabularyDao.deleteAll()
                vocabularyDao.insertAll(InitialData.initialVocabulary)
                Log.d("NihongoRepository", "Vocabulary prepopulated and expanded.")
            }
        } catch (e: Exception) {
            Log.e("NihongoRepository", "Error during prepopulation", e)
        }
    }

    suspend fun recordTestResult(char: String, wasCorrect: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (wasCorrect) {
            kanaDao.incrementCorrect(char, currentTime)
        } else {
            kanaDao.incrementWrong(char, currentTime)
        }
    }

    suspend fun updateVocabMastery(id: Int, isMastered: Boolean) {
        vocabularyDao.updateVocabMastery(id, isMastered)
    }

    suspend fun recordLevelCompleted(levelIndex: Int, scorePercentage: Int) {
        // Save matching high score
        levelStatusDao.updateHighScore(levelIndex, scorePercentage)

        // If high score is >= 60%, unlock the next level (if within bounds 1..18)
        if (scorePercentage >= 60 && levelIndex < 19) {
            levelStatusDao.unlockLevel(levelIndex + 1)
        }
    }
}
