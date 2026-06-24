package com.example.snake.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data Models
data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String?, val error: String?)

data class ReqResUser(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)
data class UserListResponse(val data: List<ReqResUser>)

// API Interface
interface ReqResApi {
    @POST("api/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    // Second HTTP Request: Simulating downloading global high-score players
    @GET("api/users?per_page=12")
    suspend fun getGlobalPlayers(): UserListResponse
}

// Retrofit Client Singleton
object RetrofitClient {
    private const val BASE_URL = "https://reqres.in/"

    // TODO: Replace this string with the actual API key you copied from your ReqRes dashboard
    private const val REQRES_API_KEY = "free_user_3FaNKuGXI0xnpDdBPTADs63lGC0"

    // Set up OkHttpClient to inject required headers automatically into every call
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest.newBuilder()
                .header("x-api-key", REQRES_API_KEY)
                .header("Content-Type", "application/json")
                .header("X-Reqres-Env", "prod") // Ensures consistency on the platform
                .build()
            chain.proceed(authenticatedRequest)
        }
        .build()

    val api: ReqResApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Attach the client with our interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReqResApi::class.java)
    }
}