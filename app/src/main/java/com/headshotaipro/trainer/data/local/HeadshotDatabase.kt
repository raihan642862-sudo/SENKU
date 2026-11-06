package com.headshotaipro.trainer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrainingSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HeadshotDatabase : RoomDatabase() {

    abstract fun trainingSessionDao(): TrainingSessionDao

    companion object {
        @Volatile
        private var INSTANCE: HeadshotDatabase? = null

        fun getInstance(context: Context): HeadshotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeadshotDatabase::class.java,
                    "headshot_ai_pro.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
