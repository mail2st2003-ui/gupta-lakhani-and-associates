package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Employees
    @Query("SELECT * FROM employees")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesDirect(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("DELETE FROM employees")
    suspend fun clearEmployeesTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    // Attendance Logs
    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllAttendanceLogsFlow(): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    suspend fun getAllAttendanceLogsDirect(): List<AttendanceLog>

    @Query("SELECT * FROM attendance_logs WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getAttendanceLogsForEmployeeFlow(employeeId: String): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLog): Long

    @Query("UPDATE attendance_logs SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAttendanceLogsSynced()

    // Todo Items
    @Query("SELECT * FROM todo_items ORDER BY timestamp DESC")
    fun getAllTodoItemsFlow(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items ORDER BY timestamp DESC")
    suspend fun getAllTodoItemsDirect(): List<TodoItem>

    @Query("SELECT * FROM todo_items WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getTodoItemsForEmployeeFlow(employeeId: String): Flow<List<TodoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodoItem(item: TodoItem)

    @Update
    suspend fun updateTodoItem(item: TodoItem)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoItemById(id: Int)

    @Query("UPDATE todo_items SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markTodoItemsSynced()

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesDirect(): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("UPDATE messages SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markMessagesSynced()

    // System Alerts
    @Query("SELECT * FROM system_alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<SystemAlert>>

    @Query("SELECT * FROM system_alerts ORDER BY timestamp DESC")
    suspend fun getAllAlertsDirect(): List<SystemAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SystemAlert)

    @Query("UPDATE system_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Int)

    // Leave Requests
    @Query("SELECT * FROM leave_requests ORDER BY timestamp DESC")
    fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests ORDER BY timestamp DESC")
    suspend fun getAllLeaveRequestsDirect(): List<LeaveRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest)

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequest)

    // Summons
    @Query("SELECT * FROM summons WHERE staffId = :staffId AND isCleared = 0")
    fun getActiveSummonsForStaffFlow(staffId: String): Flow<List<SummonAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummon(summon: SummonAlert)

    @Query("UPDATE summons SET isCleared = 1 WHERE staffId = :staffId")
    suspend fun clearSummonsForStaff(staffId: String)

    // Detailed Profile queries
    @Query("SELECT * FROM detailed_profiles WHERE id = :id")
    fun getDetailedProfileFlow(id: String): Flow<DetailedProfile?>

    @Query("SELECT * FROM detailed_profiles")
    suspend fun getAllDetailedProfilesDirect(): List<DetailedProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailedProfile(profile: DetailedProfile)
}

@Database(
    entities = [Employee::class, AttendanceLog::class, TodoItem::class, Message::class, SystemAlert::class, LeaveRequest::class, SummonAlert::class, DetailedProfile::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "onsite_attendance_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class OnSiteRepository(private val appDao: AppDao) {
    val allEmployees: Flow<List<Employee>> = appDao.getAllEmployeesFlow()
    val allAttendanceLogs: Flow<List<AttendanceLog>> = appDao.getAllAttendanceLogsFlow()
    val allTodoItems: Flow<List<TodoItem>> = appDao.getAllTodoItemsFlow()
    val allMessages: Flow<List<Message>> = appDao.getAllMessagesFlow()
    val allAlerts: Flow<List<SystemAlert>> = appDao.getAllAlertsFlow()
    val allLeaveRequests: Flow<List<LeaveRequest>> = appDao.getAllLeaveRequestsFlow()

    suspend fun getAllEmployeesDirect(): List<Employee> = appDao.getAllEmployeesDirect()
    suspend fun getAllAttendanceLogsDirect(): List<AttendanceLog> = appDao.getAllAttendanceLogsDirect()
    suspend fun getAllTodoItemsDirect(): List<TodoItem> = appDao.getAllTodoItemsDirect()
    suspend fun getAllMessagesDirect(): List<Message> = appDao.getAllMessagesDirect()
    suspend fun getAllAlertsDirect(): List<SystemAlert> = appDao.getAllAlertsDirect()
    suspend fun getAllLeaveRequestsDirect(): List<LeaveRequest> = appDao.getAllLeaveRequestsDirect()
    suspend fun getAllDetailedProfilesDirect(): List<DetailedProfile> = appDao.getAllDetailedProfilesDirect()

    fun getAttendanceLogsForEmployee(employeeId: String): Flow<List<AttendanceLog>> {
        return appDao.getAttendanceLogsForEmployeeFlow(employeeId)
    }

    fun getTodoItemsForEmployee(employeeId: String): Flow<List<TodoItem>> {
        return appDao.getTodoItemsForEmployeeFlow(employeeId)
    }

    suspend fun getEmployeeById(id: String): Employee? {
        return appDao.getEmployeeById(id)
    }

    suspend fun deleteEmployeeById(id: String) {
        appDao.deleteEmployeeById(id)
    }

    suspend fun clearEmployeesTable() {
        appDao.clearEmployeesTable()
    }

    suspend fun insertEmployees(employees: List<Employee>) {
        appDao.insertEmployees(employees)
    }

    suspend fun updateEmployee(employee: Employee) {
        appDao.updateEmployee(employee)
    }

    suspend fun insertAttendanceLog(log: AttendanceLog): Long {
        return appDao.insertAttendanceLog(log)
    }

    suspend fun insertTodoItem(item: TodoItem) {
        appDao.insertTodoItem(item)
    }

    suspend fun updateTodoItem(item: TodoItem) {
        appDao.updateTodoItem(item)
    }

    suspend fun deleteTodoItemById(id: Int) {
        appDao.deleteTodoItemById(id)
    }

    suspend fun insertMessage(message: Message) {
        appDao.insertMessage(message)
    }

    suspend fun insertAlert(alert: SystemAlert) {
        appDao.insertAlert(alert)
    }

    suspend fun markAlertAsRead(id: Int) {
        appDao.markAlertAsRead(id)
    }

    suspend fun insertLeaveRequest(request: LeaveRequest) {
        appDao.insertLeaveRequest(request)
    }

    suspend fun updateLeaveRequest(request: LeaveRequest) {
        appDao.updateLeaveRequest(request)
    }

    suspend fun syncOfflineData() {
        // Simulates syncing with a cloud back-end by updating the 'isSynced' fields
        appDao.markAttendanceLogsSynced()
        appDao.markTodoItemsSynced()
        appDao.markMessagesSynced()
    }

    fun getActiveSummonsForStaff(staffId: String): Flow<List<SummonAlert>> {
        return appDao.getActiveSummonsForStaffFlow(staffId)
    }

    suspend fun insertSummon(summon: SummonAlert) {
        appDao.insertSummon(summon)
    }

    suspend fun clearSummonsForStaff(staffId: String) {
        appDao.clearSummonsForStaff(staffId)
    }

    fun getDetailedProfileFlow(id: String): Flow<DetailedProfile?> {
        return appDao.getDetailedProfileFlow(id)
    }

    suspend fun insertDetailedProfile(profile: DetailedProfile) {
        appDao.insertDetailedProfile(profile)
    }
}
