package com.example.api

import com.example.data.Employee
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: Employee?,
    val session: SessionData?,
    val requires2FA: Boolean? = null,
    val authId: String? = null
)

data class Setup2FARequest(val email: String)
data class Setup2FAResponse(val secret: String, val qrCode: String)
data class Verify2FASetupRequest(val email: String, val token: String)
data class Verify2FASetupResponse(val success: Boolean)
data class Verify2FALoginRequest(val email: String, val password: String, val token: String)
data class Verify2FALoginResponse(val user: Employee, val session: SessionData)

data class SessionData(
    val access_token: String,
    val refresh_token: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val role: String,
    val department: String,
    val custom_id: String?,
    val otp: String,
    val age: Int?,
    val dob: String?,
    val fathers_name: String?,
    val mothers_name: String?,
    val address: String?,
    val phone: String?,
    val emergency_contact: String?,
    val doj: String?,
    val blood_group: String?
)

data class RegisterResponse(
    val message: String,
    val user: Employee
)

// API Interface
interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ChangePasswordResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ChangePasswordResponse

    @POST("api/auth/2fa/setup")
    suspend fun setup2FA(@Body request: Setup2FARequest): Setup2FAResponse

    @POST("api/auth/2fa/verify-setup")
    suspend fun verify2FASetup(@Body request: Verify2FASetupRequest): Verify2FASetupResponse

    @POST("api/auth/2fa/login")
    suspend fun verify2FALogin(@Body request: Verify2FALoginRequest): Verify2FALoginResponse

    @POST("api/auth/2fa/disable")
    suspend fun disable2FA(@Body request: Setup2FARequest): Verify2FASetupResponse
}

data class SendOtpRequest(val email: String)
data class SendOtpResponse(val message: String)

data class ChangePasswordRequest(
    val email: String,
    val currentPassword: String,
    val newPassword: String
)
data class ChangePasswordResponse(val message: String)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

// Retrofit Client
object ApiClient {
    // Using the live backend deployed on Render
    private const val BASE_URL = "https://gupta-lakhani-and-associates.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()

    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val tasksService: TasksApiService = retrofit.create(TasksApiService::class.java)
    val leavesService: LeavesApiService = retrofit.create(LeavesApiService::class.java)
}

interface TasksApiService {
    @POST("api/tasks")
    suspend fun createTask(@Body task: com.example.data.TodoItem): Any
}

interface LeavesApiService {
    @POST("api/leaves")
    suspend fun createLeave(@Body leave: com.example.data.LeaveRequest): Any
}
