package com.matolimp.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ThemeEntity::class,
        SubthemeEntity::class,
        ProblemEntity::class,
        ProgressEntity::class,
        ProfileEntity::class,
        PurchaseEntity::class,
        AiCacheEntity::class
    ],
    version = 23,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "matolimp.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
