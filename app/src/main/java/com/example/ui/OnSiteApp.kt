package com.example.ui
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.horizontalScroll


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.data.AttendanceLog
import com.example.data.Employee
import com.example.data.Message
import com.example.data.SystemAlert
import com.example.data.TodoItem
import com.example.data.LeaveRequest
import com.example.data.DetailedProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import android.app.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnSiteApp(viewModel: OnSiteViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isManager by viewModel.isManager.collectAsStateWithLifecycle()
    val activeBanner by viewModel.activeBannerAlert.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    var landingMode by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tabs = if (isManager) {
        listOf("Partner Ledger", "On-Site Register", "Attendance", "Group Discussion", "Talk to", "Task Manager", "Leave Requests", "My Profile")
    } else {
        listOf("On-Site Register", "Task Manager", "Talk to", "Group Discussion", "Leave Request", "My Profile")
    }

    val icons = if (isManager) {
        listOf(Icons.Default.AccountBalance, Icons.Default.LocationOn, Icons.Default.CalendarMonth, Icons.Default.Forum, Icons.Default.Chat, Icons.Default.Assignment, Icons.Default.EventNote, Icons.Default.Person)
    } else {
        listOf(Icons.Default.LocationOn, Icons.Default.Checklist, Icons.Default.Chat, Icons.Default.Forum, Icons.Default.EventNote, Icons.Default.Person)
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            selectedTab = 0
            landingMode = null
        }
    }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentUser != null) {
            if (selectedTab != 0) {
                selectedTab = 0
            } else {
                showExitDialog = true
            }
        } else {
            if (landingMode != null) {
                landingMode = null
            } else {
                showExitDialog = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentUser != null,
        drawerContent = {
            if (currentUser != null) {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.width(300.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Firm Logo",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gupta Lakhani & Associates",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "CHARTERED ACCOUNTANTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = if (isManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentUser!!.name.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser!!.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                DesignationBadge(empId = currentUser!!.id)
                                Text(
                                    text = "ID: ${currentUser!!.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = "NAVIGATION SECTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tabs.size) { index ->
                            val label = tabs[index]
                            val icon = icons[index]
                            val selected = selectedTab == index

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    selectedTab = index
                                    scope.launch { drawerState.close() }
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("drawer_item_$index")
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Cloud,
                                contentDescription = "Offline state icon",
                                tint = if (isOfflineMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Offline Mode",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { viewModel.setOfflineMode(it) },
                            modifier = Modifier.testTag("drawer_offline_toggle")
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logoutSession()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        if (currentUser != null) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Sidebar Navigation Menu"
                                )
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "OnSite Shield",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "Gupta Lakhani & Associates",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isOfflineMode) "Offline Mode Enabled" else "M.P. Nagar Chambers • Bhopal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOfflineMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
                        
                        // Offline Mode Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Offline Mode",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = isOfflineMode,
                                onCheckedChange = { viewModel.setOfflineMode(it) },
                                modifier = Modifier.testTag("offline_toggle")
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Urgent Announcement Banner System
            AnimatedVisibility(
                visible = activeBanner != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeBanner?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("urgent_alert_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Urgent Notification",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = alert.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Broadcasted by ${alert.senderName} at ${formatTime(alert.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissActiveBanner() },
                                modifier = Modifier.testTag("dismiss_banner_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner"
                                )
                            }
                        }
                    }
                }
            }

            if (showExitDialog) {
                ExitConfirmationDialog(
                    onConfirm = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    onDismiss = {
                        showExitDialog = false
                    }
                )
            }

            if (currentUser == null) {
                if (landingMode == null) {
                    OnSiteAuthLandingScreen(
                        onLoginClick = { landingMode = "login" },
                        onSignUpClick = { landingMode = "signup" }
                    )
                } else {
                    LoginSelectionScreen(
                        viewModel = viewModel,
                        initialShowRegister = (landingMode == "signup"),
                        onBack = { landingMode = null }
                    )
                }
            } else {
                MainDashboardContainer(
                    viewModel = viewModel,
                    currentUser = currentUser!!,
                    isManager = isManager,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}
}

fun getMaskedId(id: String, isCa: Boolean): String {
    if (isCa) return id
    if (id.length <= 4) return "***"
    return if (id.startsWith("EMP-")) {
        val parts = id.split("-")
        if (parts.size >= 3) {
            "${parts[0]}-***-${parts[2]}"
        } else {
            "EMP-***-" + id.takeLast(2)
        }
    } else {
        id.take(3) + "-***-" + id.takeLast(2)
    }
}

fun getEmployeeDesignation(id: String): String {
    return when (id) {
        "CA-GU-01", "CA-LA-02" -> "PARTNER"
        "EMP-RS-54", "EMP-RM-19" -> "ARTICLE"
        "EMP-PP-88", "EMP-VS-22" -> "STAFF"
        else -> "STAFF"
    }
}

@Composable
fun DesignationBadge(empId: String, modifier: Modifier = Modifier) {
    val designation = getEmployeeDesignation(empId)
    if (designation == "PARTNER") return

    val (containerColor, contentColor) = when (designation) {
        "ARTICLE" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Soft Green
        "STAFF" -> Color(0xFFE3F2FD) to Color(0xFF1565C0) // Soft Blue
        else -> Color(0xFFECEFF1) to Color(0xFF37474F) // Grey
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = designation,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun LoginSelectionScreen(
    viewModel: OnSiteViewModel,
    initialShowRegister: Boolean = false,
    onBack: () -> Unit = {}
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    var isRegisterMode by remember { mutableStateOf(initialShowRegister) }
    val scope = rememberCoroutineScope()

    // Login Form State
    var loginUserId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Forgot Password State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var isForgotRequestingOtp by remember { mutableStateOf(false) }
    var isForgotVerifyingOtp by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotOtp by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var isForgotNewPasswordVisible by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableStateOf(1) } // 1: Email, 2: OTP & New Password
    var forgotError by remember { mutableStateOf("") }
    var forgotSuccess by remember { mutableStateOf("") }

    // Signup Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Employee") } // "Employee" or "Manager"
    var regDept by remember { mutableStateOf("Article") }
    var regCustomId by remember { mutableStateOf("") }
    var regError by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    // 2FA Login State
    var show2FADialog by remember { mutableStateOf(false) }
    var login2FACode by remember { mutableStateOf("") }
    var login2FAError by remember { mutableStateOf("") }
    var isVerifying2FA by remember { mutableStateOf(false) }

    // Personal Details States
    var regDob by remember { mutableStateOf("") }
    var regAge by remember { mutableStateOf("") }
    var regFathersName by remember { mutableStateOf("") }
    var regMothersName by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regEmergencyContact by remember { mutableStateOf("") }
    var regDoj by remember { mutableStateOf("2026-07-01") }
    var regBloodGroup by remember { mutableStateOf("O+") }

    var showCalendarForField by remember { mutableStateOf<String?>(null) }

    // OTP verification flow
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var isRequestingOtp by remember { mutableStateOf(false) }
    var enteredRegOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }

    val departments = listOf(
        "Article",
        "Partner",
        "Staff"
    )

    val context = LocalContext.current

    ResponsiveLayout.ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 520.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button & Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isVerifyingOtp) {
                        isVerifyingOtp = false
                        otpError = ""
                    } else {
                        loginError = ""
                        regError = ""
                        otpError = ""
                        onBack()
                    }
                },
                modifier = Modifier.testTag("auth_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Logo Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Firm Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GUPTA LAKHANI & ASSOCIATES",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Chartered Accountants • FirmSync Portal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isVerifyingOtp) {
            // ==========================================
            // OTP VERIFICATION FLOW
            // ==========================================
            Text(
                text = "Secure Two-Factor Authentication",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To safeguard our CA database, please enter the high-security verification PINs dispatched to your office address and registered email.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Registered Email OTP Field
            OutlinedTextField(
                value = enteredRegOtp,
                onValueChange = { if (it.length <= 6) enteredRegOtp = it },
                label = { Text("Email Verification PIN (6 digits)") },
                placeholder = { Text("Enter OTP received on email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("registered_otp_input")
            )

            if (otpError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = otpError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    otpError = ""
                    isRegistering = true
                    viewModel.registerNewEmployee(
                        name = regName,
                        email = regEmail,
                        department = regDept,
                        password = regPassword,
                        role = regRole,
                        customId = if (regCustomId.isBlank()) null else regCustomId.trim(),
                        otp = enteredRegOtp,
                        age = regAge.toIntOrNull() ?: calculateAge(regDob),
                        dob = regDob,
                        fathersName = regFathersName,
                        mothersName = regMothersName,
                        address = regAddress,
                        phone = regMobile,
                        emergencyContact = regEmergencyContact,
                        doj = regDoj,
                        bloodGroup = regBloodGroup,
                        onSuccess = { registeredEmp ->
                            isRegistering = false
                            val newProfile = DetailedProfile(
                                id = registeredEmp.id,
                                userId = registeredEmp.id,
                                age = regAge.toIntOrNull() ?: calculateAge(regDob),
                                dob = regDob,
                                fathersName = regFathersName,
                                mothersName = regMothersName,
                                address = regAddress,
                                email = regEmail,
                                phone = regMobile,
                                emergencyContact = regEmergencyContact,
                                doj = regDoj,
                                bloodGroup = regBloodGroup
                            )
                            viewModel.saveDetailedProfile(newProfile)

                            android.widget.Toast.makeText(
                                context,
                                "Sign up successful! Welcome, " + registeredEmp.name + ".",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        onError = { err ->
                            isRegistering = false
                            otpError = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("verify_otp_button"),
                enabled = !isRegistering
            ) {
                if (isRegistering) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registering...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify & Complete Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    scope.launch {
                        try {
                            com.example.api.ApiClient.authService.sendOtp(com.example.api.SendOtpRequest(email = regEmail))
                            android.widget.Toast.makeText(context, "OTP resent to $regEmail", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            otpError = "Failed to resend OTP: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Didn't receive OTP? Resend")
            }

        } else if (!isRegisterMode) {
            // ==========================================
            // LOGIN MODE
            // ==========================================
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Please verify your credentials to access your secure workspace profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User ID field
            OutlinedTextField(
                value = loginUserId,
                onValueChange = { loginUserId = it },
                label = { Text("Enter your registered email for login") },
                placeholder = { Text("e.g. employee@example.com") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("login_username_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Passcode field
            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Secure Passcode / PIN") },
                placeholder = { Text("Enter your password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Passcode Visibility"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("login_password_input")
            )

            if (loginError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TextButton(
                onClick = {
                    forgotEmail = loginUserId
                    forgotStep = 1
                    forgotError = ""
                    forgotSuccess = ""
                    showForgotPasswordDialog = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    loginError = ""
                    isLoggingIn = true
                    viewModel.loginUser(loginUserId.trim(), loginPassword) { error, user, requires2FA ->
                        isLoggingIn = false
                        if (requires2FA) {
                            show2FADialog = true
                        } else if (error != null) {
                            loginError = error
                        } else if (user != null) {
                            loginError = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                enabled = !isLoggingIn
            ) {
                if (isLoggingIn) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging in...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login to My Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link to Register
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an ID yet?", style = MaterialTheme.typography.bodyMedium)
                TextButton(
                    onClick = {
                        isRegisterMode = true
                        loginError = ""
                        regError = ""
                        otpError = ""
                    },
                    modifier = Modifier.testTag("goto_signup_button")
                ) {
                    Text("Register Now", fontWeight = FontWeight.Bold)
                }
            }

        } else {
            // ==========================================
            // REGISTER MODE (SIGNUP)
            // ==========================================
            Text(
                text = "Register New Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create your professional account in the Gupta Lakhani & Associates firm ledger.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Field
            OutlinedTextField(
                value = regName,
                onValueChange = { regName = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Shreya Gupta") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Custom User ID / Username Field
            OutlinedTextField(
                value = regCustomId,
                onValueChange = { input -> regCustomId = input.filter { it.isLetterOrDigit() || it == '-' || it == '_' } },
                label = { Text("Choose User ID / Username (Mandatory)") },
                placeholder = { Text("e.g. shreya_g") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_custom_id_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email Field
            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Personal Email Address") },
                placeholder = { Text("e.g. shreya@gmail.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mobile Field
            OutlinedTextField(
                value = regMobile,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 10) {
                        regMobile = filtered
                    }
                },
                label = { Text("Mobile Number (10 Digits) *") },
                placeholder = { Text("e.g. 9876543210") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Designation (replaces Role and Department)
            Text(
                text = "Account Designation *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                departments.forEach { dept ->
                    val isSelected = regDept == dept
                    Surface(
                        onClick = {
                            regDept = dept
                            regRole = if (dept == "Partner") "Manager" else "Employee"
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dept_chip_" + dept)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dept,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PERSONAL DETAILS INPUTS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Personal Details (Mandatory)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Date of Birth (DOB) Field (Read only text field, trigger calendar preview)
                    OutlinedTextField(
                        value = regDob,
                        onValueChange = {},
                        label = { Text("Date of Birth (DOB) *") },
                        placeholder = { Text("Select date via calendar") },
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showCalendarForField = "dob" }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Choose DOB", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "dob" }.testTag("reg_dob_input")
                    )

                    // Auto Calculated Age (Read Only)
                    OutlinedTextField(
                        value = if (regAge.isNotEmpty()) "$regAge Years" else "",
                        onValueChange = {},
                        label = { Text("Auto-Calculated Age") },
                        placeholder = { Text("Age will auto-calculate") },
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_age_input")
                    )

                    // Father's Name
                    OutlinedTextField(
                        value = regFathersName,
                        onValueChange = { regFathersName = it },
                        label = { Text("Father's Name *") },
                        placeholder = { Text("e.g. Anand Gupta") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_father_input")
                    )

                    // Mother's Name
                    OutlinedTextField(
                        value = regMothersName,
                        onValueChange = { regMothersName = it },
                        label = { Text("Mother's Name *") },
                        placeholder = { Text("e.g. Sunita Gupta") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_mother_input")
                    )

                    // Permanent Address
                    OutlinedTextField(
                        value = regAddress,
                        onValueChange = { regAddress = it },
                        label = { Text("Permanent Address *") },
                        placeholder = { Text("Enter your complete address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("reg_address_input")
                    )

                    // Emergency Contact (10 digits validation)
                    OutlinedTextField(
                        value = regEmergencyContact,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 10) {
                                regEmergencyContact = filtered
                            }
                        },
                        label = { Text("Emergency Contact (10 Digits) *") },
                        placeholder = { Text("e.g. 9876543211") },
                        leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("reg_emergency_input")
                    )

                    // Date of Joining (DOJ) Field (Read only text field, trigger calendar preview)
                    OutlinedTextField(
                        value = regDoj,
                        onValueChange = {},
                        label = { Text("Date of Joining (DOJ) *") },
                        placeholder = { Text("Select date via calendar") },
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showCalendarForField = "doj" }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Choose DOJ", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "doj" }.testTag("reg_doj_input")
                    )

                    // Blood Group Field
                    OutlinedTextField(
                        value = regBloodGroup,
                        onValueChange = { regBloodGroup = it },
                        label = { Text("Blood Group *") },
                        placeholder = { Text("e.g. O+, A+, B+") },
                        leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_blood_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Passcode Field
            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Secure Login Passcode (e.g. 1234)") },
                placeholder = { Text("Choose a password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                        Icon(
                            imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Passcode Visibility"
                        )
                    }
                },
                visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
            )

            if (regError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = regError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (regName.isBlank() || regEmail.isBlank() || regMobile.isBlank() || regPassword.isBlank() || regCustomId.isBlank() ||
                        regDob.isBlank() || regFathersName.isBlank() || regMothersName.isBlank() || regAddress.isBlank() || regEmergencyContact.isBlank() || regBloodGroup.isBlank()) {
                        regError = "Please fill in all personal details and columns, including User ID / Username."
                    } else if (!regEmail.contains("@") or !regEmail.contains(".")) {
                        regError = "Please enter a valid personal email address."
                    } else if (regPassword.length < 4) {
                        regError = "Password must be at least 4 characters long."
                    } else if (regMobile.length != 10) {
                        regError = "Mobile Number must be exactly 10 digits."
                    } else if (regEmergencyContact.length != 10) {
                        regError = "Emergency Contact Number must be exactly 10 digits."
                    } else {
                        val trimmedCustomId = regCustomId.trim()
                        val alreadyExists = employees.any { it.id.equals(trimmedCustomId, ignoreCase = true) }
                        if (alreadyExists) {
                            regError = "This Username/User ID is already taken. Please choose another one."
                            return@Button
                        }
                        regError = ""
                        enteredRegOtp = ""
                        isRequestingOtp = true
                        viewModel.requestRegistrationOtp(regEmail) { success, msg ->
                            isRequestingOtp = false
                            if (success) {
                                isVerifyingOtp = true
                            } else {
                                regError = msg
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("request_otp_button"),
                enabled = !isRequestingOtp
            ) {
                if (isRequestingOtp) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending PIN...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.VpnKey, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link to Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have a verified ID?", style = MaterialTheme.typography.bodyMedium)
                TextButton(
                    onClick = {
                        isRegisterMode = false
                        regError = ""
                        loginError = ""
                        otpError = ""
                    },
                    modifier = Modifier.testTag("goto_login_button")
                ) {
                    Text("Login Instead", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCalendarForField != null) {
        val fieldName = showCalendarForField!!
        val currentVal = when (fieldName) {
            "dob" -> regDob
            "doj" -> regDoj
            else -> ""
        }
        CalendarDialog(
            initialDate = if (currentVal.isBlank()) {
                if (fieldName == "dob") "2000-01-01" else "2026-07-01"
            } else currentVal,
            format = "yyyy-MM-dd",
            onDismissRequest = { showCalendarForField = null },
            onDateSelected = { selectedDate ->
                if (fieldName == "dob") {
                    regDob = selectedDate
                    regAge = calculateAge(selectedDate).toString()
                } else if (fieldName == "doj") {
                    regDoj = selectedDate
                }
                showCalendarForField = null
            }
        )
    }
    if (showForgotPasswordDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (forgotStep == 1) {
                        Text("Enter your registered email to receive an OTP.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else if (forgotStep == 2) {
                        Text("Enter the OTP sent to your email and your new password.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = forgotOtp,
                            onValueChange = { forgotOtp = it },
                            label = { Text("OTP (6 digits)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isForgotNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isForgotNewPasswordVisible = !isForgotNewPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isForgotNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            }
                        )
                    }

                    if (forgotError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(forgotError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (forgotSuccess.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(forgotSuccess, color = Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = false }, enabled = !isForgotRequestingOtp && !isForgotVerifyingOtp) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                forgotError = ""
                                forgotSuccess = ""
                                if (forgotStep == 1) {
                                    if (forgotEmail.isBlank()) {
                                        forgotError = "Email is required."
                                        return@Button
                                    }
                                    isForgotRequestingOtp = true
                                    scope.launch {
                                        try {
                                            com.example.api.ApiClient.authService.sendOtp(
                                                com.example.api.SendOtpRequest(email = forgotEmail.trim())
                                            )
                                            forgotSuccess = "OTP sent successfully!"
                                            forgotStep = 2
                                        } catch (e: Exception) {
                                            forgotError = "Failed to send OTP. Please check email."
                                        } finally {
                                            isForgotRequestingOtp = false
                                        }
                                    }
                                } else {
                                    if (forgotOtp.isBlank() || forgotNewPassword.isBlank()) {
                                        forgotError = "OTP and New Password are required."
                                        return@Button
                                    }
                                    isForgotVerifyingOtp = true
                                    scope.launch {
                                        try {
                                            val response = com.example.api.ApiClient.authService.resetPassword(
                                                com.example.api.ResetPasswordRequest(
                                                    email = forgotEmail.trim(),
                                                    otp = forgotOtp.trim(),
                                                    newPassword = forgotNewPassword
                                                )
                                            )
                                            forgotSuccess = response.message
                                            kotlinx.coroutines.delay(1500)
                                            showForgotPasswordDialog = false
                                            forgotStep = 1
                                            forgotOtp = ""
                                            forgotNewPassword = ""
                                        } catch (e: Exception) {
                                            forgotError = "Failed to reset password: ${e.message}"
                                        } finally {
                                            isForgotVerifyingOtp = false
                                        }
                                    }
                                }
                            },
                            enabled = !isForgotRequestingOtp && !isForgotVerifyingOtp
                        ) {
                            if (isForgotRequestingOtp || isForgotVerifyingOtp) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (forgotStep == 1) "Send OTP" else "Reset Password")
                            }
                        }
                    }
                }
            }
        }
    }
}
    }
    
    // 2FA Verification Dialog
    if (show2FADialog) {
        AlertDialog(
            onDismissRequest = {
                show2FADialog = false
                login2FACode = ""
                login2FAError = ""
            },
            title = { Text("Two-Factor Authentication", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter the 6-digit code from your authenticator app.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = login2FACode,
                        onValueChange = { login2FACode = it },
                        label = { Text("Authentication Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (login2FAError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(login2FAError, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (login2FACode.isBlank()) {
                            login2FAError = "Code is required"
                            return@Button
                        }
                        isVerifying2FA = true
                        login2FAError = ""
                        scope.launch {
                            try {
                                val response = com.example.api.ApiClient.authService.verify2FALogin(
                                    com.example.api.Verify2FALoginRequest(
                                        email = loginUserId.trim(),
                                        password = loginPassword,
                                        token = login2FACode.trim()
                                    )
                                )
                                if (response.user != null) {
                                    viewModel.completeLogin(response.user)
                                    show2FADialog = false
                                }
                            } catch (e: retrofit2.HttpException) {
                                if (e.code() == 401) {
                                    login2FAError = "Invalid code. Please try again."
                                } else {
                                    login2FAError = "Failed to verify 2FA."
                                }
                            } catch (e: Exception) {
                                login2FAError = "Network error."
                            } finally {
                                isVerifying2FA = false
                            }
                        }
                    },
                    enabled = !isVerifying2FA
                ) {
                    if (isVerifying2FA) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        show2FADialog = false
                        login2FACode = ""
                        login2FAError = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun MainDashboardContainer(
    viewModel: OnSiteViewModel,
    currentUser: Employee,
    isManager: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = if (isManager) {
        listOf("Partner Ledger", "Attendance", "Group Discussion", "Talk to", "Task Manager", "Leave Requests", "My Profile")
    } else {
        listOf("On-Site Register", "Task Manager", "Talk to", "Group Discussion", "Leave Request", "My Profile")
    }

    ResponsiveLayout.ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 960.dp,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab views container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (isManager) {
                    when (selectedTab) {
                        0 -> ManagerLiveDashboard(viewModel)
                        1 -> EmployeeOnSiteScreen(viewModel, currentUser, onTabSelected)
                        2 -> PartnerAttendanceScreen(viewModel)
                        3 -> GroupDiscussionScreen(viewModel, currentUser, isManager = true)
                        4 -> TalkToScreen(viewModel, currentUser, isManager = true)
                        5 -> ManagerTasksDashboard(viewModel, currentUser)
                        6 -> LeaveRequestsManagerScreen(viewModel, currentUser)
                        7 -> MyProfileScreen(viewModel, currentUser)
                        else -> ManagerLiveDashboard(viewModel)
                    }
                } else {
                    when (selectedTab) {
                        0 -> EmployeeOnSiteScreen(viewModel, currentUser, onTabSelected)
                        1 -> EmployeeTasksScreen(viewModel, currentUser)
                        2 -> TalkToScreen(viewModel, currentUser, isManager = false)
                        3 -> GroupDiscussionScreen(viewModel, currentUser, isManager = false)
                        4 -> LeaveRequestEmployeeScreen(viewModel, currentUser)
                        5 -> MyProfileScreen(viewModel, currentUser)
                        else -> EmployeeOnSiteScreen(viewModel, currentUser, onTabSelected)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// EMPLOYEE COMPOSABLES
// -------------------------------------------------------------

data class GroupedShift(
    val dateString: String,
    val dateKey: String,
    val year: Int,
    val checkInLog: AttendanceLog?,
    val checkOutLog: AttendanceLog?,
    val hoursWorked: Double,
    val dateMillis: Long
)

fun groupLogsToShifts(logs: List<AttendanceLog>): List<GroupedShift> {
    val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val sdfDisplay = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
    val calendar = java.util.Calendar.getInstance()

    val logsByDay = logs.groupBy { log ->
        calendar.timeInMillis = log.timestamp
        sdfKey.format(calendar.time)
    }

    return logsByDay.map { (dateKey, dayLogs) ->
        val checkIn = dayLogs.filter { it.type == "Check-In" }.minByOrNull { it.timestamp }
        val checkOut = dayLogs.filter { it.type == "Check-Out" }.maxByOrNull { it.timestamp }

        calendar.timeInMillis = dayLogs.first().timestamp
        val year = calendar.get(java.util.Calendar.YEAR)
        val dateString = try {
            val date = sdfKey.parse(dateKey)
            if (date != null) sdfDisplay.format(date) else dateKey
        } catch (e: Exception) {
            dateKey
        }

        val hours = if (checkIn != null && checkOut != null && checkOut.timestamp > checkIn.timestamp) {
            (checkOut.timestamp - checkIn.timestamp).toDouble() / (1000.0 * 60.0 * 60.0)
        } else if (checkIn != null && checkOut == null) {
            val isToday = android.text.format.DateUtils.isToday(checkIn.timestamp)
            if (isToday) {
                val diff = System.currentTimeMillis() - checkIn.timestamp
                if (diff > 0) diff.toDouble() / (1000.0 * 60.0 * 60.0) else 0.0
            } else {
                0.0
            }
        } else {
            0.0
        }

        GroupedShift(
            dateString = dateString,
            dateKey = dateKey,
            year = year,
            checkInLog = checkIn,
            checkOutLog = checkOut,
            hoursWorked = hours,
            dateMillis = checkIn?.timestamp ?: dayLogs.first().timestamp
        )
    }.sortedByDescending { it.dateMillis }
}

@Composable
fun EmployeeOnSiteScreen(
    viewModel: OnSiteViewModel,
    employee: Employee,
    onTabSelected: (Int) -> Unit = {}
) {
    val simulatedLocation by viewModel.currentSimulatedLocation.collectAsStateWithLifecycle()
    val geofenceLat by viewModel.geofenceLat.collectAsStateWithLifecycle()
    val geofenceLng by viewModel.geofenceLng.collectAsStateWithLifecycle()
    val geofenceRadius by viewModel.geofenceRadius.collectAsStateWithLifecycle()
    val checkInLogs by viewModel.currentEmployeeLogs.collectAsStateWithLifecycle()

    var showTasksPopup by remember { mutableStateOf(true) }
    val employeeTasks by viewModel.currentEmployeeTodoItems.collectAsStateWithLifecycle()
    val companyAlerts by viewModel.systemAlerts.collectAsStateWithLifecycle()

    val pendingCaTasks = employeeTasks.filter { !it.isPersonal && it.status == "Incomplete" }
    
    val context = LocalContext.current
    val prefs = remember(employee.id) { context.getSharedPreferences("onsite_prefs", android.content.Context.MODE_PRIVATE) }
    
    var realLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    // Check if permission is already granted
    val fineGranted = remember(context) {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    val coarseGranted = remember(context) {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineG = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseG = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        locationPermissionGranted = fineG || coarseG
    }

    LaunchedEffect(Unit) {
        if (fineGranted || coarseGranted) {
            locationPermissionGranted = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        realLocation = loc
                    }
                }

                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    5000L
                ).build()

                val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(p0: com.google.android.gms.location.LocationResult) {
                        p0.lastLocation?.let {
                            realLocation = it
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // Ignore security exceptions gracefully
            }
        }
    }

    val unacknowledgedUrgentAlerts = companyAlerts.filter { alert ->
        alert.priority == "Urgent" && !prefs.getBoolean("acknowledged_alert_${employee.id}_${alert.id}", false)
    }

    // 1. Separate Task Manager Popup
    if (showTasksPopup && pendingCaTasks.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showTasksPopup = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Notification Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Directive Alert: Pending Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📌 Tasks Assigned to You:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pendingCaTasks.forEach { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = when (task.priority) {
                                                "Urgent" -> Color(0xFFFFEBEE)
                                                "High" -> Color(0xFFFFF3E0)
                                                else -> Color(0xFFF5F5F5)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = task.priority,
                                                color = when (task.priority) {
                                                    "Urgent" -> Color(0xFFC62828)
                                                    "High" -> Color(0xFFEF6C00)
                                                    else -> Color(0xFF616161)
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (task.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTasksPopup = false
                        onTabSelected(if (employee.role == "Manager") 5 else 1) // Navigate to Task Manager tab
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Go to Task Manager", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTasksPopup = false }
                ) {
                    Text("Acknowledge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("ca_directive_popup")
        )
    }

    // 2. Separate Urgent Group Discussion Popup (only for urgent messages, can be dismissed persistently)
    if (unacknowledgedUrgentAlerts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                unacknowledgedUrgentAlerts.forEach { alert ->
                    prefs.edit().putBoolean("acknowledged_alert_${employee.id}_${alert.id}", true).apply()
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Urgent Notification Icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Urgent Group Discussion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📢 Urgent Broadcasts:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        unacknowledgedUrgentAlerts.forEach { alert ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alert.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Posted by ${alert.senderName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        unacknowledgedUrgentAlerts.forEach { alert ->
                            prefs.edit().putBoolean("acknowledged_alert_${employee.id}_${alert.id}", true).apply()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Acknowledge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        unacknowledgedUrgentAlerts.forEach { alert ->
                            prefs.edit().putBoolean("acknowledged_alert_${employee.id}_${alert.id}", true).apply()
                        }
                    }
                ) {
                    Text("Dismiss", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("group_discussion_urgent_popup")
        )
    }

    var selectedLogFilter by remember { mutableStateOf(0) } // 0 = Today, 1 = Previous Shifts
    var selectedYear by remember { mutableStateOf(2026) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    val todayStartMillis = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    val groupedShifts = groupLogsToShifts(checkInLogs)
    val filteredGroupedShifts = groupedShifts.filter { shift ->
        if (selectedLogFilter == 0) {
            shift.dateMillis >= todayStartMillis
        } else {
            shift.dateMillis < todayStartMillis && shift.year == selectedYear
        }
    }

    val activeLat = realLocation?.latitude ?: simulatedLocation.latitude
    val activeLng = realLocation?.longitude ?: simulatedLocation.longitude

    val distance = viewModel.calculateDistanceFromGeofence(activeLat, activeLng)
    val isWithinRange = distance <= geofenceRadius

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // BIRTHDAY REMINDERS & CELEBRATIONS
        BirthdayCelebrationsSection(viewModel)

        val isWideLayout = ResponsiveLayout.isExpandedWidth()

        ResponsiveLayout.AdaptiveRowOrColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // GEOFENCING RADAR VISUAL COMPONENT
            Card(
                modifier = if (isWideLayout) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GEOFENCED COMPLIANCE RADAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Simulated radar circle with radial effect
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isWithinRange)
                                        listOf(Color(0xFF81C784).copy(alpha = 0.3f), Color(0xFF4CAF50).copy(alpha = 0.05f))
                                    else
                                        listOf(Color(0xFFE57373).copy(alpha = 0.3f), Color(0xFFF44336).copy(alpha = 0.05f))
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (isWithinRange) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isWithinRange) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                contentDescription = "Radar Symbol",
                                tint = if (isWithinRange) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = if (isWithinRange) "ON-SITE" else "OUT-OF-BOUNDS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isWithinRange) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Office Center Geofence: $geofenceLat, $geofenceLng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Max Check-In Range: within ${geofenceRadius.toInt()} meters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (realLocation != null) "📡 Source: Live Device GPS Location" else "⚙️ Source: Simulated Location (GPS Loading/Permission Required)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (realLocation != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (realLocation != null) {
                        Text(
                            text = "GPS Coordinates: ${String.format(Locale.US, "%.5f, %.5f", realLocation!!.latitude, realLocation!!.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = "Your current distance: ${String.format(Locale.US, "%.1f", distance)} meters",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isWithinRange) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // CHECK-IN & CHECK-OUT ACTION BUTTONS WITH GEOFENCE ENFORCEMENT
            Column(
                modifier = if (isWideLayout) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.executeAttendanceAction("Check-In", realLocation?.latitude, realLocation?.longitude) },
                        enabled = isWithinRange,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("check_in_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Check-In Sign Icon",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Check In", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.executeAttendanceAction("Check-Out", realLocation?.latitude, realLocation?.longitude) },
                        enabled = isWithinRange,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("check_out_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9E9E9E),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Check-Out Sign Icon",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Check Out", fontSize = 14.sp)
                    }
                }
                if (!isWithinRange) {
                    Text(
                        text = "⚠️ Check-In/Out Disabled: You must be physically On-Site to log your shift.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("geofence_disabled_alert")
                    )
                }
            }
        }

        // LOG HISTORY LIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedLogFilter == 0) "Today's Shifts" else "Previous Shifts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedLogFilter == 1) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .clickable { yearDropdownExpanded = true }
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$selectedYear",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Year",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            listOf(2026, 2025, 2024).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("$year", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        selectedYear = year
                                        yearDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("year_option_$year")
                                )
                            }
                        }
                    }
                }

                listOf("Today", "Previous").forEachIndexed { index, label ->
                    val isSelected = selectedLogFilter == index
                    Surface(
                        modifier = Modifier
                            .clickable { selectedLogFilter = index }
                            .padding(vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (filteredGroupedShifts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedLogFilter == 0) "No shifts logged yet today. Tap Check In above." else "No previous shifts logged for $selectedYear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGroupedShifts) { shift ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shift_item_${shift.dateKey}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left side: Icon and Shift details
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Dynamic Shift Status Icon
                                val hasBoth = shift.checkInLog != null && shift.checkOutLog != null
                                val isToday = android.text.format.DateUtils.isToday(shift.dateMillis)
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = when {
                                        hasBoth -> Color(0xFFE8F5E9)
                                        isToday && shift.checkInLog != null -> Color(0xFFE3F2FD)
                                        else -> Color(0xFFFFF3E0)
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when {
                                                hasBoth -> Icons.Default.CheckCircle
                                                isToday && shift.checkInLog != null -> Icons.Default.PlayArrow
                                                else -> Icons.Default.History
                                            },
                                            contentDescription = "Shift status icon",
                                            tint = when {
                                                hasBoth -> Color(0xFF4CAF50)
                                                isToday && shift.checkInLog != null -> Color(0xFF2196F3)
                                                else -> Color(0xFFFF9800)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = shift.dateString,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    // Row with Check-in and Check-out timings
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (shift.checkInLog != null) "In: ${formatTime(shift.checkInLog.timestamp)}" else "In: --:--",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        Text(
                                            text = if (shift.checkOutLog != null) "Out: ${formatTime(shift.checkOutLog.timestamp)}" else {
                                                if (isToday && shift.checkInLog != null) "Active" else "Out: --:--"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isToday && shift.checkInLog != null && shift.checkOutLog == null) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (isToday && shift.checkInLog != null && shift.checkOutLog == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Right side: Hours worked and sync indicators beside it!
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f hrs", shift.hoursWorked),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Worked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Sync status icon
                                val isSynced = (shift.checkInLog?.isSynced != false) && (shift.checkOutLog?.isSynced != false)
                                Icon(
                                    imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                    contentDescription = "Sync State Icon",
                                    tint = if (isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeTasksScreen(viewModel: OnSiteViewModel, currentUser: Employee) {
    val tasks by viewModel.currentEmployeeTodoItems.collectAsStateWithLifecycle()
    
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Assigned Tasks, 1 = My Personal To-Dos
    var showAddTaskDialog by remember { mutableStateOf(false) } // For personal to-do list
    
    val assignedTasks = tasks.filter { !it.isPersonal }
    val personalTasks = tasks.filter { it.isPersonal }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        // Sub Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Assigned Tasks", "My Daily To-Do List").forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Button(
                    onClick = { selectedSubTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (selectedSubTab == 0) {
            // ASSIGNED AUDIT TASKS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Professional Task Manager",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${assignedTasks.count { it.status == "Complete" }}/${assignedTasks.size} Done",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (assignedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No professional tasks assigned by CAs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(assignedTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (task.status) {
                                    "Complete" -> Color(0xFFE8F5E9)
                                    "Have a Doubt" -> Color(0xFFFFF3E0)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                when (task.status) {
                                    "Complete" -> Color(0xFF81C784)
                                    "Have a Doubt" -> Color(0xFFFFB74D)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        if (task.assignedBy.isNotBlank()) {
                                            Text(
                                                text = "Assigned by: ${task.assignedBy}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    // Priority Badge
                                    Surface(
                                        color = when (task.priority) {
                                            "High" -> Color(0xFFFFEBEE)
                                            "Medium" -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFE8F5E9)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = task.priority,
                                            color = when (task.priority) {
                                                "High" -> Color(0xFFC62828)
                                                "Medium" -> Color(0xFFEF6C00)
                                                else -> Color(0xFF2E7D32)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                
                                // Status & Marking Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Badge
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (task.status) {
                                                "Complete" -> if (task.isApproved) Icons.Default.VerifiedUser else Icons.Default.CheckCircle
                                                "Have a Doubt" -> Icons.Default.Help
                                                else -> Icons.Default.Pending
                                            },
                                            contentDescription = "Status Icon",
                                            tint = when (task.status) {
                                                "Complete" -> Color(0xFF4CAF50)
                                                "Have a Doubt" -> Color(0xFFFF9800)
                                                else -> Color(0xFF9E9E9E)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when {
                                                task.status == "Complete" && task.isApproved -> "Approved by CA"
                                                task.status == "Complete" -> "Completed (Pending Review)"
                                                task.status == "Have a Doubt" -> "Have a Doubt"
                                                else -> "Incomplete"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when (task.status) {
                                                "Complete" -> Color(0xFF2E7D32)
                                                "Have a Doubt" -> Color(0xFFD84315)
                                                else -> Color(0xFF616161)
                                            }
                                        )
                                    }
                                    
                                    // Actions Flow
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // "Incomplete" Action
                                        if (task.status != "Incomplete") {
                                            IconButton(
                                                onClick = { viewModel.updateTaskStatus(task, "Incomplete") },
                                                modifier = Modifier.size(28.dp).background(Color(0xFFF5F5F5), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Mark Incomplete", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        // "Have a Doubt" Action
                                        if (task.status != "Have a Doubt") {
                                            IconButton(
                                                onClick = { viewModel.updateTaskStatus(task, "Have a Doubt") },
                                                modifier = Modifier.size(28.dp).background(Color(0xFFFFF3E0), CircleShape)
                                            ) {
                                                Icon(Icons.Default.HelpOutline, contentDescription = "Mark Doubt", tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        // "Complete" Action
                                        if (task.status != "Complete") {
                                            IconButton(
                                                onClick = { viewModel.updateTaskStatus(task, "Complete") },
                                                modifier = Modifier.size(28.dp).background(Color(0xFFE8F5E9), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Mark Complete", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MY PERSONAL TO-DO LIST FOR DAY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Personal Tasks for Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Personal Task", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (personalTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No personal to-do tasks added for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(personalTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTaskCompletion(task) }
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (task.description.isNotBlank()) {
                                            Text(
                                                text = task.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when (task.priority) {
                                            "High" -> Color(0xFFFFEBEE)
                                            "Medium" -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFE8F5E9)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = task.priority,
                                            color = when (task.priority) {
                                                "High" -> Color(0xFFC62828)
                                                "Medium" -> Color(0xFFEF6C00)
                                                else -> Color(0xFF2E7D32)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteTask(task.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Personal Task", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Task Dialog Component for Personal List
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var selectedPriority by remember { mutableStateOf("Medium") }
        
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Personal Daily Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    TextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Priority Level:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("High", "Medium", "Low").forEach { level ->
                            OutlinedButton(
                                onClick = { selectedPriority = level },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedPriority == level)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                            ) {
                                Text(level, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addPersonalTodo(taskTitle, taskDesc, selectedPriority)
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Save Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TalkToScreen(viewModel: OnSiteViewModel, currentUser: Employee, isManager: Boolean) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeChatUser by remember { mutableStateOf<Employee?>(null) }

    if (activeChatUser == null) {
        // Contact List / User Selector
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "Talk to Partners & Staff",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isManager) "Summon staff instantly or start a secure direct chat" else "Chat with corporate partners securely",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val targetUsers = remember(employees, isManager, currentUser) {
                if (isManager) {
                    // Managers see all employees (non-managers)
                    employees.filter { it.role != "Manager" && it.id != currentUser.id }
                } else {
                    // Employees see all partners (managers)
                    employees.filter { it.role == "Manager" && it.id != currentUser.id }
                }
            }

            if (targetUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No other firm members registered yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(targetUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = if (user.role == "Manager") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = user.name.take(2).uppercase(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.role == "Manager") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${user.role} • ${user.department}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isManager) {
                                        Button(
                                            onClick = {
                                                viewModel.sendSummon(user.id)
                                                android.widget.Toast.makeText(context, "Summon issued to ${user.name}!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Summon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { activeChatUser = user },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Active Chat Screen with the selected user
        val otherUser = activeChatUser!!

        var messageContent by remember { mutableStateOf("") }
        var selectedAttachmentType by remember { mutableStateOf<String?>(null) }
        var selectedAttachmentData by remember { mutableStateOf<String?>(null) }
        var selectedAttachmentName by remember { mutableStateOf<String?>(null) }
        var selectedVoiceDuration by remember { mutableStateOf(0) }

        var zoomPhotoData by remember { mutableStateOf<String?>(null) }
        var openDocName by remember { mutableStateOf<String?>(null) }
        var showVoiceRecorder by remember { mutableStateOf(false) }

        val directMessages = remember(messages, currentUser, otherUser) {
            messages.filter {
                (it.senderId == currentUser.id && it.recipientId == otherUser.id) ||
                (it.senderId == otherUser.id && it.recipientId == currentUser.id)
            }.sortedBy { it.timestamp }
        }

        // Attachment pickers
        val photoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    if (bytes != null) {
                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                        selectedAttachmentType = "photo"
                        selectedAttachmentData = base64
                        
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val name = if (nameIndex != null && cursor.moveToFirst()) {
                            cursor.getString(nameIndex)
                        } else {
                            "Photo_${System.currentTimeMillis()}.jpg"
                        }
                        cursor?.close()
                        selectedAttachmentName = name
                        android.widget.Toast.makeText(context, "Photo attached successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Failed to attach photo", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        val documentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    val sizeStr = if (bytes != null) "${bytes.size / 1024} KB" else "Unknown Size"
                    selectedAttachmentType = "document"
                    selectedAttachmentData = uri.toString()
                    
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val name = if (nameIndex != null && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else {
                        "Document_${System.currentTimeMillis()}.pdf"
                    }
                    cursor?.close()
                    selectedAttachmentName = "$name ($sizeStr)"
                    android.widget.Toast.makeText(context, "Document attached successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Failed to attach document", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chat Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { activeChatUser = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back to contacts list")
                        }

                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = otherUser.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = otherUser.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${otherUser.role} • ${otherUser.department}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isManager) {
                        Button(
                            onClick = {
                                viewModel.sendSummon(otherUser.id)
                                android.widget.Toast.makeText(context, "Summon issued to ${otherUser.name}!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Summon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Timeline list
            if (directMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "No messages yet. Send a secure greeting!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(directMessages) { msg ->
                        val isSelf = msg.senderId == currentUser.id
                        val alignment = if (isSelf) Alignment.End else Alignment.Start
                        val cardBg = if (isSelf) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        val borderStroke = if (isSelf) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .padding(horizontal = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = borderStroke,
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isSelf) 12.dp else 0.dp,
                                    bottomEnd = if (isSelf) 0.dp else 12.dp
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Header: sender and time
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isSelf) "You" else msg.senderName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = formatTime(msg.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }

                                    // Decrypted text
                                    if (msg.content.isNotEmpty()) {
                                        val decryptedText = remember(msg.content) {
                                            viewModel.decryptMessage(msg.content)
                                        }
                                        Text(
                                            text = decryptedText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Attachment render
                                    if (msg.attachmentType != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        when (msg.attachmentType) {
                                            "photo" -> {
                                                val bitmap = remember(msg.attachmentData) {
                                                    try {
                                                        val decodedString = android.util.Base64.decode(msg.attachmentData, android.util.Base64.DEFAULT)
                                                        android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                }

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(140.dp)
                                                        .clickable { msg.attachmentData?.let { zoomPhotoData = it } },
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        if (bitmap != null) {
                                                            Image(
                                                                bitmap = bitmap.asImageBitmap(),
                                                                contentDescription = "Attached Photo",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        } else {
                                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.Gray)
                                                                    Text("Loading Error", style = MaterialTheme.typography.bodySmall)
                                                                }
                                                            }
                                                        }

                                                        Surface(
                                                            color = Color.Black.copy(alpha = 0.6f),
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .padding(4.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                            ) {
                                                                Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.White)
                                                                Text("Preview", color = Color.White, fontSize = 9.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            "document" -> {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { msg.attachmentName?.let { openDocName = it } },
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                                            Column {
                                                                Text(msg.attachmentName ?: "Attached Doc", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                Text("Tap to open doc", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
                                                            }
                                                        }
                                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }

                                            "voice" -> {
                                                var isPlaying by remember { mutableStateOf(false) }
                                                var currentSeconds by remember { mutableStateOf(0) }
                                                var progress by remember { mutableStateOf(0.0f) }

                                                LaunchedEffect(isPlaying) {
                                                    if (isPlaying) {
                                                        val durationSeconds = if (msg.voiceDuration > 0) msg.voiceDuration else 5
                                                        val intervalMs = 100
                                                        val totalSteps = durationSeconds * 10
                                                        var currentStep = (progress * totalSteps).toInt()

                                                        while (currentStep < totalSteps && isPlaying) {
                                                            kotlinx.coroutines.delay(intervalMs.toLong())
                                                            currentStep++
                                                            progress = currentStep.toFloat() / totalSteps.toFloat()
                                                            currentSeconds = currentStep / 10
                                                        }
                                                        if (currentStep >= totalSteps) {
                                                            isPlaying = false
                                                            progress = 0.0f
                                                            currentSeconds = 0
                                                        }
                                                    }
                                                }

                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = { isPlaying = !isPlaying },
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(16.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                val barHeights = listOf(4, 10, 16, 8, 4, 12, 18, 12, 6, 10, 14, 10, 6, 12, 16, 12, 6, 4, 10, 14, 10, 6, 4)
                                                                barHeights.forEachIndexed { i, height ->
                                                                    val isActive = progress >= (i.toFloat() / barHeights.size.toFloat())
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .height(height.dp)
                                                                            .background(
                                                                                if (isActive) MaterialTheme.colorScheme.primary
                                                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                                                                RoundedCornerShape(1.dp)
                                                                            )
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(2.dp))

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(String.format("0:%02d", currentSeconds), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                                Text(String.format("0:%02d", msg.voiceDuration), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            // Composer with attachments indicator
            if (selectedAttachmentType != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (selectedAttachmentType) {
                                    "voice" -> Icons.Default.Mic
                                    "photo" -> Icons.Default.PhotoLibrary
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Attached ${selectedAttachmentType?.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(selectedAttachmentName ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(onClick = {
                            selectedAttachmentType = null
                            selectedAttachmentData = null
                            selectedAttachmentName = null
                            selectedVoiceDuration = 0
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove attachment")
                        }
                    }
                }
            }

            // Input actions row and TextField
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Attachments buttons
                IconButton(onClick = { showVoiceRecorder = true }) {
                    Icon(Icons.Default.Mic, contentDescription = "Attach Voice Note", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = { photoLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Attach Photo", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = { documentLauncher.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach Document", tint = MaterialTheme.colorScheme.primary)
                }

                TextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    placeholder = { Text("Write encrypted message...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                IconButton(
                    onClick = {
                        if (messageContent.isNotBlank() || selectedAttachmentType != null) {
                            viewModel.sendDirectMessage(
                                recipientId = otherUser.id,
                                content = messageContent,
                                attachmentType = selectedAttachmentType,
                                attachmentData = selectedAttachmentData,
                                attachmentName = selectedAttachmentName,
                                voiceDuration = selectedVoiceDuration
                            )

                            // Clear inputs
                            messageContent = ""
                            selectedAttachmentType = null
                            selectedAttachmentData = null
                            selectedAttachmentName = null
                            selectedVoiceDuration = 0
                            android.widget.Toast.makeText(context, "Encrypted message sent!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    enabled = messageContent.isNotBlank() || selectedAttachmentType != null
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Preview dialogs (reused)
        if (zoomPhotoData != null) {
            AlertDialog(
                onDismissRequest = { zoomPhotoData = null },
                title = { Text("Photo Preview", fontWeight = FontWeight.Bold) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(zoomPhotoData) {
                            try {
                                val decodedString = android.util.Base64.decode(zoomPhotoData, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Zoomed photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.BrokenImage, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                Text("Error loading photo preview")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { zoomPhotoData = null }) {
                        Text("Close")
                    }
                }
            )
        }

        if (openDocName != null) {
            AlertDialog(
                onDismissRequest = { openDocName = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Document Opened", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(openDocName ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "This simulates viewing the attached corporate bulletin, audit sheet PDF, or ledger statements in secure HD.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF2E7D32))
                                Text("Offline Secure Vault: Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(onClick = {
                            android.widget.Toast.makeText(context, "Saved to Device Downloads!", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Download")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { openDocName = null }) {
                            Text("Close")
                        }
                    }
                }
            )
        }

        if (showVoiceRecorder) {
            VoiceRecorderSimDialog(
                onDismiss = { showVoiceRecorder = false },
                onRecordingComplete = { name, duration, data ->
                    selectedAttachmentType = "voice"
                    selectedAttachmentName = name
                    selectedVoiceDuration = duration
                    selectedAttachmentData = data
                    showVoiceRecorder = false
                }
            )
        }
    }
}

@Composable
fun GroupDiscussionScreen(viewModel: OnSiteViewModel, currentUser: Employee, isManager: Boolean) {
    val alerts by viewModel.systemAlerts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var messageTitle by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    // Attachment state
    var selectedAttachmentType by remember { mutableStateOf<String?>(null) } // "voice", "photo", "document"
    var selectedAttachmentData by remember { mutableStateOf<String?>(null) }
    var selectedAttachmentName by remember { mutableStateOf<String?>(null) }
    var selectedVoiceDuration by remember { mutableStateOf(0) }

    // Dialogs
    var zoomPhotoData by remember { mutableStateOf<String?>(null) }
    var openDocName by remember { mutableStateOf<String?>(null) }
    var showVoiceRecorder by remember { mutableStateOf(false) }

    // Image Picker
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    selectedAttachmentType = "photo"
                    selectedAttachmentData = base64
                    
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val name = if (nameIndex != null && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else {
                        "Photo_${System.currentTimeMillis()}.jpg"
                    }
                    cursor?.close()
                    selectedAttachmentName = name
                    android.widget.Toast.makeText(context, "Photo attached successfully!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to attach photo", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Document Picker
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                val sizeStr = if (bytes != null) "${bytes.size / 1024} KB" else "Unknown Size"
                selectedAttachmentType = "document"
                selectedAttachmentData = uri.toString()
                
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val name = if (nameIndex != null && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    "Document_${System.currentTimeMillis()}.pdf"
                }
                cursor?.close()
                selectedAttachmentName = "$name ($sizeStr)"
                android.widget.Toast.makeText(context, "Document attached successfully!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to attach document", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Group Discussion Board",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Share text, voice notes, photos & documents with all members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Post Message Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEW DISCUSSION POST",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextField(
                    value = messageTitle,
                    onValueChange = { messageTitle = it },
                    label = { Text("Topic / Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    label = { Text("Write your message here...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Current attachment indicator
                if (selectedAttachmentType != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (selectedAttachmentType) {
                                        "voice" -> Icons.Default.Mic
                                        "photo" -> Icons.Default.PhotoLibrary
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Attached ${selectedAttachmentType?.replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = selectedAttachmentName ?: "File attached",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = {
                                selectedAttachmentType = null
                                selectedAttachmentData = null
                                selectedAttachmentName = null
                                selectedVoiceDuration = 0
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove attachment")
                            }
                        }
                    }
                }

                // Toolbar Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Voice Note
                        IconButton(onClick = { showVoiceRecorder = true }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice Note",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Photo Gallery
                        IconButton(onClick = { photoLauncher.launch("image/*") }) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Documents
                        IconButton(onClick = { documentLauncher.launch("*/*") }) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Add Document",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isManager) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isUrgent,
                                    onCheckedChange = { isUrgent = it }
                                )
                                Text("Urgent Broadcast", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (messageContent.isNotBlank() || selectedAttachmentType != null) {
                                    val finalTitle = if (messageTitle.isBlank()) {
                                        when (selectedAttachmentType) {
                                            "voice" -> "Voice Note Post"
                                            "photo" -> "Photo Share"
                                            "document" -> "Document Share"
                                            else -> "General Update"
                                        }
                                    } else messageTitle

                                    viewModel.postDiscussionMessage(
                                        title = finalTitle,
                                        content = messageContent,
                                        priority = if (isUrgent) "Urgent" else "Info",
                                        attachmentType = selectedAttachmentType,
                                        attachmentData = selectedAttachmentData,
                                        attachmentName = selectedAttachmentName,
                                        voiceDuration = selectedVoiceDuration
                                    )

                                    // Reset inputs
                                    messageTitle = ""
                                    messageContent = ""
                                    isUrgent = false
                                    selectedAttachmentType = null
                                    selectedAttachmentData = null
                                    selectedAttachmentName = null
                                    selectedVoiceDuration = 0
                                    android.widget.Toast.makeText(context, "Message posted to group!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Please enter message text or add an attachment", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = messageContent.isNotBlank() || selectedAttachmentType != null
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post")
                        }
                    }
                }
            }
        }

        // Discussion Messages Feed List
        Text(
            text = "Discussion Thread",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No messages posted in this group yet. Be the first!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val sortedAlerts = alerts.sortedByDescending { it.timestamp }
                items(sortedAlerts) { alert ->
                    DiscussionMessageCard(
                        alert = alert,
                        onPhotoClick = { zoomPhotoData = it },
                        onDocClick = { openDocName = it }
                    )
                }
            }
        }
    }

    // Zoom Photo Dialog
    if (zoomPhotoData != null) {
        AlertDialog(
            onDismissRequest = { zoomPhotoData = null },
            title = { Text("Photo Preview", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(zoomPhotoData) {
                        try {
                            val decodedString = android.util.Base64.decode(zoomPhotoData, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Zoomed photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BrokenImage, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Text("Error loading photo preview")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { zoomPhotoData = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Open Document Simulation Dialog
    if (openDocName != null) {
        AlertDialog(
            onDismissRequest = { openDocName = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Document Opened", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = openDocName ?: "",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "This simulates viewing the attached corporate bulletin, checklist PDF, or ledger statement in high definition. All safety policies are verified and cryptographically secured.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF2E7D32))
                            Text("Offline Safe Vault: Document Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        android.widget.Toast.makeText(context, "Saved to Device Downloads folder!", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Download File")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { openDocName = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Voice Recorder Simulation Dialog
    if (showVoiceRecorder) {
        VoiceRecorderSimDialog(
            onDismiss = { showVoiceRecorder = false },
            onRecordingComplete = { name, duration, data ->
                selectedAttachmentType = "voice"
                selectedAttachmentName = name
                selectedVoiceDuration = duration
                selectedAttachmentData = data
                showVoiceRecorder = false
            }
        )
    }
}

@Composable
fun DiscussionMessageCard(
    alert: SystemAlert,
    onPhotoClick: (String) -> Unit,
    onDocClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.priority == "Urgent")
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (alert.priority == "Urgent")
                MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Avatar, Name, Time, Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = alert.senderName.take(2).uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            text = alert.senderName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(alert.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (alert.priority == "Urgent") {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "URGENT",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Message Title & Content
            if (alert.title.isNotEmpty() && alert.title != "Voice Note Post" && alert.title != "Photo Share" && alert.title != "Document Share") {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alert.priority == "Urgent") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            if (alert.content.isNotEmpty()) {
                Text(
                    text = alert.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Render Attachments
            if (alert.attachmentType != null) {
                Spacer(modifier = Modifier.height(4.dp))
                when (alert.attachmentType) {
                    "photo" -> {
                        val bitmap = remember(alert.attachmentData) {
                            try {
                                val decodedString = android.util.Base64.decode(alert.attachmentData, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable { alert.attachmentData?.let { onPhotoClick(it) } },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Attached photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.Gray)
                                            Text("Photo loading error", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Text("Preview Photo", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    "document" -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { alert.attachmentName?.let { onDocClick(it) } },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = alert.attachmentName ?: "Attached Document",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Tap to open/download secured document",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open file", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    "voice" -> {
                        var isPlaying by remember { mutableStateOf(false) }
                        var currentSeconds by remember { mutableStateOf(0) }
                        var progress by remember { mutableStateOf(0.0f) }

                        LaunchedEffect(isPlaying) {
                            if (isPlaying) {
                                val durationSeconds = if (alert.voiceDuration > 0) alert.voiceDuration else 5
                                val intervalMs = 100
                                val totalSteps = durationSeconds * 10
                                var currentStep = (progress * totalSteps).toInt()

                                while (currentStep < totalSteps && isPlaying) {
                                    kotlinx.coroutines.delay(intervalMs.toLong())
                                    currentStep++
                                    progress = currentStep.toFloat() / totalSteps.toFloat()
                                    currentSeconds = currentStep / 10
                                }
                                if (currentStep >= totalSteps) {
                                    isPlaying = false
                                    progress = 0.0f
                                    currentSeconds = 0
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause voice note" else "Play voice note",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val barHeights = listOf(4, 12, 18, 10, 6, 14, 22, 16, 8, 12, 20, 14, 8, 16, 24, 18, 10, 6, 12, 18, 14, 8, 4, 10, 16, 12, 6, 12, 22, 14, 8, 4)
                                        barHeights.forEachIndexed { i, height ->
                                            val isActive = progress >= (i.toFloat() / barHeights.size.toFloat())
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(height.dp)
                                                    .background(
                                                        if (isActive) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                                        RoundedCornerShape(1.dp)
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = String.format("0:%02d", currentSeconds),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format("0:%02d", alert.voiceDuration),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceRecorderSimDialog(
    onDismiss: () -> Unit,
    onRecordingComplete: (String, Int, String) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingFinished by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableStateOf(0) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var previewProgress by remember { mutableStateOf(0.0f) }
    var previewSeconds by remember { mutableStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                secondsElapsed++
                if (secondsElapsed >= 30) {
                    isRecording = false
                    recordingFinished = true
                    break
                }
            }
        }
    }

    LaunchedEffect(isPlayingPreview) {
        if (isPlayingPreview) {
            val totalSteps = secondsElapsed * 10
            var step = (previewProgress * totalSteps).toInt()
            while (step < totalSteps && isPlayingPreview) {
                kotlinx.coroutines.delay(100L)
                step++
                previewProgress = step.toFloat() / totalSteps.toFloat()
                previewSeconds = step / 10
            }
            if (step >= totalSteps) {
                isPlayingPreview = false
                previewProgress = 0.0f
                previewSeconds = 0
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Voice Message", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!recordingFinished) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            var isDotVisible by remember { mutableStateOf(true) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    kotlinx.coroutines.delay(500L)
                                    isDotVisible = !isDotVisible
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isDotVisible) Color.Red else Color.Transparent,
                                        CircleShape
                                    )
                            )
                            Text("Recording...", fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                    } else {
                        Text("Ready to Record", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = String.format("0:%02d", secondsElapsed),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = {
                                    isRecording = true
                                    secondsElapsed = 0
                                }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Recording")
                            }
                        } else {
                            Button(
                                onClick = {
                                    isRecording = false
                                    recordingFinished = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Review")
                            }
                        }
                    }
                } else {
                    Text("Recorded Audio Preview", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { isPlayingPreview = !isPlayingPreview },
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { previewProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format("0:%02d", previewSeconds),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = String.format("0:%02d", secondsElapsed),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                recordingFinished = false
                                secondsElapsed = 0
                                isPlayingPreview = false
                                previewProgress = 0.0f
                                previewSeconds = 0
                            }
                        ) {
                            Text("Re-record")
                        }

                        Button(
                            onClick = {
                                val voiceNoteName = "VoiceNote_${System.currentTimeMillis()}.mp3"
                                onRecordingComplete(voiceNoteName, secondsElapsed, "simulated_base64_audio_data")
                            }
                        ) {
                            Text("Attach Voice Note")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// MANAGER COMPOSABLES
// -------------------------------------------------------------

@Composable
fun ManagerLiveDashboard(viewModel: OnSiteViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allLogs by viewModel.allAttendanceLogs.collectAsStateWithLifecycle()

    var geofenceLatInput by remember { mutableStateOf("23.2328") }
    var geofenceLngInput by remember { mutableStateOf("77.4303") }
    var geofenceRadiusInput by remember { mutableStateOf("150") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployeeForDetail by remember { mutableStateOf<Employee?>(null) }

    // Quick Stats Calculation
    val totalCount = employees.size
    val presentCount = employees.count { it.status == "Present" }
    val absentCount = totalCount - presentCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BIRTHDAY REMINDERS & CELEBRATIONS
        BirthdayCelebrationsSection(viewModel)

        // GEOFENCE CALIBRATOR CONTROL PANEL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "GEOFENCE BOUNDS CALIBRATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextField(
                        value = geofenceLatInput,
                        onValueChange = { geofenceLatInput = it },
                        label = { Text("Latitude", fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("geofence_lat_input"),
                        singleLine = true
                    )

                    TextField(
                        value = geofenceLngInput,
                        onValueChange = { geofenceLngInput = it },
                        label = { Text("Longitude", fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("geofence_lng_input"),
                        singleLine = true
                    )

                    TextField(
                        value = geofenceRadiusInput,
                        onValueChange = { geofenceRadiusInput = it },
                        label = { Text("Radius (m)", fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("geofence_radius_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val lat = geofenceLatInput.toDoubleOrNull() ?: 23.2328
                        val lng = geofenceLngInput.toDoubleOrNull() ?: 77.4303
                        val radius = geofenceRadiusInput.toDoubleOrNull() ?: 150.0
                        viewModel.updateGeofenceSettings(lat, lng, radius)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("update_geofence_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save settings",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Recalibrate Office Geofence limits", fontSize = 13.sp)
                }
            }
        }

        // QUICK STATS CHIPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Staff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Active On-Site", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    Text("$presentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Out of Bounds", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                    Text("$absentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                }
            }
        }

        // EMPLOYEE ROSTER LIVE STATUS HEADCOUNT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Partner & Assistant Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "CA VIEW (ID Unmasked)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // SEARCH BAR
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, ID or department...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        val filteredEmployees = employees.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredEmployees) { emp ->
                val isCa = emp.role == "Manager"

                // Extract check in & out for today
                val todayLogs = allLogs.filter { it.employeeId == emp.id && android.text.format.DateUtils.isToday(it.timestamp) }
                val checkInLog = todayLogs.filter { it.type == "Check-In" }.minByOrNull { it.timestamp }
                val checkOutLog = todayLogs.filter { it.type == "Check-Out" }.maxByOrNull { it.timestamp }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEmployeeForDetail = emp },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCa)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCa) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmployeeAvatar(employee = emp, size = 38.dp)

                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = emp.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    if (!isCa) {
                                        DesignationBadge(empId = emp.id)
                                    } else {
                                        Text(
                                            text = "Partner",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "• ID: ${emp.id}", // Unmasked for CA viewing
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Check In & Check Out Ticker for today
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = "In Icon",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "In: ${checkInLog?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (checkInLog != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Out Icon",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Out: ${checkOutLog?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (checkOutLog != null) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        val statusColor = when (emp.status) {
                            "Present" -> Color(0xFF4CAF50)
                            "Late" -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = emp.status,
                                color = statusColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

    }

    if (selectedEmployeeForDetail != null) {
        EmployeeProfileDetailDialog(
            employee = selectedEmployeeForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedEmployeeForDetail = null }
        )
    }
}

@Composable
fun ManagerAlertDispatch(viewModel: OnSiteViewModel) {
    var alertTitle by remember { mutableStateOf("") }
    var alertContent by remember { mutableStateOf("") }
    val dispatchedAlerts by viewModel.systemAlerts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DISPATCH URGENT SIREN BROADCAST",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextField(
                    value = alertTitle,
                    onValueChange = { alertTitle = it },
                    label = { Text("Announcement Heading") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_title_input")
                )

                TextField(
                    value = alertContent,
                    onValueChange = { alertContent = it },
                    label = { Text("Detailed Security Guidelines") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_content_input"),
                    minLines = 2
                )

                Button(
                    onClick = {
                        if (alertTitle.isNotBlank() && alertContent.isNotBlank()) {
                            viewModel.broadcastUrgentAlert(alertTitle, alertContent)
                            alertTitle = ""
                            alertContent = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dispatch_alert_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Broadcast siren icon",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Broadcast Urgent Announcement to All Staff", fontSize = 13.sp)
                }
            }
        }

        Text(
            text = "Dispatched Alerts Log",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (dispatchedAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No previous bulletins posted.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(dispatchedAlerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (alert.priority == "Urgent") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = alert.priority,
                                        color = if (alert.priority == "Urgent") Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = alert.content,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "Dispatched: ${formatTime(alert.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerTasksDashboard(viewModel: OnSiteViewModel, currentUser: Employee) {
    val allTasks by viewModel.allTodoItems.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    
    val eligibleEmployees = employees.filter { it.role != "Manager" }
    
    var showAssignDialog by remember { mutableStateOf(false) }
    val writtenClarifications = remember { mutableStateMapOf<String, String>() }
    
    val pendingApprovals = allTasks.filter { !it.isPersonal && it.status == "Complete" && !it.isApproved }

    val doubtTasks = allTasks.filter { !it.isPersonal && it.status == "Have a Doubt" }
    val activeTasks = allTasks.filter { !it.isPersonal && it.status == "Incomplete" }
    val approvedTasks = allTasks.filter { !it.isPersonal && it.isApproved }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Task Manager Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = { showAssignDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Assign Task", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assign Task", fontSize = 12.sp)
            }
        }
        
        if (doubtTasks.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, contentDescription = "Doubt Icon", tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Employee Doubts Queue (${doubtTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        doubtTasks.forEach { task ->
                            val empName = employees.find { it.id == task.employeeId }?.name ?: task.employeeId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("doubt_task_card_${task.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFFFCC80)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Header: Title and priority chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                            Text(
                                                text = "Assignee: $empName",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        // Priority pill
                                        Surface(
                                            color = when (task.priority) {
                                                "Urgent" -> Color(0xFFFFEBEE)
                                                "High" -> Color(0xFFFFF3E0)
                                                else -> Color(0xFFF5F5F5)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = task.priority,
                                                color = when (task.priority) {
                                                    "Urgent" -> Color(0xFFC62828)
                                                    "High" -> Color(0xFFEF6C00)
                                                    else -> Color(0xFF616161)
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    if (task.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color(0xFFFFF3E0).copy(alpha = 0.5f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Box / Text field to write clarification message
                                    val currentText = writtenClarifications[task.id] ?: ""
                                    OutlinedTextField(
                                        value = currentText,
                                        onValueChange = { writtenClarifications[task.id] = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("doubt_reply_input_${task.id}"),
                                        placeholder = { Text("Write clarification details here...", fontSize = 12.sp) },
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        singleLine = false,
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFF9800),
                                            unfocusedBorderColor = Color(0xFFFFCC80)
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Row with Clarification and Summon Buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                val explanation = currentText.trim()
                                                if (explanation.isNotEmpty()) {
                                                    viewModel.sendSecureMessage("@$empName: Clarification on '${task.title}': $explanation 👍")
                                                    writtenClarifications[task.id] = ""
                                                } else {
                                                    viewModel.sendSecureMessage("@$empName: Heard you had a doubt about '${task.title}'. Let me know how I can guide you! 👍")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .testTag("send_clarification_button_${task.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Send Icon",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Send Msg", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Button(
                                            onClick = {
                                                viewModel.sendSecureMessage("⚠️ @$empName: Please report to my cabin immediately to discuss doubt on task '${task.title}'.")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .testTag("summon_cabin_button_${task.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Summon Cabin Icon",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Summon Cabin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "Pending Work Verification Queue (${pendingApprovals.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (pendingApprovals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "All employee tasks are cleared.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(pendingApprovals) { task ->
                    val empName = employees.find { it.id == task.employeeId }?.name ?: task.employeeId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Assignee: $empName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                    Text("Review", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.description, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.approveTaskCompletion(task) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.rejectTaskCompletion(task) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Redo Request", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "Active Ongoing Tasks Tracker (${activeTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        
        if (activeTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                Text("No active pending assigned tasks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(0.3f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(activeTasks) { task ->
                    val empName = employees.find { it.id == task.employeeId }?.name ?: task.employeeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Assignee: $empName • Priority: ${task.priority}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(color = Color(0xFFECEFF1), shape = RoundedCornerShape(4.dp)) {
                            Text("In Progress", color = Color(0xFF37474F), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
        
        Text(
            text = "Approved Tasks Archive (${approvedTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        
        if (approvedTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                Text("No approved tasks in the historical archive.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(0.3f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(approvedTasks) { task ->
                    val empName = employees.find { it.id == task.employeeId }?.name ?: task.employeeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text("Assignee: $empName • Approved", style = MaterialTheme.typography.labelSmall, color = Color(0xFF388E3C))
                        }
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Approved", tint = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
    
    if (showAssignDialog) {
        var selectedEmpId by remember { mutableStateOf(eligibleEmployees.firstOrNull()?.id ?: "") }
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var selectedPriority by remember { mutableStateOf("Medium") }
        var expandedDropdown by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign New Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Staff/Article Assignee:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentSelectionName = eligibleEmployees.find { it.id == selectedEmpId }?.name ?: "Select Assignee"
                        OutlinedButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text(currentSelectionName)
                        }
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            eligibleEmployees.forEach { emp ->
                                val roleLabel = if (emp.role == "staff") "Staff" else "Article"
                                DropdownMenuItem(
                                    text = { Text("${emp.name} ($roleLabel)") },
                                    onClick = {
                                        selectedEmpId = emp.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    TextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    TextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Priority Level:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("High", "Medium", "Low").forEach { level ->
                            OutlinedButton(
                                onClick = { selectedPriority = level },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedPriority == level)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                            ) {
                                Text(level, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedEmpId.isNotBlank() && taskTitle.isNotBlank()) {
                            viewModel.assignTaskToEmployee(
                                employeeId = selectedEmpId,
                                title = taskTitle,
                                description = taskDesc,
                                priority = selectedPriority,
                                assignedBy = currentUser.name
                            )
                            showAssignDialog = false
                        }
                    }
                ) {
                    Text("Assign Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper time formatting tool
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Helper date & time formatting tool for historical records
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return try {
        val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

fun generateCustomAvatarBitmap(name: String, colorSeed: Int, styleIndex: Int): String {
    val width = 200
    val height = 200
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    val bgColors = listOf(
        0xFF1A237E.toInt(), // Indigo
        0xFF006064.toInt(), // Cyan
        0xFF1B5E20.toInt(), // Green
        0xFF3E2723.toInt(), // Brown
        0xFF4A148C.toInt(), // Purple
        0xFFB71C1C.toInt()  // Red
    )
    val bgColor = bgColors[colorSeed % bgColors.size]
    canvas.drawColor(bgColor)

    paint.color = 0x22FFFFFF
    canvas.drawCircle(width * 0.2f, height * 0.2f, 50f, paint)
    canvas.drawCircle(width * 0.8f, height * 0.8f, 70f, paint)

    paint.color = 0xFFF5CBA7.toInt() // Skin tone
    val faceCenterX = width / 2f
    val faceCenterY = height * 0.45f
    val faceRadius = 45f
    canvas.drawCircle(faceCenterX, faceCenterY, faceRadius, paint)

    paint.color = if (styleIndex == 0) 0xFF3E2723.toInt() else 0xFF212121.toInt() // Dark Brown / Black
    if (styleIndex == 0) {
        val hairPath = android.graphics.Path().apply {
            moveTo(faceCenterX - faceRadius - 5f, faceCenterY)
            lineTo(faceCenterX - faceRadius, faceCenterY - 30f)
            quadTo(faceCenterX, faceCenterY - faceRadius - 15f, faceCenterX + faceRadius, faceCenterY - 30f)
            lineTo(faceCenterX + faceRadius + 5f, faceCenterY)
            close()
        }
        canvas.drawPath(hairPath, paint)
    } else if (styleIndex == 1) {
        val hairPath = android.graphics.Path().apply {
            moveTo(faceCenterX - faceRadius - 8f, faceCenterY + 10f)
            lineTo(faceCenterX - faceRadius, faceCenterY - 35f)
            quadTo(faceCenterX, faceCenterY - faceRadius - 25f, faceCenterX + faceRadius + 8f, faceCenterY - 20f)
            lineTo(faceCenterX + faceRadius + 4f, faceCenterY + 15f)
            close()
        }
        canvas.drawPath(hairPath, paint)
    } else {
        paint.color = 0xFF3F51B5.toInt() // Indigo glasses frame
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(faceCenterX - 18f, faceCenterY - 5f, 12f, paint)
        canvas.drawCircle(faceCenterX + 18f, faceCenterY - 5f, 12f, paint)
        canvas.drawLine(faceCenterX - 6f, faceCenterY - 5f, faceCenterX + 6f, faceCenterY - 5f, paint)
    }

    paint.style = android.graphics.Paint.Style.FILL
    paint.color = 0xFF1565C0.toInt() // Corporate blue suit
    val shouldersPath = android.graphics.Path().apply {
        moveTo(width * 0.15f, height.toFloat())
        lineTo(width * 0.85f, height.toFloat())
        lineTo(faceCenterX + 35f, height * 0.7f)
        lineTo(faceCenterX - 35f, height * 0.7f)
        close()
    }
    canvas.drawPath(shouldersPath, paint)

    paint.color = Color.White.toArgb()
    val collarPath = android.graphics.Path().apply {
        moveTo(faceCenterX - 15f, height * 0.7f)
        lineTo(faceCenterX + 15f, height * 0.7f)
        lineTo(faceCenterX, height * 0.82f)
        close()
    }
    canvas.drawPath(collarPath, paint)

    paint.color = 0xFFD32F2F.toInt() // Red tie
    val tiePath = android.graphics.Path().apply {
        moveTo(faceCenterX - 5f, height * 0.75f)
        lineTo(faceCenterX + 5f, height * 0.75f)
        lineTo(faceCenterX + 8f, height * 0.95f)
        lineTo(faceCenterX, height.toFloat())
        lineTo(faceCenterX - 8f, height * 0.95f)
        close()
    }
    canvas.drawPath(tiePath, paint)

    val outputStream = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
    val bytes = outputStream.toByteArray()
    return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
}

@Composable
fun EmployeeAvatar(
    employee: Employee,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    onClick: (() -> Unit)? = null
) {
    val isCa = employee.role == "Manager"
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(if (isCa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(employee.profilePhoto) {
            employee.profilePhoto?.let { decodeBase64ToBitmap(it) }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "${employee.name} Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = employee.name.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                color = if (isCa) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ProfilePhotoUploadDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onPhotoSelected: (String?) -> Unit
) {
    var customUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    // Standard Base64 encoding
                    val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    onPhotoSelected(base64String)
                    android.widget.Toast.makeText(context, "Photo uploaded from device successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Failed to read image data.", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error reading image: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Profile Photo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Insert or update profile photo for ${employee.name}.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Current Image Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(employee.profilePhoto) {
                        employee.profilePhoto?.let { decodeBase64ToBitmap(it) }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Current Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = employee.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Gallery Option
                Text(
                    text = "Option 1: Select from Gallery or Photos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery / Photos", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Presets Title
                Text(
                    text = "Option 2: Choose Portrait Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Presets Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (0..4).forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val presetBase64 = generateCustomAvatarBitmap(
                                        name = employee.name,
                                        colorSeed = index * 7 + 3,
                                        styleIndex = index % 3
                                    )
                                    onPhotoSelected(presetBase64)
                                    android.widget.Toast.makeText(context, "Preset profile photo applied!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val presetBase64 = remember {
                                generateCustomAvatarBitmap(
                                    name = employee.name,
                                    colorSeed = index * 7 + 3,
                                    styleIndex = index % 3
                                )
                            }
                            val bitmap = remember { decodeBase64ToBitmap(presetBase64) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Preset $index",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Shutter Camera Simulation Title
                Text(
                    text = "Option 3: Camera Simulation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Button(
                    onClick = {
                        val cameraBase64 = generateCustomAvatarBitmap(
                            name = employee.name,
                            colorSeed = (10..99).random(),
                            styleIndex = (0..2).random()
                        )
                        onPhotoSelected(cameraBase64)
                        android.widget.Toast.makeText(context, "📸 Click! Simulated photo captured successfully!", android.widget.Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capture Simulated Snap", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Clear Photo Option
                Text(
                    text = "Option 4: Clear Photo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                OutlinedButton(
                    onClick = {
                        onPhotoSelected(null)
                        android.widget.Toast.makeText(context, "Profile photo cleared.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Current Profile Photo")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun EmployeeProfileDetailDialog(
    employee: Employee,
    viewModel: OnSiteViewModel,
    onDismiss: () -> Unit
) {
    val details = remember(employee.id) { getDetailedProfile(employee.id) }
    var showPhotoUpload by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCa = currentUser?.role == "Manager"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EmployeeAvatar(employee, size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(employee.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Role: ${employee.role} • Dept: ${employee.department}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showPhotoUpload = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Insert / Change Profile Photo", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Detailed profile items
                ProfileDetailItem("Fathers Name", details.fathersName)
                ProfileDetailItem("Mothers Name", details.mothersName)
                ProfileDetailItem("Date of Birth", details.dob)
                ProfileDetailItem("Date of Joining", details.doj)
                ProfileDetailItem("Blood Group", details.bloodGroup)
                ProfileDetailItem("Personal Phone", details.phone)
                ProfileDetailItem("Emergency Contact", details.emergencyContact)
                ProfileDetailItem("Personal Email", details.email)
                ProfileDetailItem("Residential Address", details.address)
                ProfileDetailItem("On-Site ID (Unmasked)", employee.id)
                ProfileDetailItem("Security Passcode", employee.password)

                if (isCa) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("delete_profile_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showPhotoUpload) {
        ProfilePhotoUploadDialog(
            employee = employee,
            onDismiss = { showPhotoUpload = false },
            onPhotoSelected = { base64 ->
                viewModel.updateEmployeeProfilePhoto(employee.id, base64)
                showPhotoUpload = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete the profile of ${employee.name}? All attendance logs and records for this user will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(employee.id)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_profile_confirm")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun getDetailedProfile(empId: String): DetailedProfile {
    return when (empId) {
        "CA-GU-01" -> DetailedProfile(
            id = "CA-GU-01",
            userId = "CA-GU-01",
            age = 45,
            dob = "15-Aug-1981",
            fathersName = "Late Sh. R. K. Gupta",
            mothersName = "Mrs. Savitri Gupta",
            address = "74, Trilanga, Bhopal, MP - 462039",
            email = "anuj.gupta@guptalakhani.com",
            phone = "+91 94250 11223",
            emergencyContact = "+91 94250 99887",
            doj = "10-Apr-2008",
            bloodGroup = "B+"
        )
        "CA-LA-02" -> DetailedProfile(
            id = "CA-LA-02",
            userId = "CA-LA-02",
            age = 42,
            dob = "22-Nov-1983",
            fathersName = "Sh. Suresh Lakhani",
            mothersName = "Mrs. Pushpa Lakhani",
            address = "102, Riviera Town, Bhopal, MP - 462003",
            email = "meera.lakhani@guptalakhani.com",
            phone = "+91 98930 44556",
            emergencyContact = "+91 98930 11223",
            doj = "15-Jul-2010",
            bloodGroup = "O+"
        )
        "EMP-RS-54" -> DetailedProfile(
            id = "EMP-RS-54",
            userId = "EMP-RS-54",
            age = 22,
            dob = "12-Oct-2003",
            fathersName = "Mr. Ramesh Sharma",
            mothersName = "Mrs. Sunita Sharma",
            address = "H.No. 45, E-7, Arera Colony, Bhopal, MP - 462016",
            email = "rahul.sharma@guptalakhani.com",
            phone = "+91 98765 43210",
            emergencyContact = "+91 98765 99999",
            doj = "01-Jun-2025",
            bloodGroup = "O+"
        )
        "EMP-PP-88" -> DetailedProfile(
            id = "EMP-PP-88",
            userId = "EMP-PP-88",
            age = 24,
            dob = "05-May-2002",
            fathersName = "Mr. Kirit Patel",
            mothersName = "Mrs. Kokila Patel",
            address = "Flat 304, Gulmohar Heights, M.P. Nagar, Bhopal, MP - 462011",
            email = "priya.patel@guptalakhani.com",
            phone = "+91 91112 33445",
            emergencyContact = "+91 91112 55667",
            doj = "15-Jan-2024",
            bloodGroup = "A+"
        )
        "EMP-VS-22" -> DetailedProfile(
            id = "EMP-VS-22",
            userId = "EMP-VS-22",
            age = 26,
            dob = "18-Sep-2000",
            fathersName = "Mr. Mahendra Singh",
            mothersName = "Mrs. Rajeshwari Singh",
            address = "C-12, Shahpura, Bhopal, MP - 462039",
            email = "vikram.singh@guptalakhani.com",
            phone = "+91 95890 77889",
            emergencyContact = "+91 95890 11223",
            doj = "10-Nov-2023",
            bloodGroup = "AB+"
        )
        "EMP-RM-19" -> DetailedProfile(
            id = "EMP-RM-19",
            userId = "EMP-RM-19",
            age = 23,
            dob = "30-Jan-2003",
            fathersName = "Mr. Nitin Mehta",
            mothersName = "Mrs. Alpa Mehta",
            address = "B-8, Saket Nagar, Bhopal, MP - 462024",
            email = "rohan.mehta@guptalakhani.com",
            phone = "+91 70001 22334",
            emergencyContact = "+91 70001 99887",
            doj = "01-Mar-2025",
            bloodGroup = "A-"
        )
        else -> DetailedProfile(
            id = empId,
            userId = empId,
            age = 23,
            dob = "01-Jan-2003",
            fathersName = "Father Name",
            mothersName = "Mother Name",
            address = "Bhopal, Madhya Pradesh",
            email = "support@guptalakhani.com",
            phone = "+91 99999 99999",
            emergencyContact = "+91 99999 88888",
            doj = "01-Jan-2025",
            bloodGroup = "O+"
        )
    }
}

@Composable
fun PartnerAttendanceScreen(viewModel: OnSiteViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allLogs by viewModel.allAttendanceLogs.collectAsStateWithLifecycle()
    val allLeaveApplications by viewModel.allLeaveRequests.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Day-wise Attendance, 1: Individual History

    // Date state for Day-wise
    var dayWiseDate by remember { mutableStateOf("01-07-2026") }
    // Date states for Individual
    var selectedEmployeeId by remember { mutableStateOf("") }
    var dateFrom by remember { mutableStateOf("01-07-2026") }
    var dateTo by remember { mutableStateOf("07-07-2026") }
    var showCalendarForField by remember { mutableStateOf<String?>(null) }

    // Dropdowns open state
    var showEmpDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab selector
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Day-wise Attendance", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Individual History", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (activeTab == 0) {
            // DAY-WISE SEARCH
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Filter Attendance by Specific Date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dayWiseDate,
                            onValueChange = {},
                            label = { Text("Date (dd-MM-yyyy)") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCalendarForField = "dayWiseDate" }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose Date", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.weight(1f).clickable { showCalendarForField = "dayWiseDate" }.testTag("daywise_date_input"),
                            singleLine = true
                        )

                        // Quick buttons to step or select
                        Button(
                            onClick = { dayWiseDate = "01-07-2026" }
                        ) {
                            Text("Reset")
                        }
                    }

                    // Dynamically listing unique log dates from DB to tap and pick instantly
                    val uniqueDates = remember(allLogs) {
                        allLogs.map { 
                            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                            sdf.format(java.util.Date(it.timestamp))
                        }.distinct().take(5)
                    }

                    if (uniqueDates.isNotEmpty()) {
                        Column {
                            Text("Quick Select Recent Activity Dates:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                uniqueDates.forEach { date ->
                                    SuggestionChip(
                                        onClick = { dayWiseDate = date },
                                        label = { Text(date, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results list for the chosen date
            val matchingLogsForDay = remember(allLogs, dayWiseDate) {
                allLogs.filter {
                    val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                    sdf.format(java.util.Date(it.timestamp)) == dayWiseDate
                }
            }

            Text("Attendance roster for date: $dayWiseDate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (matchingLogsForDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs found for $dayWiseDate. Try another date like 01-07-2026.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group by employee
                    val logsByEmployee = matchingLogsForDay.groupBy { it.employeeId }
                    items(logsByEmployee.keys.toList()) { empId ->
                        val empName = logsByEmployee[empId]?.firstOrNull()?.employeeName ?: "Employee"
                        val empLogs = logsByEmployee[empId] ?: emptyList()
                        val checkIn = empLogs.filter { it.type == "Check-In" }.minByOrNull { it.timestamp }
                        val checkOut = empLogs.filter { it.type == "Check-Out" }.maxByOrNull { it.timestamp }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(empName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("ID: $empId", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "In: ${checkIn?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (checkIn != null) Color(0xFF2E7D32) else Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Out: ${checkOut?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (checkOut != null) Color(0xFFC62828) else Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Hours worked calculation
                                val hrs = if (checkIn != null && checkOut != null) {
                                    val diffMs = checkOut.timestamp - checkIn.timestamp
                                    val hours = diffMs.toDouble() / (1000.0 * 60.0 * 60.0)
                                    String.format(java.util.Locale.US, "%.2f hrs", hours)
                                } else {
                                    "--"
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Worked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(hrs, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // INDIVIDUAL SEARCH
            val selectedEmployee = employees.find { it.id == selectedEmployeeId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Query Individual Member Reports", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    // Employee drop down selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showEmpDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedEmployee?.name ?: "Select Staff Member", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showEmpDropdown,
                            onDismissRequest = { showEmpDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.name} (${emp.role})", fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedEmployeeId = emp.id
                                        showEmpDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Date From and To range textfields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = dateFrom,
                            onValueChange = {},
                            label = { Text("Date From (dd-MM-yyyy)", fontSize = 10.sp) },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCalendarForField = "dateFrom" }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose From Date", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.weight(1f).clickable { showCalendarForField = "dateFrom" }.testTag("individual_from_date"),
                            singleLine = true
                        )

                        TextField(
                            value = dateTo,
                            onValueChange = {},
                            label = { Text("Date To (dd-MM-yyyy)", fontSize = 10.sp) },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCalendarForField = "dateTo" }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose To Date", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.weight(1f).clickable { showCalendarForField = "dateTo" }.testTag("individual_to_date"),
                            singleLine = true
                        )
                    }
                }
            }

            if (selectedEmployeeId.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please select a staff member to compute work and leave summary.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Compute summary metrics
                // Parse date range
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                val fromDate = try { sdf.parse(dateFrom) } catch (e: Exception) { null }
                val toDate = try { sdf.parse(dateTo) } catch (e: Exception) { null }

                // Filter logs within range for this specific employee
                val filteredLogs = remember(allLogs, selectedEmployeeId, fromDate, toDate) {
                    allLogs.filter { log ->
                        if (log.employeeId != selectedEmployeeId) return@filter false
                        val logDateStr = sdf.format(java.util.Date(log.timestamp))
                        val logDate = try { sdf.parse(logDateStr) } catch (e: Exception) { null }
                        if (logDate != null) {
                            val afterFrom = fromDate?.let { !logDate.before(it) } ?: true
                            val beforeTo = toDate?.let { !logDate.after(it) } ?: true
                            afterFrom && beforeTo
                        } else {
                            false
                        }
                    }
                }

                // Group filtered logs by day to calculate work hours per day
                val logsByDayStr = filteredLogs.groupBy { log ->
                    sdf.format(java.util.Date(log.timestamp))
                }

                var totalWorkingHours = 0.0
                var daysWorked = 0

                logsByDayStr.forEach { (_, logs) ->
                    val inLog = logs.filter { it.type == "Check-In" }.minByOrNull { it.timestamp }
                    val outLog = logs.filter { it.type == "Check-Out" }.maxByOrNull { it.timestamp }
                    if (inLog != null && outLog != null) {
                        val diffMs = outLog.timestamp - inLog.timestamp
                        totalWorkingHours += diffMs.toDouble() / (1000.0 * 60.0 * 60.0)
                        daysWorked++
                    } else if (inLog != null) {
                        // Assume 4 hours if only checked in or active
                        totalWorkingHours += 4.0
                        daysWorked++
                    }
                }

                // Filter approved leaves taken in date range
                val approvedLeaves = remember(allLeaveApplications, selectedEmployeeId, fromDate, toDate) {
                    allLeaveApplications.filter { leave ->
                        leave.employeeId == selectedEmployeeId && leave.status == "Accepted" && run {
                            val leaveDate = try { sdf.parse(leave.startDate) } catch (e: Exception) { null }
                            if (leaveDate != null) {
                                val afterFrom = fromDate?.let { !leaveDate.before(it) } ?: true
                                val beforeTo = toDate?.let { !leaveDate.after(it) } ?: true
                                afterFrom && beforeTo
                            } else {
                                false
                            }
                        }
                    }
                }

                val leavesCount = approvedLeaves.size

                // Summary Dashboard Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Days Worked", style = MaterialTheme.typography.labelSmall)
                            Text("$daysWorked days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Hours Worked", style = MaterialTheme.typography.labelSmall)
                            Text(String.format(java.util.Locale.US, "%.1f hrs", totalWorkingHours), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Leaves Taken", style = MaterialTheme.typography.labelSmall)
                            Text("$leavesCount leaves", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }

                Text("Detailed Logs Range: $dateFrom to $dateTo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (logsByDayStr.isEmpty() && approvedLeaves.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records found in this range for ${selectedEmployee?.name}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Days list
                        val sortedDays = logsByDayStr.keys.sortedByDescending { sdf.parse(it) }
                        items(sortedDays) { day ->
                            val logs = logsByDayStr[day] ?: emptyList()
                            val inLog = logs.filter { it.type == "Check-In" }.minByOrNull { it.timestamp }
                            val outLog = logs.filter { it.type == "Check-Out" }.maxByOrNull { it.timestamp }
                            val hours = if (inLog != null && outLog != null) {
                                (outLog.timestamp - inLog.timestamp).toDouble() / (1000.0 * 60.0 * 60.0)
                            } else if (inLog != null) {
                                4.0
                            } else {
                                0.0
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(day, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "In: ${inLog?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (inLog != null) Color(0xFF2E7D32) else Color.Gray
                                            )
                                            Text(
                                                text = "Out: ${outLog?.let { formatTime(it.timestamp) } ?: "--:--"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (outLog != null) Color(0xFFC62828) else Color.Gray
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Worked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format(java.util.Locale.US, "%.2f hrs", hours), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Leaves list
                        items(approvedLeaves) { leave ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                border = BorderStroke(1.dp, Color(0xFFFFD54F))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Leave: ${leave.startDate}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF8D6E63))
                                        Text("Reason: ${leave.reason}", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                                    }

                                    Surface(
                                        color = Color(0xFFFFB300),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Approved Leave",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalendarForField != null) {
        val fieldName = showCalendarForField!!
        val currentVal = when (fieldName) {
            "dayWiseDate" -> dayWiseDate
            "dateFrom" -> dateFrom
            "dateTo" -> dateTo
            else -> ""
        }
        CalendarDialog(
            initialDate = if (currentVal.isBlank()) "01-07-2026" else currentVal,
            format = "dd-MM-yyyy",
            onDismissRequest = { showCalendarForField = null },
            onDateSelected = { selectedDate ->
                when (fieldName) {
                    "dayWiseDate" -> dayWiseDate = selectedDate
                    "dateFrom" -> dateFrom = selectedDate
                    "dateTo" -> dateTo = selectedDate
                }
                showCalendarForField = null
            }
        )
    }
}

@Composable
fun MyProfileScreen(viewModel: OnSiteViewModel, user: Employee) {
    val profileDbState by viewModel.getDetailedProfileFlow(user.id).collectAsStateWithLifecycle(initialValue = null)
    val profile = profileDbState ?: remember(user.id) { getDetailedProfile(user.id) }
    val isDarkModeState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val colorThemeState by viewModel.colorTheme.collectAsStateWithLifecycle()
    val isFirestoreEnabledState by viewModel.isFirestoreEnabled.collectAsStateWithLifecycle()
    val firestoreProjectIdState by viewModel.firestoreProjectId.collectAsStateWithLifecycle()
    val lastFirestoreSyncTimeState by viewModel.lastFirestoreSyncTime.collectAsStateWithLifecycle()
    val firestoreSyncStatusState by viewModel.firestoreSyncStatus.collectAsStateWithLifecycle()
    val isCa = user.role == "Manager"
    var showPhotoUpload by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // 2FA Setup States
    var showSetup2FADialog by remember { mutableStateOf(false) }
    var setup2FAQrCode by remember { mutableStateOf("") }
    var setup2FASecret by remember { mutableStateOf("") }
    var setup2FACode by remember { mutableStateOf("") }
    var setup2FAError by remember { mutableStateOf("") }
    var isSettingUp2FA by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }

    // Temporary form states
    var editAge by remember { mutableStateOf("") }
    var editDob by remember { mutableStateOf("") }
    var editFathersName by remember { mutableStateOf("") }
    var editMothersName by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editEmergencyContact by remember { mutableStateOf("") }
    var editDoj by remember { mutableStateOf("") }
    var editBloodGroup by remember { mutableStateOf("") }
    var showCalendarForField by remember { mutableStateOf<String?>(null) }

    // Core Employee account details states
    var editName by remember { mutableStateOf("") }
    var editDept by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }

    // Initialize/Update text states when entering edit mode or when profile changes
    LaunchedEffect(isEditing, profile, user) {
        if (isEditing) {
            editAge = profile.age.toString()
            editDob = profile.dob
            editFathersName = profile.fathersName
            editMothersName = profile.mothersName
            editAddress = profile.address
            editPhone = profile.phone
            editEmail = profile.email
            editEmergencyContact = profile.emergencyContact
            editDoj = profile.doj
            editBloodGroup = profile.bloodGroup

            // Core Employee fields
            editName = user.name
            editDept = user.department
            editPassword = user.password
        }
    }

    ResponsiveLayout.ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 640.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TOP HEADER CARD WITH GRADIENT/HERO EFFECT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmployeeAvatar(user, size = 80.dp)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showPhotoUpload = true },
                        modifier = Modifier.testTag("change_photo_btn")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (!isEditing) {
                        TextButton(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { isEditing = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("cancel_edit_btn")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCa) {
                        Text(
                            text = "Senior Partner, CA",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        DesignationBadge(empId = user.id)
                    }
                }
            }
        }

        if (isEditing) {
            // Save Changes header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "You are in Edit Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            val updatedProfile = DetailedProfile(
                                id = user.id,
                                userId = user.id,
                                age = editAge.toIntOrNull() ?: profile.age,
                                dob = editDob,
                                fathersName = editFathersName,
                                mothersName = editMothersName,
                                address = editAddress,
                                phone = editPhone,
                                email = editEmail,
                                emergencyContact = editEmergencyContact,
                                doj = editDoj,
                                bloodGroup = editBloodGroup
                            )
                            viewModel.saveDetailedProfile(updatedProfile)
                            // Save the core Employee details
                            viewModel.updateEmployeeDetails(
                                employeeId = user.id,
                                name = editName,
                                email = editEmail, // Keep emails in sync
                                department = editDept,
                                password = editPassword
                            )
                            isEditing = false
                        },
                        modifier = Modifier.testTag("save_profile_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Editable Core Credentials Card
            ProfileSectionCard(title = "Edit Core Account Details", icon = Icons.Default.Lock) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Designation instead of Department text field
                Text(
                    text = "Designation (Article / Partner / Staff)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Article", "Partner", "Staff").forEach { d ->
                        val isSelected = editDept == d
                        Surface(
                            onClick = { editDept = d },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_dept_chip_" + d)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editPassword,
                    onValueChange = { editPassword = it },
                    label = { Text("Secure Passcode / PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_password")
                )
            }

            // Editable Personal Details Card
            ProfileSectionCard(title = "Edit Personal Information", icon = Icons.Default.Person) {
                // Auto Calculated Age (Read Only)
                OutlinedTextField(
                    value = if (editAge.isNotEmpty()) "$editAge Years" else "",
                    onValueChange = {},
                    label = { Text("Auto-Calculated Age") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_age")
                )

                // Date of Birth (DOB) Field (Read only text field, trigger calendar preview)
                OutlinedTextField(
                    value = editDob,
                    onValueChange = {},
                    label = { Text("Date of Birth (DOB)") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showCalendarForField = "editDob" }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Choose DOB", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "editDob" }.testTag("edit_profile_dob")
                )

                OutlinedTextField(
                    value = editBloodGroup,
                    onValueChange = { editBloodGroup = it },
                    label = { Text("Blood Group") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_blood")
                )
            }

            // Editable Family Details Card
            ProfileSectionCard(title = "Edit Family Details", icon = Icons.Default.Groups) {
                OutlinedTextField(
                    value = editFathersName,
                    onValueChange = { editFathersName = it },
                    label = { Text("Father's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_father")
                )
                OutlinedTextField(
                    value = editMothersName,
                    onValueChange = { editMothersName = it },
                    label = { Text("Mother's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_mother")
                )
            }

            // Editable Contact Details Card
            ProfileSectionCard(title = "Edit Contact & Address Details", icon = Icons.Default.Business) {
                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 10) {
                            editPhone = filtered
                        }
                    },
                    label = { Text("Mobile Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                )
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Official Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_email")
                )
                OutlinedTextField(
                    value = editEmergencyContact,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 10) {
                            editEmergencyContact = filtered
                        }
                    },
                    label = { Text("Emergency Contact") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_emergency")
                )
                OutlinedTextField(
                    value = editAddress,
                    onValueChange = { editAddress = it },
                    label = { Text("Permanent Address") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_address")
                )
            }

            // Editable Professional Details Card
            ProfileSectionCard(title = "Edit Professional Details", icon = Icons.Default.Work) {
                // Date of Joining (DOJ) Field (Read only text field, trigger calendar preview)
                OutlinedTextField(
                    value = editDoj,
                    onValueChange = {},
                    label = { Text("Date of Joining (DOJ)") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showCalendarForField = "editDoj" }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Choose DOJ", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "editDoj" }.testTag("edit_profile_doj")
                )
            }
        } else {
            // PERSONAL DETAILS CARD
            ProfileSectionCard(title = "Personal Information", icon = Icons.Default.Person) {
                ProfileRow(label = "Age", value = "${profile.age} Years")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Date of Birth (DOB)", value = profile.dob)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Blood Group", value = profile.bloodGroup)
            }

            // FAMILY DETAILS CARD
            ProfileSectionCard(title = "Family Details", icon = Icons.Default.Groups) {
                ProfileRow(label = "Father's Name", value = profile.fathersName)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Mother's Name", value = profile.mothersName)
            }

            // CONTACT DETAILS CARD
            ProfileSectionCard(title = "Contact & Address Details", icon = Icons.Default.Business) {
                ProfileRow(label = "Mobile Number", value = profile.phone)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Official Email", value = profile.email)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Emergency Contact", value = profile.emergencyContact)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Permanent Address", value = profile.address)
            }

            // PROFESSIONAL DETAILS CARD
            ProfileSectionCard(title = "Professional Placement", icon = Icons.Default.Work) {
                ProfileRow(label = "Date of Joining (DOJ)", value = profile.doj)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Firm Chambers Placement", value = "Bhopal Branch, Zone-I")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Audit Department", value = user.department)
            }

            // SECURITY & CREDENTIALS CARD
            ProfileSectionCard(title = "Security & Credentials", icon = Icons.Default.Lock) {
                ProfileRow(label = "Account ID", value = user.id)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ProfileRow(label = "Account Role", value = if (user.role == "Manager") "CA Partner" else "Article / Staff")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("change_password_option_btn").weight(1f)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Password", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Two-Factor Authentication", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    var is2faChecked by remember(user.is_2fa_enabled) { mutableStateOf(user.is_2fa_enabled) }
                    Switch(
                        checked = is2faChecked,
                        onCheckedChange = { checked ->
                            is2faChecked = checked
                            scope.launch {
                                try {
                                    if (checked) {
                                        val response = com.example.api.ApiClient.authService.setup2FA(com.example.api.Setup2FARequest(email = user.email))
                                        setup2FAQrCode = response.qrCode
                                        setup2FASecret = response.secret
                                        showSetup2FADialog = true
                                    } else {
                                        com.example.api.ApiClient.authService.disable2FA(com.example.api.Setup2FARequest(email = user.email))
                                        android.widget.Toast.makeText(context, "2FA Disabled", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    is2faChecked = !checked
                                    android.widget.Toast.makeText(context, "Error updating 2FA: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THEME SETTINGS CARD
            ProfileSectionCard(title = "Theme Settings", icon = Icons.Default.Palette) {
                // Dark Mode Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark Mode Theme",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isDarkModeState) "Currently Dark" else "Currently Light (Default)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isDarkModeState,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                
                // Color Theme Selector
                Text(
                    text = "Select Application Color Palette",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rows of themes
                val availableThemes = listOf(
                    "Classic M3" to Color(0xFF6750A4),
                    "Emerald Audit" to Color(0xFF006B54),
                    "Sapphire Sky" to Color(0xFF0D47A1),
                    "Sunset Crimson" to Color(0xFFB22A00)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableThemes.forEach { (themeName, primaryColor) ->
                        val isSelected = colorThemeState == themeName
                        Card(
                            onClick = { viewModel.setColorTheme(themeName) },
                            modifier = Modifier.fillMaxWidth().testTag("theme_card_$themeName"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // A small color indicator circle
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    ) {}
                                    Text(
                                        text = themeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected Theme",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    if (showPhotoUpload) {
        ProfilePhotoUploadDialog(
            employee = user,
            onDismiss = { showPhotoUpload = false },
            onPhotoSelected = { base64 ->
                viewModel.updateEmployeeProfilePhoto(user.id, base64)
                showPhotoUpload = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            user = user,
            viewModel = viewModel,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showCalendarForField != null) {
        val fieldName = showCalendarForField!!
        val currentVal = when (fieldName) {
            "editDob" -> editDob
            "editDoj" -> editDoj
            else -> ""
        }
        CalendarDialog(
            initialDate = if (currentVal.isBlank()) "2000-01-01" else currentVal,
            format = "yyyy-MM-dd",
            onDismissRequest = { showCalendarForField = null },
            onDateSelected = { selectedDate ->
                if (fieldName == "editDob") {
                    editDob = selectedDate
                    editAge = calculateAge(selectedDate).toString()
                } else if (fieldName == "editDoj") {
                    editDoj = selectedDate
                }
                showCalendarForField = null
            }
        )
    }
    }
    
    // 2FA Setup Dialog
    if (showSetup2FADialog) {
        AlertDialog(
            onDismissRequest = {
                showSetup2FADialog = false
                setup2FAQrCode = ""
                setup2FASecret = ""
                setup2FACode = ""
                setup2FAError = ""
            },
            title = { Text("Setup Two-Factor Authentication") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1. Scan the QR code with Google Authenticator.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (setup2FAQrCode.isNotEmpty()) {
                        val base64String = setup2FAQrCode.substringAfter("base64,")
                        val bitmap = try {
                            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) {
                            null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "2FA QR Code",
                                modifier = Modifier.size(200.dp)
                            )
                        } else {
                            Text("Failed to load QR Code.")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("2. Enter the 6-digit code to verify.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = setup2FACode,
                        onValueChange = { setup2FACode = it },
                        label = { Text("6-Digit Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (setup2FAError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(setup2FAError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (setup2FACode.isBlank()) {
                            setup2FAError = "Code is required"
                            return@Button
                        }
                        isSettingUp2FA = true
                        setup2FAError = ""
                        scope.launch {
                            try {
                                val response = com.example.api.ApiClient.authService.verify2FASetup(
                                    com.example.api.Verify2FASetupRequest(email = user.email, token = setup2FACode.trim())
                                )
                                if (response.success) {
                                    showSetup2FADialog = false
                                }
                            } catch (e: Exception) {
                                setup2FAError = "Invalid code. Try again."
                            } finally {
                                isSettingUp2FA = false
                            }
                        }
                    },
                    enabled = !isSettingUp2FA
                ) {
                    if (isSettingUp2FA) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify & Enable")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSetup2FADialog = false
                        setup2FAQrCode = ""
                        setup2FASecret = ""
                        setup2FACode = ""
                        setup2FAError = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    user: Employee,
    viewModel: OnSiteViewModel,
    onDismiss: () -> Unit
) {
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    var isChanging by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isChanging) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ensure your passcode is secure and easy to remember. Passcodes must be at least 4 characters long.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current Password Field
                OutlinedTextField(
                    value = currentPasswordInput,
                    onValueChange = { currentPasswordInput = it; errorMsg = "" },
                    label = { Text("Current Passcode") },
                    placeholder = { Text("Enter current passcode") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                            Icon(
                                imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Passcode Visibility"
                            )
                        }
                    },
                    visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !isChanging,
                    modifier = Modifier.fillMaxWidth().testTag("change_password_current")
                )

                // New Password Field
                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it; errorMsg = "" },
                    label = { Text("New Passcode (min 4 chars)") },
                    placeholder = { Text("Enter new passcode") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(
                                imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Passcode Visibility"
                            )
                        }
                    },
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !isChanging,
                    modifier = Modifier.fillMaxWidth().testTag("change_password_new")
                )

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it; errorMsg = "" },
                    label = { Text("Confirm New Passcode") },
                    placeholder = { Text("Re-enter new passcode") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Passcode Visibility"
                            )
                        }
                    },
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !isChanging,
                    modifier = Modifier.fillMaxWidth().testTag("change_password_confirm")
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPasswordInput.isEmpty() -> {
                            errorMsg = "Current passcode is required."
                        }
                        newPasswordInput.length < 4 -> {
                            errorMsg = "New passcode must be at least 4 characters long."
                        }
                        newPasswordInput != confirmPasswordInput -> {
                            errorMsg = "Confirm passcode does not match the new passcode."
                        }
                        else -> {
                            isChanging = true
                            viewModel.changePassword(
                                email = user.email,
                                currentPass = currentPasswordInput,
                                newPass = newPasswordInput
                            ) { err, success ->
                                isChanging = false
                                if (success) {
                                    android.widget.Toast.makeText(context, "Passcode updated successfully!", android.widget.Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    errorMsg = err ?: "Failed to update passcode."
                                }
                            }
                        }
                    }
                },
                enabled = !isChanging,
                modifier = Modifier.testTag("change_password_submit_btn")
            ) {
                if (isChanging) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating...")
                } else {
                    Text("Update Passcode")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isChanging,
                modifier = Modifier.testTag("change_password_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End
        )
    }
}

fun getBirthdayStatus(dob: String): String {
    try {
        val parts = dob.split("-")
        if (parts.size >= 2) {
            val dayStr = parts[0]
            val monthStr = parts[1]
            
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentMonthIndex = calendar.get(java.util.Calendar.MONTH)
            
            val monthsList = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val currentMonthStr = monthsList[currentMonthIndex]
            
            val dayInt = dayStr.toIntOrNull() ?: return ""
            
            if (currentMonthStr.equals(monthStr, ignoreCase = true) && currentDay == dayInt) {
                return "Today"
            } else if (currentMonthStr.equals(monthStr, ignoreCase = true)) {
                return "This Month"
            }
        }
    } catch (e: Exception) {
        // ignore
    }
    return ""
}

@Composable
fun BirthdayCelebrationsSection(viewModel: OnSiteViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    
    val birthdayList = employees.mapNotNull { emp ->
        val profile = getDetailedProfile(emp.id)
        val status = getBirthdayStatus(profile.dob)
        if (status.isNotEmpty()) {
            Pair(emp, status)
        } else {
            null
        }
    }
    
    if (birthdayList.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = "Cake Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Birthday Corner & Reminders 🍰",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                birthdayList.forEach { (emp, status) ->
                    val profile = getDetailedProfile(emp.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = emp.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (status == "Today") Color(0xFFFFEB3B) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (status == "Today") "🎂 TODAY!" else "Upcoming (${profile.dob.split("-").take(2).joinToString(" ")})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (status == "Today") Color(0xFF5D4037) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Age: ${profile.age} • Designation: ${if (emp.role == "Manager") "Senior Partner" else "Article/Staff"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.sendSecureMessage("Wishing a spectacular Happy Birthday to our amazing colleague, ${emp.name}! Hope you have an awesome day! 🎉🎈🎂")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Wish",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wish Them", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveRequestEmployeeScreen(viewModel: OnSiteViewModel, user: Employee) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allLeaveRequests by viewModel.allLeaveRequests.collectAsStateWithLifecycle()

    val myLeaveRequests = allLeaveRequests.filter { it.employeeId == user.id }
    val cas = employees.filter { it.role == "Manager" }

    var selectedSection by remember { mutableStateOf(0) } // 0 = Apply, 1 = Previous Leaves

    var reason by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedCaIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var applyToAll by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showCalendarForField by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        // Tab Row to switch between Apply and History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Apply for Leave", "Previous Leaves (${myLeaveRequests.size})").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selectedSection == index) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { selectedSection = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedSection == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (selectedSection == 0) {
            // Apply for Leave Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "To Whom It May Concern (Select CAs)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    applyToAll = !applyToAll
                                    if (applyToAll) selectedCaIds = emptySet()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = applyToAll,
                                onCheckedChange = {
                                    applyToAll = it ?: true
                                    if (applyToAll) selectedCaIds = emptySet()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All CAs (Broadcast to Entire Partner Board)", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (!applyToAll) {
                            Text(
                                text = "Select Individual CAs:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                cas.forEach { ca ->
                                    val isSelected = selectedCaIds.contains(ca.id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                selectedCaIds = if (isSelected) {
                                                    selectedCaIds - ca.id
                                                } else {
                                                    selectedCaIds + ca.id
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedCaIds = if (checked == true) {
                                                    selectedCaIds + ca.id
                                                } else {
                                                    selectedCaIds - ca.id
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(ca.name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {},
                            label = { Text("Start Date") },
                            placeholder = { Text("Select date via calendar") },
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showCalendarForField = "startDate" }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose Start Date", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "startDate" }.testTag("leave_start_date")
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {},
                            label = { Text("End Date") },
                            placeholder = { Text("Select date via calendar") },
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showCalendarForField = "endDate" }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose End Date", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().clickable { showCalendarForField = "endDate" }.testTag("leave_end_date")
                        )

                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it; errorMessage = "" },
                            label = { Text("Reason for Leave Application") },
                            placeholder = { Text("Explain clearly why you require leave...") },
                            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (reason.isBlank() || startDate.isBlank() || endDate.isBlank()) {
                                    errorMessage = "Please enter the start date, end date, and reason for leave."
                                } else if (!applyToAll && selectedCaIds.isEmpty()) {
                                    errorMessage = "Please select at least one CA or choose All CAs."
                                } else {
                                    val finalIds = if (applyToAll) listOf("All") else selectedCaIds.toList()
                                    val finalNames = if (applyToAll) listOf("All CAs") else cas.filter { selectedCaIds.contains(it.id) }.map { it.name }
                                    viewModel.submitLeaveRequest(
                                        reason = reason,
                                        startDate = startDate,
                                        endDate = endDate,
                                        recipientIds = finalIds,
                                        recipientNames = finalNames
                                    )
                                    reason = ""
                                    startDate = ""
                                    endDate = ""
                                    selectedCaIds = emptySet()
                                    applyToAll = true
                                    errorMessage = ""
                                    selectedSection = 1 // Go to previous leave history
                                    android.widget.Toast.makeText(context, "Leave Application Submitted successfully!", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = reason.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("submit_leave_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Leave Application", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Previous Leaves History
            if (myLeaveRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No leaves icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No previous leave applications found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(myLeaveRequests) { req ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${req.startDate} to ${req.endDate}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Addressed To: ${req.recipientNames}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = when (req.status) {
                                            "Accepted" -> Color(0xFFE8F5E9)
                                            "Rejected" -> Color(0xFFFFEBEE)
                                            else -> Color(0xFFFFF3E0)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = req.status.uppercase(),
                                            color = when (req.status) {
                                                "Accepted" -> Color(0xFF2E7D32)
                                                "Rejected" -> Color(0xFFC62828)
                                                else -> Color(0xFFE65100)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Text(
                                    text = "Reason: ${req.reason}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (req.status != "Pending") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (req.status == "Accepted") Color(0xFFF1F8E9) else Color(0xFFFFEBEE).copy(alpha = 0.5f)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (req.status == "Accepted") Color(0xFFDCEDC8) else Color(0xFFFFCDD2)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "${req.status} by: ${req.respondedBy}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (req.status == "Accepted") Color(0xFF33691E) else Color(0xFFB71C1C)
                                            )
                                            if (req.responseComment.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "CA Remark: \"${req.responseComment}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontStyle = FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalendarForField != null) {
        val fieldName = showCalendarForField!!
        val currentVal = when (fieldName) {
            "startDate" -> startDate
            "endDate" -> endDate
            else -> ""
        }
        CalendarDialog(
            initialDate = if (currentVal.isBlank()) "2026-07-04" else currentVal,
            format = "yyyy-MM-dd",
            onDismissRequest = { showCalendarForField = null },
            onDateSelected = { selectedDate ->
                if (fieldName == "startDate") {
                    startDate = selectedDate
                } else if (fieldName == "endDate") {
                    endDate = selectedDate
                }
                errorMessage = ""
                showCalendarForField = null
            }
        )
    }
}

@Composable
fun LeaveRequestsManagerScreen(viewModel: OnSiteViewModel, user: Employee) {
    val allLeaveRequests by viewModel.allLeaveRequests.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    // Determine requests addressed to this CA specifically, or all CAs
    val pendingRequests = allLeaveRequests.filter {
        it.status == "Pending" && (it.recipientIds.contains("All") || it.recipientIds.contains(user.id))
    }
    
    val processedRequests = allLeaveRequests.filter { it.status != "Pending" }

    var selectedSection by remember { mutableStateOf(0) } // 0 = Pending, 1 = Previous Leaves, 2 = Individual Leaves
    var selectedEmployeeId by remember { mutableStateOf("") }
    var showEmpDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        // Tab Row to switch between Pending, History and Individual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "Pending (${pendingRequests.size})",
                "Previous (${processedRequests.size})",
                "Individual"
            ).forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selectedSection == index) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { selectedSection = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedSection == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (selectedSection == 0) {
            // Pending Leave Requests
            if (pendingRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Clear Checklist Icon",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No pending leave applications.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "You are all caught up!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingRequests) { req ->
                        var comment by remember { mutableStateOf("") }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = req.employeeName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${req.employeeRole} • ID: ${req.employeeId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PENDING",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Text(
                                            text = "Requested Dates: ${req.startDate} to ${req.endDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Reason: ${req.reason}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Addressed To: ${req.recipientNames}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Response Comment / CA Remarks") },
                                    placeholder = { Text("Optional approval or rejection remarks...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.approveLeaveRequest(req.id, user.name, comment)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Accept", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.rejectLeaveRequest(req.id, user.name, comment)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedSection == 1) {
            // Previous Leaves History for CA
            if (processedRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No leaves history icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No previous leaves history found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(processedRequests) { req ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = req.employeeName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Requested Dates: ${req.startDate} to ${req.endDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = when (req.status) {
                                            "Accepted" -> Color(0xFFE8F5E9)
                                            "Rejected" -> Color(0xFFFFEBEE)
                                            else -> Color(0xFFFFF3E0)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = req.status.uppercase(),
                                            color = when (req.status) {
                                                "Accepted" -> Color(0xFF2E7D32)
                                                "Rejected" -> Color(0xFFC62828)
                                                else -> Color(0xFFE65100)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Text(
                                    text = "Reason: ${req.reason}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Addressed To: ${req.recipientNames}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (req.status == "Accepted") Color(0xFFF1F8E9) else Color(0xFFFFEBEE).copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (req.status == "Accepted") Color(0xFFDCEDC8) else Color(0xFFFFCDD2)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${req.status} by: ${req.respondedBy}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (req.status == "Accepted") Color(0xFF33691E) else Color(0xFFB71C1C)
                                        )
                                        if (req.responseComment.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Remarks: \"${req.responseComment}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // INDIVIDUAL LEAVES
            val selectedEmployee = employees.find { it.id == selectedEmployeeId }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Query Individual Member Leaves", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // Employee drop down selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showEmpDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedEmployee?.name ?: "Select Staff Member", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = showEmpDropdown,
                                onDismissRequest = { showEmpDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.role})", fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedEmployeeId = emp.id
                                            showEmpDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedEmployeeId.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Please select a staff member to view leaves summary.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Filter leaves for this employee
                    val empLeaves = allLeaveRequests.filter { it.employeeId == selectedEmployeeId }
                    val empPending = empLeaves.filter { it.status == "Pending" }
                    val empAccepted = empLeaves.filter { it.status == "Accepted" }
                    val empRejected = empLeaves.filter { it.status == "Rejected" }

                    // Quick Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pending", style = MaterialTheme.typography.labelSmall)
                                Text("${empPending.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Accepted", style = MaterialTheme.typography.labelSmall)
                                Text("${empAccepted.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rejected", style = MaterialTheme.typography.labelSmall)
                                Text("${empRejected.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                        }
                    }

                    Text("All Leave Records (${empLeaves.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (empLeaves.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No leave records found for ${selectedEmployee?.name}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(empLeaves) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Dates: ${req.startDate} to ${req.endDate}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Applied on: ${formatTime(req.timestamp)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Surface(
                                                color = when (req.status) {
                                                    "Accepted" -> Color(0xFFE8F5E9)
                                                    "Rejected" -> Color(0xFFFFEBEE)
                                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = req.status.uppercase(),
                                                    color = when (req.status) {
                                                        "Accepted" -> Color(0xFF2E7D32)
                                                        "Rejected" -> Color(0xFFC62828)
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                        Text(
                                            text = "Reason: ${req.reason}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (req.status != "Pending") {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (req.status == "Accepted") Color(0xFFF1F8E9) else Color(0xFFFFEBEE).copy(alpha = 0.5f)
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (req.status == "Accepted") Color(0xFFDCEDC8) else Color(0xFFFFCDD2)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = "${req.status} by: ${req.respondedBy}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (req.status == "Accepted") Color(0xFF33691E) else Color(0xFFB71C1C)
                                                    )
                                                    if (req.responseComment.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Remarks: \"${req.responseComment}\"",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontStyle = FontStyle.Italic,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "Awaiting decision. Go to 'Pending' tab to accept/reject.",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnSiteAuthLandingScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        ResponsiveLayout.ResponsivePage(
            maxWidth = 460.dp,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            horizontalPadding = 24.dp,
            verticalPadding = 24.dp
        ) {
            // Styled Logo / Graphic
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "On-Site Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Titles
            Text(
                text = "Gupta Lakhani & Associates",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "FirmSync On-Site Ledger",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Centralized check-in, audit trackers, and instant communication portal for CAs, Article Assistants, and staff.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("landing_login_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Login to My Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("landing_signup_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Register New Staff / Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer info
            Text(
                text = "FirmSync Secure Attendance Engine v2.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Exit On-Site", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text("Are you sure you want to exit the application?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_exit_btn")
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_exit_btn")
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("exit_confirmation_dialog")
    )
}

// Helper to calculate age from DOB in yyyy-MM-dd format
fun calculateAge(dobString: String): Int {
    return try {
        val parts = dobString.split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: return 0
            val month = parts[1].toIntOrNull() ?: return 0
            val day = parts[2].toIntOrNull() ?: return 0

            val birthCal = java.util.Calendar.getInstance()
            birthCal.set(year, month - 1, day)

            val today = java.util.Calendar.getInstance()
            var age = today.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--
            }
            if (age < 0) 0 else age
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    initialDate: String,
    format: String = "yyyy-MM-dd", // "yyyy-MM-dd" or "dd-MM-yyyy"
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val sdf = remember(format) { java.text.SimpleDateFormat(format, java.util.Locale.US) }
    val initialCal = remember(initialDate, sdf) {
        java.util.Calendar.getInstance().apply {
            try {
                if (initialDate.isNotEmpty()) {
                    val parsed = sdf.parse(initialDate)
                    if (parsed != null) {
                        time = parsed
                    }
                }
            } catch (e: Exception) {
                // fallback is today
            }
        }
    }

    var selectedYear by remember { mutableStateOf(initialCal.get(java.util.Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialCal.get(java.util.Calendar.MONTH)) } // 0-indexed
    var selectedDay by remember { mutableStateOf(initialCal.get(java.util.Calendar.DAY_OF_MONTH)) }

    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = remember { ((currentYear - 80)..(currentYear + 10)).toList().reversed() }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        val tempCal = java.util.Calendar.getInstance()
        tempCal.set(java.util.Calendar.YEAR, selectedYear)
        tempCal.set(java.util.Calendar.MONTH, selectedMonth)
        tempCal.set(java.util.Calendar.DAY_OF_MONTH, 1)

        val maxDays = tempCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = tempCal.get(java.util.Calendar.DAY_OF_WEEK) // 1 = Sunday

        Pair(maxDays, firstDayOfWeek)
    }

    val maxDays = daysInMonth.first
    val startOffset = daysInMonth.second - 1 // offset days

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val resultCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.YEAR, selectedYear)
                        set(java.util.Calendar.MONTH, selectedMonth)
                        set(java.util.Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    onDateSelected(sdf.format(resultCal.time))
                },
                modifier = Modifier.testTag("calendar_ok_btn")
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("calendar_cancel_btn")
            ) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { showMonthDropdown = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.widthIn(min = 120.dp).testTag("calendar_month_btn")
                        ) {
                            Text(months[selectedMonth], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false }
                        ) {
                            months.forEachIndexed { index, mName ->
                                DropdownMenuItem(
                                    text = { Text(mName) },
                                    onClick = {
                                        selectedMonth = index
                                        showMonthDropdown = false
                                        val tempCal = java.util.Calendar.getInstance()
                                        tempCal.set(java.util.Calendar.YEAR, selectedYear)
                                        tempCal.set(java.util.Calendar.MONTH, selectedMonth)
                                        val maxInNewMonth = tempCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                                        if (selectedDay > maxInNewMonth) {
                                            selectedDay = maxInNewMonth
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showYearDropdown = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.widthIn(min = 100.dp).testTag("calendar_year_btn")
                        ) {
                            Text(selectedYear.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showYearDropdown,
                            onDismissRequest = { showYearDropdown = false },
                            modifier = Modifier.heightIn(max = 250.dp)
                        ) {
                            years.forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text(yr.toString()) },
                                    onClick = {
                                        selectedYear = yr
                                        showYearDropdown = false
                                        val tempCal = java.util.Calendar.getInstance()
                                        tempCal.set(java.util.Calendar.YEAR, selectedYear)
                                        tempCal.set(java.util.Calendar.MONTH, selectedMonth)
                                        val maxInNewMonth = tempCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                                        if (selectedDay > maxInNewMonth) {
                                            selectedDay = maxInNewMonth
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalCells = startOffset + maxDays
                val rowsCount = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until rowsCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                if (cellIndex < startOffset || cellIndex >= totalCells) {
                                    Box(modifier = Modifier.size(32.dp))
                                } else {
                                    val dayNum = cellIndex - startOffset + 1
                                    val isSelected = dayNum == selectedDay
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDay = dayNum }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier.testTag("custom_calendar_dialog")
    )
}


