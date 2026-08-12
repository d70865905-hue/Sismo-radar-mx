package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EarthquakeEntity::class], version = 1, exportSchema = false)
abstract class SismoDatabase : RoomDatabase() {
    abstract fun earthquakeDao(): EarthquakeDao

    companion object {
        @Volatile
        private var INSTANCE: SismoDatabase? = null

        fun getDatabase(context: Context): SismoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SismoDatabase::class.java,
                    "sismo_radar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
