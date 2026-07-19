package com.example.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.data.Employee
import io.mockk.every

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoginSelectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `credential autofill dropdown displays saved credentials`() {
        // Setup SharedPreferences
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("rememberMe", true)
            .putString("savedEmail", "test@example.com")
            .putString("savedPassword", "mypassword")
            .apply()

        // Mock ViewModel
        val mockViewModel = mockk<OnSiteViewModel>(relaxed = true)
        every { mockViewModel.employees } returns MutableStateFlow(emptyList<Employee>())

        // Launch UI
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                LoginSelectionScreen(
                    viewModel = mockViewModel,
                    initialShowRegister = false
                )
            }
        }

        // Click the text field to expand the dropdown
        composeTestRule.onNodeWithText("Enter your registered email for login").performClick()
        
        // Now the dropdown should be expanded and the autofill text should be visible
        composeTestRule.onNodeWithText("Autofill: test@example.com").assertIsDisplayed()
        
        // Click the autofill option
        composeTestRule.onNodeWithText("Autofill: test@example.com").performClick()

        // Now the text field should be populated with the email
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }
}
