package com.example.data.local

import androidx.room.*
import com.example.data.model.KanaMastery
import com.example.data.model.Vocabulary
import com.example.data.model.LevelStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface KanaDao {
    @Query("SELECT COUNT(*) FROM kana_mastery")
    suspend fun getCount(): Int

    @Query("SELECT * FROM kana_mastery")
    fun getAllKana(): Flow<List<KanaMastery>>

    @Query("SELECT * FROM kana_mastery WHERE kanaType = :type")
    fun getKanaByType(type: String): Flow<List<KanaMastery>>

    @Query("SELECT * FROM kana_mastery WHERE level = :level")
    fun getKanaByLevel(level: Int): Flow<List<KanaMastery>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(kanaList: List<KanaMastery>)

    @Update
    suspend fun updateKana(kana: KanaMastery)

    @Query("UPDATE kana_mastery SET correctCount = correctCount + 1, lastTested = :currentTime WHERE char = :char")
    suspend fun incrementCorrect(char: String, currentTime: Long)

    @Query("UPDATE kana_mastery SET wrongCount = wrongCount + 1, lastTested = :currentTime WHERE char = :char")
    suspend fun incrementWrong(char: String, currentTime: Long)
}

@Dao
interface VocabularyDao {
    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun getCount(): Int

    @Query("SELECT * FROM vocabulary WHERE book = :book ORDER BY lesson ASC, id ASC")
    fun getVocabByBook(book: String): Flow<List<Vocabulary>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vocabList: List<Vocabulary>)

    @Query("DELETE FROM vocabulary")
    suspend fun deleteAll()

    @Query("UPDATE vocabulary SET isMastered = :isMastered WHERE id = :id")
    suspend fun updateVocabMastery(id: Int, isMastered: Boolean)
}

@Dao
interface LevelStatusDao {
    @Query("SELECT COUNT(*) FROM level_status")
    suspend fun getCount(): Int

    @Query("SELECT * FROM level_status ORDER BY levelIndex ASC")
    fun getAllLevels(): Flow<List<LevelStatus>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(levels: List<LevelStatus>)

    @Query("UPDATE level_status SET isUnlocked = 1 WHERE levelIndex = :levelIndex")
    suspend fun unlockLevel(levelIndex: Int)

    @Query("UPDATE level_status SET highScore = :score WHERE levelIndex = :levelIndex AND highScore < :score")
    suspend fun updateHighScore(levelIndex: Int, score: Int)
}

@Database(entities = [KanaMastery::class, Vocabulary::class, LevelStatus::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kanaDao(): KanaDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun levelStatusDao(): LevelStatusDao
}
