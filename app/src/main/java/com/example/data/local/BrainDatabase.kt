package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActivityRecord
import com.example.data.model.DailyReward
import com.example.data.model.GameProgress
import com.example.data.model.UnlockedPerk
import com.example.data.model.UserStats

@Database(
    entities = [
        UserStats::class,
        GameProgress::class,
        UnlockedPerk::class,
        DailyReward::class,
        ActivityRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrainDatabase : RoomDatabase() {
    abstract fun brainDao(): BrainDao

    companion object {
        @Volatile
        private var INSTANCE: BrainDatabase? = null

        fun getDatabase(context: Context): BrainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrainDatabase::class.java,
                    "brain_sparks_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
