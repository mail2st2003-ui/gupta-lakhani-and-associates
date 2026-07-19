package com.example.api

import com.example.data.Employee
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.DELETE
import java.util.concurrent.TimeUnit

// Models
data class OverlapResponse(val overlap: Boolean)

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
data class ProfileImageRequest(val profile_image: String?)

data class SessionData(
    val access_token: String,
    val refresh_token: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val first_name: String,
    val middle_name: String?,
    val last_name: String,
    val role: String,
    val designation: String,
    val custom_id: String?,
    val otp: String,
    val age: Int?,
    val dob: String?,
    val father_name: String?,
    val mother_name: String?,
    val permanent_address: String?,
    val current_address: String?,
    val contact: String?,
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

    @PUT("api/auth/profile/{userUuid}")
    suspend fun updateProfile(@Path("userUuid") userUuid: String, @Body profile: com.example.data.UserDetails): Any

    @PUT("api/auth/profile/{userUuid}/photo")
    suspend fun updateProfileImage(@Path("userUuid") userUuid: String, @Body request: ProfileImageRequest): Any

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

    @GET("api/auth/users")
    suspend fun getAllUsers(): List<com.example.data.Employee>

    @GET("api/auth/profile/{userUuid}")
    suspend fun getUserProfile(@Path("userUuid") userUuid: String): com.example.data.UserDetails
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
    private const val BASE_URL = "https://gupta-lakhani-and-associates-szv9.onrender.com/"

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
    val todosService: TodosApiService = retrofit.create(TodosApiService::class.java)
    val leavesService: LeavesApiService = retrofit.create(LeavesApiService::class.java)
    val messagesService: MessagesApiService = retrofit.create(MessagesApiService::class.java)
}

interface TasksApiService {
    @GET("api/tasks")
    suspend fun getTasks(
        @Query("employee_id") employeeId: String
    ): List<com.example.data.TodoItem>

    @POST("api/tasks")
    suspend fun createTask(@Body task: com.example.data.TodoItem): Any
}

interface TodosApiService {
    @GET("api/todos/user/{userUuid}")
    suspend fun getUserTodos(
        @Path("userUuid") userUuid: String
    ): List<com.example.data.TodoItem>

    @POST("api/todos")
    suspend fun createTodo(@Body todo: com.example.data.TodoItem): Any

    @PUT("api/todos/{id}")
    suspend fun updateTodo(@Path("id") id: String, @Body todo: com.example.data.TodoItem): Any

    @retrofit2.http.DELETE("api/todos/{id}")
    suspend fun deleteTodo(@Path("id") id: String): Any
}

interface LeavesApiService {
    @POST("api/leaves")
    suspend fun createLeave(@Body leave: com.example.data.LeaveRequest): Any

    @PUT("api/leaves/{id}")
    suspend fun updateLeave(@Path("id") id: String, @Body leave: com.example.data.LeaveRequest): Any

    @retrofit2.http.DELETE("api/leaves/{id}")
    suspend fun deleteLeave(@Path("id") id: String): Any

    @GET("api/leaves/user/{userUuid}")
    suspend fun getUserLeaves(@Path("userUuid") userUuid: String): List<com.example.data.LeaveRequest>

    @GET("api/leaves/check-overlap/{userUuid}")
    suspend fun checkOverlap(
        @Path("userUuid") userUuid: String,
        @Query("start") start: Long,
        @Query("end") end: Long,
        @Query("exclude") exclude: String? = null
    ): OverlapResponse
}

interface MessagesApiService {
    @POST("api/messages")
    suspend fun sendMessage(@Body message: com.example.data.Message): Any

    @GET("api/messages/{userUuid}/{otherUuid}")
    suspend fun getUserMessages(
        @Path("userUuid") userUuid: String,
        @Path("otherUuid") otherUuid: String
    ): List<com.example.data.Message>

    @GET("api/messages/{userUuid}")
    suspend fun getAllMessagesForUser(
        @Path("userUuid") userUuid: String
    ): List<com.example.data.Message>

    @DELETE("api/messages/{userUuid}/{otherUuid}")
    suspend fun deleteMessages(
        @Path("userUuid") userUuid: String,
        @Path("otherUuid") otherUuid: String
    )
}
