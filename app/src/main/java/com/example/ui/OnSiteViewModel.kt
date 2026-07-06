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
import com.example.data.LeaveRequest
import com.example.data.SummonAlert
import com.example.data.DetailedProfile
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
    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    private val _isManager = MutableStateFlow(false)
    val isManager: StateFlow<Boolean> = _isManager.asStateFlow()

    // Database Flows
    val employees: StateFlow<List<Employee>>
    val allAttendanceLogs: StateFlow<List<AttendanceLog>>
    val messages: StateFlow<List<Message>>
    val systemAlerts: StateFlow<List<SystemAlert>>
    val allTodoItems: StateFlow<List<TodoItem>>
    val allLeaveRequests: StateFlow<List<LeaveRequest>>
    val activeSummons: StateFlow<List<SummonAlert>>
    
    // Employee-specific Flow
    val currentEmployeeTodoItems: StateFlow<List<TodoItem>>
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

        activeSummons = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getActiveSummonsForStaff(user.id)
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
                repository.getTodoItemsForEmployee(user.id)
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
                repository.getAttendanceLogsForEmployee(user.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            if (savedUserId != null) {
                var employee = repository.getEmployeeById(savedUserId)
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

        // Seed initial tasks
        repository.insertTodoItem(
            TodoItem(
                employeeId = "EMP-RS-54",
                title = "Draft Tax Audit Report",
                description = "Draft the Q1 tax audit report and reconcile the GST ledger balances.",
                priority = "High",
                isCompleted = false
            )
        )
        repository.insertTodoItem(
            TodoItem(
                employeeId = "EMP-RS-54",
                title = "Prepare IT Returns filing",
                description = "Extract the corporate balance sheets and fill out Form ITR-6 draft.",
                priority = "Medium",
                isCompleted = true,
                isApproved = true
            )
        )
        repository.insertTodoItem(
            TodoItem(
                employeeId = "EMP-PP-88",
                title = "Audit Voucher Verification",
                description = "Perform physical verification of cash receipts and cross-check ledger transactions.",
                priority = "High",
                isCompleted = false
            )
        )

        // Seed initial secure chat messages
        repository.insertMessage(
            Message(
                senderId = "CA-GU-01",
                senderName = "CA Anuj Gupta",
                senderRole = "Manager",
                recipientId = "Group",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-PP-88",
                employeeName = "Priya Patel",
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
                employeeId = "EMP-PP-88",
                employeeName = "Priya Patel",
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
                employeeId = "EMP-VS-22",
                employeeName = "Vikram Singh",
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
                employeeId = "EMP-VS-22",
                employeeName = "Vikram Singh",
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
                employeeId = "EMP-RM-19",
                employeeName = "Rohan Mehta",
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
                employeeId = "EMP-RM-19",
                employeeName = "Rohan Mehta",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
                employeeId = "EMP-RS-54",
                employeeName = "Rahul Sharma",
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
        themePrefs.edit().putString("logged_in_user_id", employee.id).apply()
    }

    fun logoutSession() {
        _currentUser.value = null
        _isManager.value = false
        themePrefs.edit().remove("logged_in_user_id").apply()
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
                repository.syncOfflineData()
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
                    val existing = mergedMap[emp.id]
                    if (existing == null) {
                        mergedMap[emp.id] = emp
                    } else {
                        val existingTime = existing.lastCheckedIn ?: 0L
                        val empTime = emp.lastCheckedIn ?: 0L
                        if (empTime > existingTime) {
                            mergedMap[emp.id] = emp
                        } else if (empTime == existingTime) {
                            if (emp.profilePhoto != null && existing.profilePhoto == null) {
                                mergedMap[emp.id] = emp
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
                val merged = allSources.distinctBy { "${it.employeeId}_${it.timestamp}_${it.type}" }
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
                    val key = "${item.employeeId}_${item.timestamp}_${item.title}"
                    val existing = mergedMap[key]
                    if (existing == null) {
                        mergedMap[key] = item
                    } else {
                        if (item.isCompleted || item.isApproved || item.status == "Complete") {
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

        // 4. Messages
        try {
            val local = repository.getAllMessagesDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("messages", Message::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("messages", Message::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<Message>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val merged = allSources.distinctBy { "${it.senderId}_${it.timestamp}" }
                if (merged.size != local.size || merged != local) {
                    merged.forEach { msg ->
                        repository.insertMessage(msg)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("messages", merged, Message::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("messages", merged, Message::class.java)
                    if (!ok) firestoreSuccess = false
                }
            }
        } catch (e: Exception) {
            Log.e("OnSiteViewModel", "Sync messages error", e)
            if (_isFirestoreEnabled.value) firestoreSuccess = false
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

            if (kvdbRemote != null || firestoreRemote != null) {
                val merged = allSources.distinctBy { "${it.senderName}_${it.timestamp}" }
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
                    val key = "${req.employeeId}_${req.timestamp}"
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
            val local = repository.getAllDetailedProfilesDirect()
            val kvdbRemote = CloudSyncHelper.fetchList("detailed_profiles", DetailedProfile::class.java)
            val firestoreRemote = if (_isFirestoreEnabled.value) {
                com.example.data.FirestoreSyncHelper.fetchList("detailed_profiles", DetailedProfile::class.java)
            } else {
                null
            }

            val allSources = mutableListOf<DetailedProfile>()
            allSources.addAll(local)
            if (kvdbRemote != null) allSources.addAll(kvdbRemote)
            if (firestoreRemote != null) allSources.addAll(firestoreRemote)

            if (kvdbRemote != null || firestoreRemote != null) {
                val mergedMap = mutableMapOf<String, DetailedProfile>()
                allSources.forEach { profile ->
                    mergedMap[profile.id] = profile
                }
                val mergedList = mergedMap.values.toList()
                if (mergedList.size != local.size || mergedList != local) {
                    mergedList.forEach { profile ->
                        repository.insertDetailedProfile(profile)
                    }
                }
                if (kvdbRemote != null) {
                    CloudSyncHelper.saveList("detailed_profiles", mergedList, DetailedProfile::class.java)
                }
                if (_isFirestoreEnabled.value) {
                    val ok = com.example.data.FirestoreSyncHelper.saveList("detailed_profiles", mergedList, DetailedProfile::class.java)
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
        val activeLat = realLat ?: _currentSimulatedLocation.value.latitude
        val activeLng = realLng ?: _currentSimulatedLocation.value.longitude
        val distance = calculateDistanceFromGeofence(activeLat, activeLng)
        val isWithinRange = distance <= _geofenceRadius.value

        val status = if (isWithinRange) "On-Site" else "Out-of-Bounds"

        viewModelScope.launch {
            val log = AttendanceLog(
                employeeId = user.id,
                employeeName = user.name,
                timestamp = System.currentTimeMillis(),
                type = type,
                status = status,
                latitude = activeLat,
                longitude = activeLng,
                isSynced = !_isOfflineMode.value
            )

            repository.insertAttendanceLog(log)

            // Update employee's main status block
            val newStatus = if (type == "Check-In") {
                if (isWithinRange) "Present" else "Absent"
            } else {
                "Absent"
            }

            val updatedEmployee = user.copy(
                status = newStatus,
                lastCheckedIn = if (type == "Check-In") System.currentTimeMillis() else user.lastCheckedIn
            )
            repository.updateEmployee(updatedEmployee)
            _currentUser.value = updatedEmployee

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
                isRead = false,
                senderName = _currentUser.value?.name ?: "Management"
            )

            repository.insertAlert(alert)
            _activeBannerAlert.value = alert

            // Push an actual local Android status bar notification!
            NotificationHelper.postUrgentAlertNotification(
                getApplication(),
                title,
                content
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
                isRead = false,
                senderName = _currentUser.value?.name ?: "Anonymous",
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
            val targetEmployee = _currentUser.value?.name ?: "Sarah Chen"
            NotificationHelper.postMissedCheckinNotification(
                getApplication(),
                targetEmployee,
                "Morning Shift (09:00 AM)"
            )
        }
    }

    // Tasks Handlers
    fun addNewTask(title: String, description: String, priority: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val task = TodoItem(
                employeeId = user.id,
                title = title,
                description = description,
                priority = priority,
                isCompleted = false,
                isSynced = !_isOfflineMode.value,
                status = "Incomplete",
                isPersonal = false
            )
            repository.insertTodoItem(task)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun assignTaskToEmployee(employeeId: String, title: String, description: String, priority: String, assignedBy: String) {
        viewModelScope.launch {
            val task = TodoItem(
                employeeId = employeeId,
                title = title,
                description = description,
                priority = priority,
                isCompleted = false,
                isApproved = false,
                isSynced = !_isOfflineMode.value,
                status = "Incomplete",
                isPersonal = false,
                assignedBy = assignedBy
            )
            repository.insertTodoItem(task)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun addPersonalTodo(title: String, description: String, priority: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val task = TodoItem(
                employeeId = user.id,
                title = title,
                description = description,
                priority = priority,
                isCompleted = false,
                isApproved = false,
                isSynced = !_isOfflineMode.value,
                status = "Incomplete",
                isPersonal = true
            )
            repository.insertTodoItem(task)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun updateTaskStatus(task: TodoItem, status: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = status,
                isCompleted = (status == "Complete"),
                isSynced = !_isOfflineMode.value
            )
            repository.updateTodoItem(updatedTask)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun toggleTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) "Incomplete" else "Complete"
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted,
                status = newStatus,
                isSynced = !_isOfflineMode.value
            )
            repository.updateTodoItem(updatedTask)

            if (_isOfflineMode.value) {
                _syncStatus.value = "Pending Sync (Offline Mode)"
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTodoItemById(taskId)
        }
    }

    // Manager Actions for Approvals
    fun approveTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isApproved = true,
                status = "Complete"
            )
            repository.updateTodoItem(updatedTask)
        }
    }

    fun rejectTaskCompletion(task: TodoItem) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = false,
                isApproved = false,
                status = "Incomplete"
            )
            repository.updateTodoItem(updatedTask)
        }
    }

    // Secure Encrypted Messaging
    fun sendSecureMessage(content: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val encryptedMessage = encryptMessage(content)
            val msg = Message(
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                recipientId = "Group",
                content = encryptedMessage,
                timestamp = System.currentTimeMillis(),
                isEncrypted = true,
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
                partnerId = partner.id,
                partnerName = partner.name,
                staffId = staffId,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSummon(summon)
        }
    }

    fun clearActiveSummons() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearSummonsForStaff(user.id)
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
            val encryptedMessage = if (content.isNotEmpty()) encryptMessage(content) else ""
            val msg = Message(
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                recipientId = recipientId,
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
                    syncAllWithCloud()
                } catch (e: Exception) {
                    Log.e("OnSiteViewModel", "Instant direct message sync failed", e)
                }
            }
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

    fun registerNewEmployee(
        name: String,
        email: String,
        department: String,
        password: String = "1234",
        role: String = "Employee",
        customId: String? = null,
        onSuccess: (Employee) -> Unit
    ) {
        viewModelScope.launch {
            val finalId = if (!customId.isNullOrBlank()) {
                customId.trim()
            } else {
                val randomNum = (10..99).random()
                val initials = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
                val cleanInitials = if (initials.length >= 2) initials.take(2) else (initials + "X").take(2)
                if (role == "Manager") "CA-$cleanInitials-$randomNum" else "EMP-$cleanInitials-$randomNum"
            }

            val newEmp = Employee(
                id = finalId,
                name = name,
                role = role,
                email = email,
                department = department,
                status = "Absent",
                lastCheckedIn = null,
                password = password
            )
            repository.insertEmployees(listOf(newEmp))
            selectUserSession(newEmp)
            try {
                syncAllWithCloud()
            } catch (e: Exception) {
                Log.e("OnSiteViewModel", "Instant registration sync failed", e)
            }
            onSuccess(newEmp)
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            repository.deleteEmployeeById(employeeId)
            if (_currentUser.value?.id == employeeId) {
                logoutSession()
            }
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
                employeeId = user.id,
                employeeName = user.name,
                employeeRole = if (user.role == "Manager") "Manager" else "Employee",
                recipientIds = recipientIds.joinToString(","),
                recipientNames = recipientNames.joinToString(", "),
                reason = reason,
                startDate = startDate,
                endDate = endDate,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.insertLeaveRequest(request)
        }
    }

    fun approveLeaveRequest(requestId: Int, approverName: String, comment: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.id == requestId } ?: return@launch
            val updated = target.copy(
                status = "Accepted",
                responseComment = comment,
                respondedBy = approverName
            )
            repository.updateLeaveRequest(updated)
        }
    }

    fun rejectLeaveRequest(requestId: Int, approverName: String, comment: String) {
        viewModelScope.launch {
            val target = allLeaveRequests.value.find { it.id == requestId } ?: return@launch
            val updated = target.copy(
                status = "Rejected",
                responseComment = comment,
                respondedBy = approverName
            )
            repository.updateLeaveRequest(updated)
        }
    }

    fun updateEmployeeProfilePhoto(employeeId: String, photoBase64: String?) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(employeeId)
            if (emp != null) {
                val updated = emp.copy(profilePhoto = photoBase64)
                repository.updateEmployee(updated)
                if (_currentUser.value?.id == employeeId) {
                    _currentUser.value = updated
                }
            }
        }
    }

    fun getDetailedProfileFlow(id: String): kotlinx.coroutines.flow.Flow<DetailedProfile?> {
        return repository.getDetailedProfileFlow(id)
    }

    fun saveDetailedProfile(profile: DetailedProfile) {
        viewModelScope.launch {
            repository.insertDetailedProfile(profile)
        }
    }

    fun updateEmployeeDetails(employeeId: String, name: String, email: String, department: String, password: String) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(employeeId)
            if (emp != null) {
                val updated = emp.copy(
                    name = name,
                    email = email,
                    department = department,
                    password = password,
                    role = if (department == "Partner") "Manager" else "Employee"
                )
                repository.updateEmployee(updated)
                if (_currentUser.value?.id == employeeId) {
                    _currentUser.value = updated
                    _isManager.value = (updated.role == "Manager")
                }
            }
        }
    }
}
