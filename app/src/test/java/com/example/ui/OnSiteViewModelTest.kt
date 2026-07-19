package com.example.ui

import com.example.data.Employee
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.api.ApiClient
import com.example.api.AuthApiService
import com.example.api.MessagesApiService
import com.example.api.LoginResponse
import android.app.Application
import com.example.data.AppDatabase
import com.example.data.AppDao
import kotlinx.coroutines.flow.flowOf
import com.example.data.AttendanceLog
import com.example.data.Message
import com.example.data.SystemAlert
import com.example.data.TodoItem
import com.example.data.LeaveRequest
import com.example.data.SummonAlert

@OptIn(ExperimentalCoroutinesApi::class)
class OnSiteViewModelTest {

    private lateinit var viewModel: OnSiteViewModel
    private val mockApplication = mockk<Application>(relaxed = true)
    private val mockDao = mockk<AppDao>(relaxed = true)
    private val mockDatabase = mockk<AppDatabase>(relaxed = true)
    private val mockAuthService = mockk<AuthApiService>(relaxed = true)
    private val mockMessagesService = mockk<MessagesApiService>(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkObject(AppDatabase.Companion)
        every { AppDatabase.getDatabase(any()) } returns mockDatabase
        every { mockDatabase.appDao() } returns mockDao
        every { mockDao.getAllEmployeesFlow() } returns flowOf(emptyList<Employee>())
        every { mockDao.getAllAttendanceLogsFlow() } returns flowOf(emptyList<AttendanceLog>())
        every { mockDao.getAllMessagesFlow() } returns flowOf(emptyList<Message>())
        every { mockDao.getAllAlertsFlow() } returns flowOf(emptyList<SystemAlert>())
        every { mockDao.getAllTodoItemsFlow() } returns flowOf(emptyList<TodoItem>())
        every { mockDao.getAllLeaveRequestsFlow() } returns flowOf(emptyList<LeaveRequest>())
        every { mockDao.getActiveSummonsForStaffFlow(any()) } returns flowOf(emptyList<SummonAlert>())
        
        mockkObject(ApiClient)
        every { ApiClient.authService } returns mockAuthService
        every { ApiClient.messagesService } returns mockMessagesService
        
        viewModel = OnSiteViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loginUser with invalid credentials returns error via callback`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        
        coEvery { mockAuthService.login(any()) } throws Exception("Invalid credentials")

        var resultError: String? = null
        var resultEmployee: Employee? = null
        var resultRequires2FA = false

        viewModel.loginUser(email, password) { err, emp, r2fa ->
            resultError = err
            resultEmployee = emp
            resultRequires2FA = r2fa
        }
        advanceUntilIdle()

        assertEquals("Network error or server unavailable.", resultError)
        assertNull(resultEmployee)
        assertFalse(resultRequires2FA)
    }

    @Test
    fun `loginUser with valid credentials invokes completeLogin and returns success via callback`() = runTest {
        val email = "test@example.com"
        val password = "password"
        
        val mockEmployee = Employee(uuid = "1", email = email, role = "Staff", is_mfa_enabled = false)
        val mockResponse = LoginResponse(
            user = mockEmployee,
            session = null,
            requires2FA = false,
            authId = null
        )
        coEvery { mockAuthService.login(any()) } returns mockResponse

        var resultError: String? = null
        var resultEmployee: Employee? = null
        var resultRequires2FA = false

        viewModel.loginUser(email, password) { err, emp, r2fa ->
            resultError = err
            resultEmployee = emp
            resultRequires2FA = r2fa
        }
        advanceUntilIdle()

        assertNull(resultError)
        assertEquals(mockEmployee, resultEmployee)
        assertFalse(resultRequires2FA)
        coVerify { mockDao.insertEmployees(listOf(mockEmployee)) }
    }

    @Test
    fun `deleteChatWithUser calls repository and api correctly`() = runTest {
        // Given a current user
        val user = Employee(uuid = "user1", email = "test1@example.com", role = "Staff")
        val mockResponse = LoginResponse(user = user, session = null, requires2FA = false)
        coEvery { mockAuthService.login(any()) } returns mockResponse
        
        viewModel.loginUser(user.email, "password") { _, _, _ -> }
        advanceUntilIdle()

        // Call the method to test
        val otherUserId = "user2"
        viewModel.deleteChatWithUser(otherUserId)
        advanceUntilIdle()

        // Verify local DB deletion
        coVerify { mockDao.deleteMessagesBetween(user.uuid, otherUserId) }

        // Verify remote API deletion
        coVerify { mockMessagesService.deleteMessages(user.uuid, otherUserId) }
    }
}
