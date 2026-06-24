package com.example.snake.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow


class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("snake_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) = prefs.edit().putString("auth_token", token).apply()
    fun getAuthToken(): String? = prefs.getString("auth_token", null)
    fun clearAuthToken() = prefs.edit().remove("auth_token").apply()
}


@Entity(tableName = "scores")
data class LocalScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String
)


@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: LocalScore)

    @Update
    suspend fun updateScore(score: LocalScore)

    @Query("SELECT * FROM scores WHERE username = :username LIMIT 1")
    suspend fun getScoreByUsername(username: String): LocalScore?

    @Query("SELECT * FROM scores ORDER BY score DESC")
    fun getAllScoresDescending(): Flow<List<LocalScore>>

    @Transaction
    suspend fun insertOrUpdateHigherScore(score: LocalScore) {
        val existing = getScoreByUsername(score.username)
        if (existing == null) {
            insertScore(score)
        } else if (score.score > existing.score) {
            updateScore(score.copy(id = existing.id))
        }
    }
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}


@Database(entities = [LocalScore::class, User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snake_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}