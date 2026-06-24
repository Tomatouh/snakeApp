package com.example.snake.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. SharedPreferences Helper
class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("snake_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) = prefs.edit().putString("auth_token", token).apply()
    fun getAuthToken(): String? = prefs.getString("auth_token", null)
    fun clearAuthToken() = prefs.edit().remove("auth_token").apply()
}

// 2. Room Database Entities
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

// 3. Room DAOs
@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: LocalScore)

    @Query("SELECT * FROM scores ORDER BY score DESC")
    fun getAllScoresDescending(): Flow<List<LocalScore>>
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}

// 4. Room Database Instance
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
                .fallbackToDestructiveMigration(true) // handle version change easily
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}