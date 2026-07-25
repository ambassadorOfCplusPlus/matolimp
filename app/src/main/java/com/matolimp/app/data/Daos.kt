package com.matolimp.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT COUNT(*) FROM themes")
    suspend fun themeCount(): Int

    @Query("SELECT * FROM themes ORDER BY `order`")
    fun themes(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE id = :id")
    suspend fun theme(id: String): ThemeEntity?

    @Query("SELECT * FROM subthemes WHERE themeId = :themeId ORDER BY `order`")
    fun subthemes(themeId: String): Flow<List<SubthemeEntity>>

    @Query("SELECT * FROM subthemes ORDER BY themeId, `order`")
    fun allSubthemes(): Flow<List<SubthemeEntity>>

    @Query("SELECT * FROM problems ORDER BY subthemeId, `order`")
    fun allProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE subthemeId = :subthemeId ORDER BY `order`")
    fun problems(subthemeId: String): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun problem(id: String): ProblemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemes(items: List<ThemeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubthemes(items: List<SubthemeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(items: List<ProblemEntity>)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun all(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE problemId = :problemId")
    suspend fun get(problemId: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("SELECT * FROM profile WHERE id = 0")
    fun profileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0")
    suspend fun profile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT themeId FROM purchases")
    fun purchasedThemeIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM purchases WHERE themeId = :themeId")
    suspend fun isPurchased(themeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Query("SELECT text FROM ai_cache WHERE problemId = :problemId")
    suspend fun aiExplanation(problemId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiExplanation(entity: AiCacheEntity)
}
