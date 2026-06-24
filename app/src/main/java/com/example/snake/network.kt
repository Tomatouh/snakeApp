package com.example.snake.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String?, val error: String?)

data class ReqResUser(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)
data class UserListResponse(val data: List<ReqResUser>)


interface ReqResApi {
    @POST("api/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @GET("api/users?per_page=12")
    suspend fun getGlobalPlayers(): UserListResponse
}


object RetrofitClient {
    private const val BASE_URL = "https://reqres.in/"

    private const val REQRES_API_KEY = "free_user_3FaNKuGXI0xnpDdBPTADs63lGC0"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest.newBuilder()
                .header("x-api-key", REQRES_API_KEY)
                .header("Content-Type", "application/json")
                .header("X-Reqres-Env", "prod")
                .build()
            chain.proceed(authenticatedRequest)
        }
        .build()

    val api: ReqResApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReqResApi::class.java)
    }
}