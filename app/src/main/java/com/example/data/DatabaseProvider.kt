package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.NihongoRepository

object DatabaseProvider {
    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var repository: NihongoRepository? = null

    fun getRepository(context: Context): NihongoRepository {
        return repository ?: synchronized(this) {
            repository ?: run {
                val db = database ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nihongo_master.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                database = db
                val repo = NihongoRepository(db)
                repository = repo
                repo
            }
        }
    }
}
