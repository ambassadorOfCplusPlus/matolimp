package com.matolimp.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Версия схемы Room (`version`) отвечает ТОЛЬКО за структуру таблиц. Обновление
 * контента (seed.json) НЕ должно её менять — для этого есть [Repository.CONTENT_VERSION],
 * которая пересевает лишь таблицы контента, сохраняя прогресс/профиль/покупки.
 *
 * Поэтому `version` заморожена на 23. Бампить её нужно лишь при РЕАЛЬНОЙ смене схемы
 * (новые поля/таблицы) — и тогда добавлять `addMigrations(...)`, а не полагаться на
 * деструктивный fallback (он оставлен лишь как аварийная страховка на время pre-release).
 */
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
