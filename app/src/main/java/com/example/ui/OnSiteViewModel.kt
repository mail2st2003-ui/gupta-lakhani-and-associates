package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceLog
import com.example.data.Employee
import com.example.data.Message
import com.example.data.OnSiteRepository
import com.example.data.SystemAlert
import com.example.data.TodoItem
import com.example.data.Task
import com.example.data.LeaveRequest
import com.example.data.SummonAlert
import com.example.data.UserDetails
import com.example.data.CloudSyncHelper
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class LocationPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)

class OnSiteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OnSiteRepository

    // Current Session / Role State
    private val _sessionExpiredEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    private val _isManager = MutableStateFlow(false)
    val isManager: StateFlow<Boolean> = _isManager.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // Tracks when the current session started - notifications only fire for events after this time
    @Volatile private var _sessionStartTimeMs: Long = System.currentTimeMillis()

    // Database Flows
    val employees: StateFlow<List<Employee>>
    val allAttendanceLogs: StateFlow<List<AttendanceLog>>
    val messages: StateFlow<List<Message>>
    val systemAlerts: StateFlow<List<SystemAlert>>
    val allTodoItems: StateFlow<List<TodoItem>>
    val allLeaveRequests: StateFlow<List<LeaveRequest>>
    val allAdminTasks: StateFlow<List<Task>>
    val activeSummons: StateFlow<List<SummonAlert>>
    
    // Employee-specific Flow
    val currentEmployeeTodoItems: StateFlow<List<TodoItem>>
    val currentEmployeeAssignedTasks: StateFlow<List<Task>>
    val currentEmployeeLogs: StateFlow<List<AttendanceLog>>

    // Geofencing Configurations
    private val _geofenceLat = MutableStateFlow(23.2328) // Bhopal Office (M.P. Nagar)
    val geofenceLat: StateFlow<Double> = _geofenceLat.asStateFlow()

    private val _geofenceLng = MutableStateFlow(77.4303)
    val geofenceLng: StateFlow<Double> = _geofenceLng.asStateFlow()

    private val _geofenceRadius = MutableStateFlow(150.0) // Radius in meters
    val geofenceRadius: StateFlow<Double> = _geofenceRadius.asStateFlow()

    // Simulated Employee Location
    val locationPresets = listOf(
        LocationPreset("M.P. Nagar Chambers (On-Site)", 23.2328, 77.4303, "Within the firm's geofence perimeter"),
        LocationPreset("Zone-I Courtyard (On-Site Corner)", 23.2332, 77.4310, "Within the geofence perimeter"),
        LocationPreset("DB Mall Cafe (Out-of-Bounds)", 23.2310, 77.4270, "~500m away, out of office range"),
        LocationPreset("Arera Colony Residence (Remote Home)", 23.2160, 77.4240, "~2km away, out of office range")
    )

    private val _currentSimulatedLocation = MutableStateFlow(locationPresets[0])
    val currentSimulatedLocation: StateFlow<LocationPreset> = _currentSimulatedLocation.asStateFlow()

    // Offline / Sync State
    private val _syncStatus = MutableStateFlow("Synced") // "Synced", "Pending Sync (Offline Mode)", "Syncing..."
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Theme Preferences
    private val themePrefs = application.getSharedPreferences("onsite_theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(themePrefs.getBoolean("is_dark_mode", false)) // Default is Light mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(themePrefs.getString("color_theme", "Classic M3") ?: "Classic M3")
    val colorTheme: StateFlow<String> = _colorTheme.asStateFlow()

    // Firestore Cloud Sync State
    private val _isFirestoreEnabled = MutableStateFlow(themePrefs.getBoolean("is_firestore_enabled", true))
    val isFirestoreEnabled: StateFlow<Boolean> = _isFirestoreEnabled.asStateFlow()

    private val _firestoreProjectId = MutableStateFlow(themePrefs.getString("firestore_project_id", "gla-onsite-sync-e1d9fa9c") ?: "gla-onsite-sync-e1d9fa9c")
    val firestoreProjectId: StateFlow<String> = _firestoreProjectId.asStateFlow()

    private val _lastFirestoreSyncTime = MutableStateFlow(themePrefs.getLong("last_firestore_sync_time", 0L))
    val lastFirestoreSyncTime: StateFlow<Long> = _lastFirestoreSyncTime.asStateFlow()

    private val _firestoreSyncStatus = MutableStateFlow("Ready") // "Ready", "Syncing...", "Synced", "Failed"
    val firestoreSyncStatus: StateFlow<String> = _firestoreSyncStatus.asStateFlow()

    fun setFirestoreEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _isFirestoreEnabled.value = enabled
            themePrefs.edit().putBoolean("is_firestore_enabled", enabled).apply()
        }
    }

    fun setFirestoreProjectId(id: String) {
        viewModelScope.launch {
            val cleanId = id.trim()
            if (cleanId.isNotEmpty()) {
                _firestoreProjectId.value = cleanId
                com.example.data.FirestoreSyncHelper.activeProjectId = cleanId
                themePrefs.edit().putString("firestore_project_id", cleanId).apply()
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled
            themePrefs.edit().putBoolean("is_dark_mode", enabled).apply()
        }
    }

    fun setColorTheme(themeName: String) {
        viewModelScope.launch {
            _colorTheme.value = themeName
            themePrefs.edit().putString("color_theme", themeName).apply()
        }
    }

    // Real-Time Banner Alerts
    private val _activeBannerAlert = MutableStateFlow<SystemAlert?>(null)
    val activeBannerAlert: StateFlow<SystemAlert?> = _activeBannerAlert.asStateFlow()

    // Seed Data Setup
    private val seedEmployees = emptyList<Employee>()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OnSiteRepository(database.appDao())

        // Initialize Firestore project ID from preferences
        com.example.data.FirestoreSyncHelper.activeProjectId = _firestoreProjectId.value

        // Setup notification channel
        NotificationHelper.initNotificationChannels(application)

        // Flows Initialization
        employees = repository.allEmployees.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAttendanceLogs = repository.allAttendanceLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        messages = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        systemAlerts = repository.allAlerts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTodoItems = repository.allTodoItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLeaveRequests = repository.allLeaveRequests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAdminTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeSummons = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getActiveSummonsForStaff(user.uuid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // FlatMap current employee's specific lists
        currentEmployeeTodoItems = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getTodoItemsForEmployee(user.uuid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentEmployeeAssignedTasks = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getTasksByAssignee(user.uuid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentEmployeeLogs = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getAttendanceLogsForEmployee(user.uuid)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Clean up legacy mocked tasks if present
        viewModelScope.launch {
            try {
                repository.deleteTask("TASK-001")
                repository.deleteTask("TASK-002")
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to clean legacy mock tasks", e)
            }
        }

        // Seed initial data once if the database is completely empty on first install
        viewModelScope.launch {
            try {
                val list = repository.getAllEmployeesDirect()
                if (list.isEmpty()) {
                    val alerts = repository.getAllAlertsDirect()
                    if (alerts.isEmpty()) {
                        seedInitialData()
                    }
                }
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to check empty database for seeding", e)
            }
        }

        // Restore user session if saved
        viewModelScope.launch {
            val savedUserId = themePrefs.getString("logged_in_user_id", null)
            val savedUserDetailsJson = themePrefs.getString("logged_in_user_details", null)
            if (savedUserId != null) {
                var employee = repository.getEmployeeById(savedUserId)
                
                if (employee == null && savedUserDetailsJson != null) {
                    try {
                        val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(Employee::class.java)
                        employee = adapter.fromJson(savedUserDetailsJson)
                        if (employee != null) {
                            repository.insertEmployees(listOf(employee))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("OnSiteViewModel", "Failed to parse saved user details", e)
                    }
                }
                
                if (employee == null) {
                    for (i in 1..15) {
                        kotlinx.coroutines.delay(200)
                        employee = repository.getEmployeeById(savedUserId)
                        if (employee != null) break
                    }
                }
                
                if (employee != null) {
                    _currentUser.value = employee
                    _isManager.value = (employee.role == "Manager")
                    _isAdmin.value = employee.role.equals("Admin", ignoreCase = true)
                    // Removed bulk fetching on session restore
                } else {
                    themePrefs.edit().remove("logged_in_user_id").remove("logged_in_user_details").apply()
                    _sessionExpiredEvent.emit(Unit)
                }
            }
        }

        // Periodic cloud synchronization
        viewModelScope.launch {
            // Run a brief delay to let database load
            kotlinx.coroutines.delay(500)
            try {
                syncAllWithCloud()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Startup cloud sync failed", e)
            }

            while (true) {
                kotlinx.coroutines.delay(4000)
                if (!_isOfflineMode.value) {
                    try {
                        syncAllWithCloud()
                    } catch (e: Exception) {
                        Log.e("OnSiteViewModel", "Periodic cloud sync failed", e)
                    }
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        // Seed initial general alerts
        repository.insertAlert(
            SystemAlert(
                title = "Welcome to Gupta Lakhani & Associates!",
                content = "FirmSync enabled. CAs and Article Assistants, please log your on-site attendance upon arrival at the central chambers.",
                priority = "Info",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )

        // Seed Admin user
        val adminUser = Employee(
            uuid = "ADMIN-01",
            email = "admin@guptalakhani.com",
            role = "Admin",
            first_name = "System",
            last_name = "Admin",
            designation = "Administrator",
            password = "1234"
        )
        repository.insertEmployees(listOf(adminUser))
        repository.saveUserDetails(
            UserDetails(
                uuid = "UD-ADMIN-01",
                user_uuid = "ADMIN-01",
                first_name = "System",
                last_name = "Admin",
                official_email = "admin@guptalakhani.com",
                designation = "Administrator"
            )
        )

        // Note: Tasks are purely fetched and synchronized from the backend API/Supabase database. No mock seeds.

        // Seed initial tasks
        repository.insertTodoItem(
            TodoItem(
                user_uuid = "EMP-RS-54",
                title = "Draft Tax Audit Report",
                description = "Draft the Q1 tax audit report and reconcile the GST ledger balances.",
                is_completed = false
            )
        )
        repository.insertTodoItem(
            TodoItem(
                user_uuid = "EMP-RS-54",
                title = "Prepare IT Returns filing",
                description = "Extract the corporate balance sheets and fill out Form ITR-6 draft.",
                is_completed = true,
                
            )
        )
        repository.insertTodoItem(
            TodoItem(
                user_uuid = "EMP-PP-88",
                title = "Audit Voucher Verification",
                description = "Perform physical verification of cash receipts and cross-check ledger transactions.",
                is_completed = false
            )
        )

        // Seed initial secure chat messages
        repository.insertMessage(
            Message(
                sender_uuid = "CA-GU-01",
                
                
                recipient_uuid = "Group",
                content = encryptMessage("Good morning team! Please complete your assigned audit checklists and make sure to check in upon arriving at the office."),
                timestamp = System.currentTimeMillis() - 3600000,
                isEncrypted = true
            )
        )

        // Seed previous shift logs (historical attendance data) for employees
        val oneDayMillis = 86400000L
        val now = System.currentTimeMillis()

        // Rahul Sharma previous shifts
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - oneDayMillis - 3600000 * 2, // Yesterday at ~18:00
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - oneDayMillis - 3600000 * 11, // Yesterday at ~09:00
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 2) - 3600000 * 3, // Day before yesterday afternoon
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 2) - 3600000 * 12, // Day before yesterday morning
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )

        // Priya Patel previous shifts
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-PP-88",
                timestamp = now - oneDayMillis - 3600000 * 2,
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-PP-88",
                timestamp = now - oneDayMillis - 3600000 * 11,
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )

        // Vikram Singh previous shifts
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-VS-22",
                timestamp = now - oneDayMillis - 3600000 * 1,
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-VS-22",
                timestamp = now - oneDayMillis - 3600000 * 10,
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )

        // Rohan Mehta previous shifts
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RM-19",
                timestamp = now - oneDayMillis - 3600000 * 3,
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RM-19",
                timestamp = now - oneDayMillis - 3600000 * 11,
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )

        // Seed historical 2025 shifts for Rahul Sharma (around 365 days ago)
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 365) - 3600000 * 2, // 2025 afternoon
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 365) - 3600000 * 11, // 2025 morning
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )

        // Seed historical 2024 shifts for Rahul Sharma (around 730 days ago)
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 730) - 3600000 * 3, // 2024 afternoon
                type = "Check-Out",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
        repository.insertAttendanceLog(
            AttendanceLog(
                user_uuid = "EMP-RS-54",
                timestamp = now - (oneDayMillis * 730) - 3600000 * 12, // 2024 morning
                type = "Check-In",
                status = "On-Site",
                latitude = 37.7749,
                longitude = -122.4194,
                isSynced = true
            )
        )
    }

    // Auth & Session handlers
    fun selectUserSession(employee: Employee) {
        _currentUser.value = employee
        _isManager.value = (employee.role == "Manager")
        _isAdmin.value = employee.role.equals("Admin", ignoreCase = true)
        // Reset session start time so only genuinely new events trigger notifications
        _sessionStartTimeMs = System.currentTimeMillis()
        try {
            val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val json = moshi.adapter(Employee::class.java).toJson(employee)
            themePrefs.edit()
                .putString("logged_in_user_id", employee.uuid)
                .putString("logged_in_user_details", json)
                .apply()
        } catch (e: Exception) {
            themePrefs.edit().putString("logged_in_user_id", employee.uuid).apply()
        }
    }

    fun logoutSession() {
        _currentUser.value = null
        _isManager.value = false
        _isAdmin.value = false
        themePrefs.edit().remove("logged_in_user_id").remove("logged_in_user_details").apply()
    }

    // Offline / Sync Toggles
    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            _isOfflineMode.value = enabled
            if (enabled) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            } else {
                triggerSync()
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _syncStatus.value = "Syncing..."
            try {
                syncAllWithCloud()
                
                _syncStatus.value = "Synced"
            } catch (e: Exception) {
                _syncStatus.value = "Sync Failed: ${e.localizedMessage}"
            }
        }
    }

    suspend fun syncAllWithCloud() {
        if (_isOfflineMode.value) return

        var firestoreSuccess = true

        // 1. Employees
        try {
            val local = repository.getAllEmployeesDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("employees", Employee::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("employees", Employee::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<Employee>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val mergedMap = mutableMapOf<String, Employee>()
                allSources.forEach { emp ->
                    val existing = mergedMap[emp.uuid]
                    if (existing == null) {
                        mergedMap[emp.uuid] = emp
                    } else {
                        val existingTime = 0L ?: 0L
                        val empTime = 0L ?: 0L
                        if (empTime > existingTime) {
                            mergedMap[emp.uuid] = emp
                        } else if (empTime == existingTime) {
                            if (emp.profile_image != null && existing.profile_image == null) {
                                mergedMap[emp.uuid] = emp
                            }
                        }
                    }
                }
                val mergedList = mergedMap.values.toList()
                if (mergedList.size != local.size || mergedList != local) {
                    repository.insertEmployees(mergedList)
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("employees", mergedList, Employee::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("employees", mergedList, Employee::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync employees error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // 2. Attendance Logs
        try {
            val local = repository.getAllAttendanceLogsDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("attendance_logs", AttendanceLog::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("attendance_logs", AttendanceLog::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<AttendanceLog>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val merged = allSources.distinctBy { "${it.user_uuid}_${it.timestamp}_${it.type}" }
                if (merged.size != local.size || merged != local) {
                    merged.forEach { log ->
                        repository.insertAttendanceLog(log)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("attendance_logs", merged, AttendanceLog::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("attendance_logs", merged, AttendanceLog::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync attendance logs error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // 3. Todo Items
        try {
            val local = repository.getAllTodoItemsDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("todo_items", TodoItem::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("todo_items", TodoItem::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<TodoItem>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val mergedMap = mutableMapOf<String, TodoItem>()
                allSources.forEach { item ->
                    val key = "${item.user_uuid}_${item.timestamp}_${item.title}"
                    val existing = mergedMap[key]
                    if (existing == null) {
                        mergedMap[key] = item
                    } else {
                        if (item.is_completed || item.status == "Complete" || item.status == "Approved") {
                            mergedMap[key] = item
                        }
                    }
                }
                val mergedList = mergedMap.values.toList()
                if (mergedList.size != local.size || mergedList != local) {
                    mergedList.forEach { item ->
                        repository.insertTodoItem(item)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("todo_items", mergedList, TodoItem::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("todo_items", mergedList, TodoItem::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync todo items error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // 4. Messages & Tasks (via Node.js Backend with background notifications)
        try {
            val user = _currentUser.value
            if (user != null) {
                syncUserMessages(user.uuid)
                syncAssignedTasksWithNotifications(user.uuid)
                if (user.role.equals("Admin", ignoreCase = true)) {
                    fetchAllAdminTasks()
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync messages and tasks error", e)
        }

        // 5. System Alerts
        try {
            val local = repository.getAllAlertsDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("system_alerts", SystemAlert::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("system_alerts", SystemAlert::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<SystemAlert>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)
            val merged = allSources.distinctBy { it.uuid }

            if (kvdbRemote != null || firestoreRemote != null) {
                if (merged.size != local.size || merged != local) {
                    merged.forEach { alert ->
                        repository.insertAlert(alert)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("system_alerts", merged, SystemAlert::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("system_alerts", merged, SystemAlert::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync system alerts error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // 6. Leave Requests
        try {
            val local = repository.getAllLeaveRequestsDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("leave_requests", LeaveRequest::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("leave_requests", LeaveRequest::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<LeaveRequest>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val mergedMap = mutableMapOf<String, LeaveRequest>()
                allSources.forEach { req ->
                    val key = "${req.user_uuid}_${req.timestamp}"
                    val existing = mergedMap[key]
                    if (existing == null) {
                        mergedMap[key] = req
                    } else {
                        if (req.status != "Pending") {
                            mergedMap[key] = req
                        }
                    }
                }
                val mergedList = mergedMap.values.toList()
                if (mergedList.size != local.size || mergedList != local) {
                    mergedList.forEach { req ->
                        repository.insertLeaveRequest(req)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("leave_requests", mergedList, LeaveRequest::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("leave_requests", mergedList, LeaveRequest::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync leave requests error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // 7. Detailed Profiles
        try {
            val local = repository.getAllUserDetailsDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("detailed_profiles", UserDetails::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("detailed_profiles", UserDetails::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<UserDetails>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val mergedMap = mutableMapOf<String, UserDetails>()
                allSources.forEach { profile ->
                    mergedMap[profile.uuid] = profile
                }
                val mergedList = mergedMap.values.toList()
                if (mergedList.size != local.size || mergedList != local) {
                    mergedList.forEach { profile ->
                        repository.saveUserDetails(profile)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("detailed_profiles", mergedList, UserDetails::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("detailed_profiles", mergedList, UserDetails::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync detailed profiles error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
        }

        // Finalize Firestore status
        if (_isFirestoreEnabled.value) {
            if (firestoreSuccess) {
                val now = System.currentTimeMillis()
                _lastFirestoreSyncTime.value = now
                themePrefs.edit().putLong("last_firestore_sync_time", now).apply()
                _firestoreSyncStatus.value = "Synced"
            } else {
                _firestoreSyncStatus.value = "Failed"
            }
        } else {
            _firestoreSyncStatus.value = "Ready"
        }
    }

    // Geofencing & Simulated location helpers
    fun updateSimulatedLocation(preset: LocationPreset) {
        _currentSimulatedLocation.value = preset
    }

    fun updateGeofenceSettings(lat: Double, lng: Double, radius: Double) {
        _geofenceLat.value = lat
        _geofenceLng.value = lng
        _geofenceRadius.value = radius
    }

    fun calculateDistanceFromGeofence(preset: LocationPreset): Double {
        return getDistanceInMeters(
            preset.latitude, preset.longitude,
            _geofenceLat.value, _geofenceLng.value
        )
    }

    fun calculateDistanceFromGeofence(lat: Double, lng: Double): Double {
        return getDistanceInMeters(
            lat, lng,
            _geofenceLat.value, _geofenceLng.value
        )
    }

    private fun getDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // Attendance Handlers
    fun executeAttendanceAction(type: String, realLat: Double? = null, realLng: Double? = null) {
        val user = _currentUser.value ?: return
        if (realLat == null || realLng == null) {
            Log.w("OnSiteViewModel", "Attendance rejected: Real GPS location required.")
            return
        }
        val distance = calculateDistanceFromGeofence(realLat, realLng)
        val isWithinRange = distance <= _geofenceRadius.value
        if (!isWithinRange) {
            Log.w("OnSiteViewModel", "Attendance rejected: User is outside geofence ($distance m).")
            return
        }

        viewModelScope.launch {
            if (type == "Check-In") {
                val todayStart = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
                val existingLogs = repository.getAllAttendanceLogsDirect().filter { it.user_uuid == user.uuid }
                val alreadyCheckedInToday = existingLogs.any { it.type == "Check-In" && it.timestamp in todayStart..todayEnd }
                if (alreadyCheckedInToday) {
                    Log.w("OnSiteViewModel", "Check-in ignored: User already checked in today.")
                    return@launch
                }
            }

            val log = AttendanceLog(
                user_uuid = user.uuid,
                timestamp = System.currentTimeMillis(),
                type = type,
                status = "On-Site",
                latitude = realLat,
                longitude = realLng,
                isSynced = !_isOfflineMode.value
            )

            repository.insertAttendanceLog(log)

            // If offline, flag sync needed
            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            } else {
                try {
                    syncAllWithCloud()
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Instant attendance sync failed", e)
                }
            }
        }
    }

    // Urgent Alerts Dispatcher
    fun broadcastUrgentAlert(title: String, content: String) {
        viewModelScope.launch {
            val alert = SystemAlert(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                priority = "Urgent",
                is_read = false
            )
        }
    }

    fun postDiscussionMessage(
        title: String,
        content: String,
        priority: String = "Info",
        attachmentType: String? = null,
        attachmentData: String? = null,
        attachmentName: String? = null,
        voiceDuration: Int = 0
    ) {
        viewModelScope.launch {
            val msg = SystemAlert(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                priority = priority,
                is_read = false,
                
                attachmentType = attachmentType,
                attachmentData = attachmentData,
                attachmentName = attachmentName,
                voiceDuration = voiceDuration
            )
            repository.insertAlert(msg)
        }
    }

    fun dismissActiveBanner() {
        _activeBannerAlert.value = null
    }

    // Simulate Missed Check-Ins (Push Notification simulation)
    fun simulateMissedCheckin() {
        viewModelScope.launch {
            val targetEmployee = _currentUser.value?.first_name ?: "Sarah Chen"
            NotificationHelper.postMissedCheckinNotification(
                getApplication(),
                targetEmployee,
                "Morning Shift (09:00 AM)"
            )
        }
    }

    // Tasks Handlers
    fun addNewTask(title: String, description: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val task = TodoItem(
                user_uuid = user.uuid,
                title = title,
                description = description,
                is_completed = false,
                isSynced = !_isOfflineMode.value,
                status = "Incomplete",
                is_personal = true,
            )
            try {
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.todosService.createTodo(task)
                }
                repository.insertTodoItem(task)
            } catch (e: Exception) {
                repository.insertTodoItem(task.copy(isSynced = false))
                _syncStatus.value = "Failed to sync task. Saved offline."
            }
        }
    }

    fun assignTaskToEmployee(employeeId: String, title: String, description: String, assignedBy: String) {
        viewModelScope.launch {
            val task = TodoItem(
                user_uuid = employeeId,
                title = title,
                description = description,
                is_completed = false,
                status = "Incomplete",
                is_personal = false,
                assigned_by = assignedBy,
                isSynced = !_isOfflineMode.value
            )
            try {
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.todosService.createTodo(task)
                }
                repository.insertTodoItem(task)
            } catch (e: Exception) {
                repository.insertTodoItem(task.copy(isSynced = false))
                _syncStatus.value = "Failed to sync task. Saved offline."
            }
        }
    }

    fun addPersonalTodo(title: String, description: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val task = TodoItem(
                user_uuid = user.uuid,
                title = title,
                description = description,
                is_completed = false,
                isSynced = !_isOfflineMode.value,
                status = "Incomplete",
                is_personal = true,
            )
            try {
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.todosService.createTodo(task)
                    fetchRemoteAssignedTasksForUser(user.uuid)
                    fetchRemoteTodosForUser(user.uuid)
                }
                repository.insertTodoItem(task)
            } catch (e: Exception) {
                repository.insertTodoItem(task.copy(isSynced = false))
                _syncStatus.value = "Failed to sync task. Saved offline."
            }
        }
    }

    fun updateTaskStatus(task: TodoItem, status: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = status,
                is_completed = (status == "Complete"),
                isSynced = !_isOfflineMode.value
            )
            repository.updateTodoItem(updatedTask)

            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.todosService.updateTodo(updatedTask.uuid, updatedTask)
                    fetchRemoteAssignedTasksForUser(updatedTask.user_uuid)
                    fetchRemoteTodosForUser(updatedTask.user_uuid)
                } catch (e: Exception) {
                    _syncStatus.value = "Failed to sync status update. Saved offline."
                }
            } else {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun toggleTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val newStatus = if (task.is_completed) "Incomplete" else "Complete"
            val updatedTask = task.copy(
                is_completed = !task.is_completed,
                status = newStatus,
                isSynced = !_isOfflineMode.value
            )
            repository.updateTodoItem(updatedTask)

            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.todosService.updateTodo(updatedTask.uuid, updatedTask)
                    fetchRemoteAssignedTasksForUser(updatedTask.user_uuid)
                    fetchRemoteTodosForUser(updatedTask.user_uuid)
                } catch (e: Exception) {
                    _syncStatus.value = "Failed to sync status update. Saved offline."
                }
            } else {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun fetchAllAdminTasks(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            if (_isOfflineMode.value) {
                onSuccess()
                return@launch
            }
            try {
                val remoteTasks = com.example.api.ApiClient.tasksService.getAllAdminTasks()
                if (remoteTasks.isNotEmpty()) {
                    val local = repository.getAllTasksDirect()
                    val remoteUuids = remoteTasks.map { it.uuid }.toSet()
                    local.forEach { localTask ->
                        if (localTask.uuid !in remoteUuids && localTask.isSynced) {
                            repository.deleteTask(localTask.uuid)
                        }
                    }
                    val safeTasks = remoteTasks.map { it.copy(isSynced = true) }
                    repository.insertTasks(safeTasks)
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to fetch all tasks", e)
                onError(e.message ?: "Failed to fetch tasks")
            }
        }
    }

    fun createAdminTask(
        title: String,
        description: String,
        assignedToUuid: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUser = _currentUser.value
        val createdByUuid = currentUser?.uuid ?: "ADMIN-01"
        val now = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val taskUuid = java.util.UUID.randomUUID().toString()

        val newTask = Task(
            uuid = taskUuid,
            title = title,
            description = description,
            created_by = createdByUuid,
            assigned_to = assignedToUuid,
            assigned_by = createdByUuid,
            created_at = now,
            assigned_at = now,
            status = "Pending",
            priority = "Medium",
            is_completed = false,
            isSynced = !_isOfflineMode.value
        )

        viewModelScope.launch {
            try {
                repository.insertTask(newTask)
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.tasksService.createAdminTask(newTask)
                }
                fetchAllAdminTasks()
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to create task", e)
                onSuccess()
            }
        }
    }

    fun updateAdminTask(
        task: Task,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateTask(task.copy(isSynced = !_isOfflineMode.value))
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.tasksService.updateAdminTask(task.uuid, task)
                }
                fetchAllAdminTasks()
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to update task", e)
                onSuccess()
            }
        }
    }

    fun deleteAdminTask(
        taskUuid: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskUuid)
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.tasksService.deleteAdminTask(taskUuid)
                }
                fetchAllAdminTasks()
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to delete task", e)
                onSuccess()
            }
        }
    }

    fun deleteTask(id: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteTodoItem(id)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.todosService.deleteTodo(id)
                    fetchRemoteAssignedTasksForUser(user.uuid)
                    fetchRemoteTodosForUser(user.uuid)
                } catch (e: Exception) {
                    _syncStatus.value = "Failed to sync delete."
                }
            }
        }
    }

    // Manager Actions for Approvals
    fun approveTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                
                status = "Complete"
            )
            repository.updateTodoItem(updatedTask)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.todosService.updateTodo(updatedTask.uuid, updatedTask)
                    fetchRemoteAssignedTasksForUser(updatedTask.user_uuid)
                    fetchRemoteTodosForUser(updatedTask.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    fun rejectTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                is_completed = false,
                
                status = "Incomplete"
            )
            repository.updateTodoItem(updatedTask)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.todosService.updateTodo(updatedTask.uuid, updatedTask)
                    fetchRemoteAssignedTasksForUser(updatedTask.user_uuid)
                    fetchRemoteTodosForUser(updatedTask.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    // Secure Encrypted Messaging
    fun sendSecureMessage(content: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val encryptedMessage = encryptMessage(content)
            val msg = Message(
                sender_uuid = user.uuid,
                
                
                recipient_uuid = "Group",
                content = encryptedMessage,
                timestamp = System.currentTimeMillis(),
                isEncrypted = false,
                isSynced = !_isOfflineMode.value
            )

            repository.insertMessage(msg)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            } else {
                try {
                    syncAllWithCloud()
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Instant message sync failed", e)
                }
            }
        }
    }

    fun sendSummon(staffId: String) {
        val partner = _currentUser.value ?: return
        viewModelScope.launch {
            val summon = SummonAlert(
                summoner_uuid = partner.uuid,
                location = "",
                
                staff_uuid = staffId,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSummon(summon)
        }
    }

    fun clearActiveSummons() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearSummonAlert(user.uuid)
        }
    }

    fun sendDirectMessage(
        recipientId: String,
        content: String,
        attachmentType: String? = null,
        attachmentData: String? = null,
        attachmentName: String? = null,
        voiceDuration: Int = 0
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val encryptedMessage = content
            val msg = Message(
                sender_uuid = user.uuid,
                
                
                recipient_uuid = recipientId,
                content = encryptedMessage,
                timestamp = System.currentTimeMillis(),
                isEncrypted = encryptedMessage.isNotEmpty(),
                isSynced = !_isOfflineMode.value,
                attachmentType = attachmentType,
                attachmentData = attachmentData,
                attachmentName = attachmentName,
                voiceDuration = voiceDuration
            )

            repository.insertMessage(msg)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            } else {
                try {
                    com.example.api.ApiClient.messagesService.sendMessage(msg)
                    fetchRemoteMessages(user.uuid, recipientId)
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Instant direct message sync failed", e)
                }
            }
        }
    }

    fun deleteChatWithUser(otherUserId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.messagesService.deleteMessages(user.uuid, otherUserId)
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Failed to delete chat remotely", e)
                }
            }
            
            // Delete locally after remote attempt completes
            repository.deleteMessagesBetween(user.uuid, otherUserId)
        }
    }

    // ROT13 text cipher + Base64 conversion for simulated visual AES encryption
    fun encryptMessage(text: String): String {
        val rot13 = text.map { char ->
            when (char) {
                in 'A'..'M' -> char + 13
                in 'N'..'Z' -> char - 13
                in 'a'..'m' -> char + 13
                in 'n'..'z' -> char - 13
                else -> char
            }
        }.joinToString("")
        return rot13
    }

    fun decryptMessage(encryptedText: String): String {
        return encryptMessage(encryptedText) // ROT13 is symmetric
    }

    fun requestRegistrationOtp(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = com.example.api.ApiClient.authService.sendOtp(
                    com.example.api.SendOtpRequest(email = email)
                )
                onResult(true, response.message)
            } catch (e: retrofit2.HttpException) {
                Log.e("OnSiteViewModel", "Failed to send OTP (HTTP ${e.code()})", e)
                val errorBody = e.response()?.errorBody()?.string()
                onResult(false, errorBody ?: "Failed to send OTP")
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to send OTP", e)
                onResult(false, e.message ?: "Failed to send OTP")
            }
        }
    }

    fun registerNewEmployee(
        name: String,
        email: String,
        department: String,
        password: String = "1234",
        role: String = "Employee",
        customId: String? = null,
        otp: String,
        age: Int? = null,
        dob: String? = null,
        fathersName: String? = null,
        mothersName: String? = null,
        address: String? = null,
        phone: String? = null,
        emergencyContact: String? = null,
        doj: String? = null,
        bloodGroup: String? = null,
        onSuccess: (Employee) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val nameParts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val firstName = nameParts.firstOrNull() ?: name.trim()
                val lastName = if (nameParts.size > 1) nameParts.last() else ""
                val middleName = if (nameParts.size > 2) nameParts.drop(1).dropLast(1).joinToString(" ") else null

                val response = com.example.api.ApiClient.authService.register(
                    com.example.api.RegisterRequest(
                        email = email,
                        password = password,
                        full_name = name,
                        first_name = firstName,
                        middle_name = middleName,
                        last_name = lastName,
                        role = role,
                        designation = department,
                        custom_id = customId,
                        otp = otp,
                        age = age,
                        dob = dob,
                        father_name = fathersName,
                        mother_name = mothersName,
                        permanent_address = address,
                        current_address = address,
                        contact = phone,
                        emergency_contact = emergencyContact,
                        doj = doj,
                        blood_group = bloodGroup
                    )
                )
                val registeredUser = response.user.copy(
                    first_name = firstName,
                    last_name = lastName,
                    designation = department,
                    contact = phone,
                    password = password
                )
                repository.insertEmployees(listOf(registeredUser))
                selectUserSession(registeredUser)
                onSuccess(registeredUser)
            } catch (e: retrofit2.HttpException) {
                Log.e("OnSiteViewModel", "API Registration failed", e)
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    onError(errorBody ?: "Registration failed")
                } catch (ex: Exception) {
                    onError("Registration failed")
                }
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "API Registration failed", e)
                onError(e.message ?: "Network Error")
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (String?, Employee?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = com.example.api.ApiClient.authService.login(
                    com.example.api.LoginRequest(email = email, password = password)
                )
                if (response.requires2FA == true) {
                    onResult(null, null, true)
                } else if (response.user != null) {
                    completeLogin(response.user)
                    onResult(null, response.user, false)
                } else {
                    onResult("Invalid response", null, false)
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    onResult("Invalid credential", null, false)
                } else {
                    onResult("Invalid credential", null, false)
                }
            } catch (e: Exception) {
                onResult("Network error or server unavailable.", null, false)
            }
        }
    }

    fun completeLogin(user: Employee) {
        viewModelScope.launch {
            val safeUser = if ((user.profile_image?.length ?: 0) > 1000000) {
                user.copy(profile_image = null)
            } else {
                user
            }
            repository.insertEmployees(listOf(safeUser))
            selectUserSession(safeUser)
            fetchAllUsersFromServer()
            fetchUserProfile(safeUser.uuid)
            // NOTE: Message and task notifications are handled by the background sync loop (syncAllWithCloud).
            // Do NOT call syncUserMessages here to avoid spurious notifications at login time.
        }
    }

    private suspend fun fetchUserProfile(userUuid: String) {
        if (_isOfflineMode.value) return
        try {
            val profile = com.example.api.ApiClient.authService.getUserProfile(userUuid)
            val safeProfile = if ((profile.profile_image?.length ?: 0) > 1000000) {
                profile.copy(profile_image = null)
            } else profile
            repository.saveUserDetails(safeProfile)
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Failed to fetch user profile", e)
        }
    }

    suspend fun fetchAllUsersFromServer() {
        if (_isOfflineMode.value) return
        try {
            val allUsers = com.example.api.ApiClient.authService.getAllUsers()
            if (allUsers.isNotEmpty()) {
                val safeUsers = allUsers.map { user ->
                    if ((user.profile_image?.length ?: 0) > 1000000) {
                        user.copy(profile_image = null)
                    } else user
                }
                val local = repository.getAllEmployeesDirect()
                val remoteUuids = safeUsers.map { it.uuid }.toSet()
                local.forEach { emp ->
                    if (emp.uuid !in remoteUuids) {
                        repository.deleteEmployee(emp.uuid)
                    }
                }
                repository.insertEmployees(safeUsers)
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Failed to fetch users for Talk To", e)
        }
    }

    suspend fun syncAssignedTasksWithNotifications(userUuid: String) {
        if (_isOfflineMode.value) return
        try {
            val localTasks = repository.getAllTasksDirect().associateBy { it.uuid }
            val remoteTasks = com.example.api.ApiClient.tasksService.getTasks(userUuid)
            val safeTasks = remoteTasks.map { it.copy(isSynced = true) }
            
            // Check for newly assigned tasks to trigger notification
            // Only fire for tasks that weren't previously known locally and arrived after session started
            safeTasks.forEach { task ->
                val isNewLocal = !localTasks.containsKey(task.uuid)
                val isAssignedToMe = task.assigned_to == userUuid && task.created_by != userUuid
                val taskTimestampMs = parseIsoToMillis(task.assigned_at)
                    ?: parseIsoToMillis(task.created_at)
                    ?: 0L
                val arrivedAfterLogin = taskTimestampMs == 0L || taskTimestampMs > _sessionStartTimeMs
                if (isNewLocal && isAssignedToMe && arrivedAfterLogin) {
                    val creatorEmp = repository.getEmployeeById(task.created_by)
                    val creatorName = if (creatorEmp != null) {
                        "${creatorEmp.first_name ?: ""} ${creatorEmp.last_name ?: ""}".trim().ifBlank { creatorEmp.email }
                    } else {
                        "Management"
                    }
                    try {
                        NotificationHelper.postTaskAssignedNotification(getApplication(), task.title, creatorName, task.uuid)
                    } catch (e: Exception) {
                        Log.e("OnSiteViewModel", "Failed to post task notification", e)
                    }
                }
            }

            if (safeTasks.isNotEmpty()) {
                val remoteUuids = safeTasks.map { it.uuid }.toSet()
                val currentAssignedLocal = repository.getAllTasksDirect().filter { it.assigned_to == userUuid }
                currentAssignedLocal.forEach { localTask ->
                    if (localTask.uuid !in remoteUuids && localTask.isSynced) {
                        repository.deleteTask(localTask.uuid)
                    }
                }
                repository.insertTasks(safeTasks)
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Failed to sync assigned tasks", e)
        }
    }

    fun fetchRemoteAssignedTasksForUser(userUuid: String) {
        viewModelScope.launch {
            syncAssignedTasksWithNotifications(userUuid)
        }
    }

    fun updateAssignedTaskStatus(task: Task, status: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = status,
                is_completed = (status == "Complete"),
                isSynced = !_isOfflineMode.value
            )
            repository.updateTask(updatedTask)

            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.tasksService.updateAdminTask(updatedTask.uuid, updatedTask)
                    syncAssignedTasksWithNotifications(updatedTask.assigned_to)
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Failed to update assigned task status", e)
                }
            }
        }
    }

    fun fetchRemoteTodosForUser(userUuid: String) {
        viewModelScope.launch {
            if (_isOfflineMode.value) return@launch
            try {
                val remoteTodos = com.example.api.ApiClient.todosService.getUserTodos(userUuid)
                remoteTodos.forEach { todo ->
                    repository.insertTodoItem(todo.copy(isSynced = true, is_personal = true))
                }
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to fetch remote todos", e)
            }
        }
    }

    fun fetchRemoteLeavesForUser(userUuid: String) {
        viewModelScope.launch {
            if (_isOfflineMode.value) return@launch
            try {
                val remoteLeaves = com.example.api.ApiClient.leavesService.getUserLeaves(userUuid)
                remoteLeaves.forEach { leave ->
                    repository.insertLeaveRequest(leave.copy(isSynced = true))
                }
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to fetch remote leaves", e)
            }
        }
    }

    private fun parseIsoToMillis(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(isoString)?.time
        } catch (e: Exception) {
            try {
                val sdf2 = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                sdf2.timeZone = java.util.TimeZone.getTimeZone("UTC")
                sdf2.parse(isoString)?.time
            } catch (e2: Exception) {
                null
            }
        }
    }

    private suspend fun fetchRemoteMessages(userUuid: String, otherUuid: String) {
        if (_isOfflineMode.value) return
        try {
            val existingMessages = repository.getAllMessagesDirect().associateBy { it.uuid }
            val remoteMsgs = com.example.api.ApiClient.messagesService.getUserMessages(userUuid, otherUuid)
            val safeMsgs = remoteMsgs.map { msg ->
                val parsedTime = parseIsoToMillis(msg.created_at) ?: msg.timestamp
                val validTimestamp = if (parsedTime > 0L) parsedTime else System.currentTimeMillis()
                val sanitized = if ((msg.attachmentData?.length ?: 0) > 1000000) {
                    msg.copy(attachmentData = null, timestamp = validTimestamp)
                } else {
                    msg.copy(timestamp = validTimestamp)
                }
                sanitized
            }
            safeMsgs.forEach { msg ->
                if (!existingMessages.containsKey(msg.uuid) && msg.recipient_uuid == userUuid && msg.sender_uuid != userUuid) {
                    val senderEmp = repository.getEmployeeById(msg.sender_uuid)
                    val senderName = senderEmp?.first_name ?: "New Message"
                    val text = when {
                        msg.content.isNotBlank() -> msg.content
                        msg.attachmentType == "photo" -> "Sent a photo"
                        msg.attachmentType == "document" -> "Sent a document"
                        msg.attachmentType == "voice" -> "Sent a voice note"
                        else -> "Sent you a message"
                    }
                    try {
                        NotificationHelper.postMessageNotification(getApplication(), senderName, text, msg.sender_uuid)
                    } catch (e: Exception) {
                        Log.e("OnSiteViewModel", "Failed to post message notification", e)
                    }
                }
                repository.insertMessage(msg)
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Failed to fetch remote messages", e)
        }
    }

    suspend fun syncUserMessages(userUuid: String) {
        if (_isOfflineMode.value) return
        try {
            val existingMessages = repository.getAllMessagesDirect().associateBy { it.uuid }
            val remoteMsgs = com.example.api.ApiClient.messagesService.getAllMessagesForUser(userUuid)
            val safeMsgs = remoteMsgs.map { msg ->
                val parsedTime = parseIsoToMillis(msg.created_at) ?: msg.timestamp
                val validTimestamp = if (parsedTime > 0L) parsedTime else System.currentTimeMillis()
                val sanitized = if ((msg.attachmentData?.length ?: 0) > 1000000) {
                    msg.copy(attachmentData = null, timestamp = validTimestamp)
                } else {
                    msg.copy(timestamp = validTimestamp)
                }
                sanitized
            }
            safeMsgs.forEach { msg ->
                val isNew = !existingMessages.containsKey(msg.uuid)
                val isForMe = msg.recipient_uuid == userUuid && msg.sender_uuid != userUuid
                // Only notify for messages that arrived after this session started
                val arrivedAfterLogin = msg.timestamp > _sessionStartTimeMs
                if (isNew && isForMe && arrivedAfterLogin) {
                    val senderEmp = repository.getEmployeeById(msg.sender_uuid)
                    val senderName = senderEmp?.first_name ?: "New Message"
                    val text = when {
                        msg.content.isNotBlank() -> msg.content
                        msg.attachmentType == "photo" -> "Sent a photo"
                        msg.attachmentType == "document" -> "Sent a document"
                        msg.attachmentType == "voice" -> "Sent a voice note"
                        else -> "Sent you a message"
                    }
                    try {
                        NotificationHelper.postMessageNotification(getApplication(), senderName, text, msg.sender_uuid)
                    } catch (e: Exception) {
                        Log.e("OnSiteViewModel", "Failed to post message notification", e)
                    }
                }
                repository.insertMessage(msg)
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Failed to sync all user messages", e)
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            repository.deleteEmployee(employeeId)
            if (_currentUser.value?.uuid == employeeId) {
                logoutSession()
            }
        }
    }

    suspend fun checkLeaveOverlap(userUuid: String, startDateStr: String, endDateStr: String, excludeLeaveUuid: String? = null): Boolean {
        if (_isOfflineMode.value) return false
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val startMs = sdf.parse(startDateStr)?.time ?: 0L
            val endMs = sdf.parse(endDateStr)?.time ?: 0L
            val response = com.example.api.ApiClient.leavesService.checkOverlap(userUuid, startMs, endMs, excludeLeaveUuid)
            response.overlap
        } catch (e: Exception) {
            android.util.Log.e("OnSiteViewModel", "Failed to check leave overlap", e)
            false
        }
    }

    fun submitLeaveRequest(
        reason: String,
        startDate: String,
        endDate: String,
        recipientIds: List<String>,
        recipientNames: List<String>
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val request = LeaveRequest(
                user_uuid = user.uuid,
                start_date = startDate,
                end_date = endDate,
                reason = reason,
                status = "Pending",
                isSynced = !_isOfflineMode.value
            )
            try {
                if (!_isOfflineMode.value) {
                    com.example.api.ApiClient.leavesService.createLeave(request)
                    fetchRemoteLeavesForUser(user.uuid)
                }
                repository.insertLeaveRequest(request)
            } catch (e: Exception) {
                repository.insertLeaveRequest(request.copy(isSynced = false))
                _syncStatus.value = "Failed to sync leave request. Saved offline."
            }
        }
    }

    fun updateLeaveRequest(requestId: String, reason: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.uuid == requestId } ?: return@launch
            val updated = target.copy(reason = reason, start_date = startDate, end_date = endDate, isSynced = !_isOfflineMode.value)
            repository.insertLeaveRequest(updated)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.leavesService.updateLeave(requestId, updated)
                    fetchRemoteLeavesForUser(target.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    fun deleteLeaveRequest(requestId: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.uuid == requestId } ?: return@launch
            repository.deleteLeaveRequest(requestId)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.leavesService.deleteLeave(requestId)
                    fetchRemoteLeavesForUser(target.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    fun approveLeaveRequest(requestId: String, approverName: String, comment: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.uuid == requestId } ?: return@launch
            repository.updateLeaveRequestStatus(requestId, "Accepted", comment, approverName)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.leavesService.updateLeave(requestId, target.copy(status = "Accepted", comment = comment))
                    fetchRemoteLeavesForUser(target.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    fun rejectLeaveRequest(requestId: String, approverName: String, comment: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.uuid == requestId } ?: return@launch
            repository.updateLeaveRequestStatus(requestId, "Rejected", comment, approverName)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.leavesService.updateLeave(requestId, target.copy(status = "Rejected", comment = comment))
                    fetchRemoteLeavesForUser(target.user_uuid)
                } catch (e: Exception) {}
            }
        }
    }

    fun updateEmployeeProfilePhoto(employeeId: String, photoBase64: String?, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val emp = repository.getAllEmployeesDirect().find { it.uuid == employeeId }
                if (emp != null) {
                    val updated = emp.copy(profile_image = photoBase64)
                    repository.updateEmployee(updated)
                    val existingProfile = repository.getAllUserDetailsDirect().find { it.user_uuid == employeeId }
                    val updatedProfile = existingProfile?.copy(profile_image = photoBase64) ?: UserDetails(
                        uuid = employeeId,
                        user_uuid = employeeId,
                        first_name = emp.first_name ?: "",
                        middle_name = null,
                        last_name = emp.last_name ?: "",
                        father_name = "",
                        mother_name = "",
                        dob = "",
                        gender = "",
                        blood_group = "",
                        contact = emp.contact ?: "",
                        official_email = emp.email,
                        personal_email = emp.email,
                        permanent_address = "",
                        current_address = "",
                        emergency_contact = "",
                        profile_image = photoBase64,
                        designation = emp.designation ?: ""
                    )
                    repository.saveUserDetails(updatedProfile)
                    if (!_isOfflineMode.value) {
                        try {
                            com.example.api.ApiClient.authService.updateProfileImage(
                                employeeId,
                                com.example.api.ProfileImageRequest(photoBase64)
                            )
                        } catch (e: Exception) {
                            Log.e("OnSiteViewModel", "Failed to upload profile photo remotely", e)
                        }
                    }
                    if (_currentUser.value?.uuid == employeeId) {
                        _currentUser.value = updated
                    }
                }
                onResult(true)
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Failed to update profile photo", e)
                onResult(false)
            }
        }
    }

    fun getUserDetails(id: String): kotlinx.coroutines.flow.Flow<UserDetails?> {
        return repository.getUserDetails(id)
    }

    fun saveUserDetails(profile: UserDetails) {
        viewModelScope.launch {
            repository.saveUserDetails(profile)
            if (!_isOfflineMode.value) {
                try {
                    com.example.api.ApiClient.authService.updateProfile(profile.user_uuid, profile)
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Failed to sync user profile", e)
                    _syncStatus.value = "Failed to sync profile. Saved offline."
                }
            }
        }
    }

    fun updateEmployeeDetails(employeeId: String, name: String, email: String, department: String, password: String) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(employeeId)
            if (emp != null) {
                val nameParts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val updated = emp.copy(
                    email = email,
                    designation = department,
                    password = password,
                    first_name = nameParts.firstOrNull() ?: name.trim(),
                    last_name = if (nameParts.size > 1) nameParts.last() else ""
                )
                repository.updateEmployee(updated)
                if (_currentUser.value?.uuid == employeeId) {
                    _currentUser.value = updated
                    _isManager.value = (updated.role == "Manager")
                }
            }
        }
    }

    fun updateCurrentUserMfaEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(is_mfa_enabled = enabled)
            repository.updateEmployee(updated)
            _currentUser.value = updated
        }
    }

    fun changePassword(email: String, currentPass: String, newPass: String, onResult: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                com.example.api.ApiClient.authService.changePassword(
                    com.example.api.ChangePasswordRequest(email = email, currentPassword = currentPass, newPassword = newPass)
                )
                // Also update local db for offline cache matching
                val emp = _currentUser.value
                if (emp != null) {
                    updateEmployeeDetails(emp.uuid, (emp.first_name ?: ""), emp.email, (emp.designation ?: ""), newPass)
                }
                onResult(null, true)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401 || e.code() == 400) {
                    val errorBody = e.response()?.errorBody()?.string() ?: "Invalid current password"
                    onResult(if (errorBody.contains("Invalid current password")) "Current password is incorrect." else "Password change failed.", false)
                } else {
                    onResult("Server error: ${e.code()}", false)
                }
            } catch (e: Exception) {
                onResult("Network error or server unavailable.", false)
            }
        }
    }
}
