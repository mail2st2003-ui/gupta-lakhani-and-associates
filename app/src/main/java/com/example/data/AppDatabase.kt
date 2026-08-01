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
    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDirect(): List<Task>

    @Query("SELECT * FROM tasks WHERE uuid = :uuid")
    suspend fun getTaskById(uuid: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE uuid = :uuid")
    suspend fun deleteTaskById(uuid: String)

    @Query("SELECT * FROM tasks WHERE assigned_to = :userUuid OR created_by = :userUuid")
    fun getTasksByAssigneeFlow(userUuid: String): Flow<List<Task>>


    @Query("SELECT * FROM employees")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesDirect(): List<Employee>

    @Query("SELECT * FROM employees WHERE uuid = :uuid")
    suspend fun getEmployeeById(uuid: String): Employee?

    @Query("DELETE FROM employees WHERE uuid = :uuid")
    suspend fun deleteEmployeeById(uuid: String)

    @Query("DELETE FROM employees")
    suspend fun clearEmployeesTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllAttendanceLogsFlow(): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    suspend fun getAllAttendanceLogsDirect(): List<AttendanceLog>

    @Query("SELECT * FROM attendance_logs WHERE user_uuid = :user_uuid ORDER BY timestamp DESC")
    fun getAttendanceLogsForEmployeeFlow(user_uuid: String): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLog): Long

    @Query("UPDATE attendance_logs SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAttendanceLogsSynced()

    @Query("SELECT * FROM todos ORDER BY timestamp DESC")
    fun getAllTodoItemsFlow(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos ORDER BY timestamp DESC")
    suspend fun getAllTodoItemsDirect(): List<TodoItem>

    @Query("SELECT * FROM todos WHERE user_uuid = :user_uuid ORDER BY timestamp DESC")
    fun getTodoItemsForEmployeeFlow(user_uuid: String): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos WHERE user_uuid = :user_uuid ORDER BY timestamp DESC")
    suspend fun getTodoItemsForEmployeeDirect(user_uuid: String): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodoItem(item: TodoItem)

    @Update
    suspend fun updateTodoItem(item: TodoItem)

    @Query("DELETE FROM todos WHERE uuid = :uuid")
    suspend fun deleteTodoItemById(uuid: String)

    @Query("UPDATE todos SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markTodoItemsSynced()

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesDirect(): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("UPDATE messages SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markMessagesSynced()

    @Query("SELECT * FROM system_alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<SystemAlert>>

    @Query("SELECT * FROM system_alerts ORDER BY timestamp DESC")
    suspend fun getAllAlertsDirect(): List<SystemAlert>

    @Query("DELETE FROM messages WHERE (sender_uuid = :user1 AND recipient_uuid = :user2) OR (sender_uuid = :user2 AND recipient_uuid = :user1)")
    suspend fun deleteMessagesBetween(user1: String, user2: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemAlerts(alerts: List<SystemAlert>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SystemAlert)

    @Query("UPDATE system_alerts SET is_read = 1 WHERE uuid = :uuid")
    suspend fun markAlertAsRead(uuid: String)

    @Query("SELECT * FROM leave_requests ORDER BY timestamp DESC")
    fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests ORDER BY timestamp DESC")
    suspend fun getAllLeaveRequestsDirect(): List<LeaveRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest)

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequest)

    @Query("DELETE FROM leave_requests WHERE uuid = :uuid")
    suspend fun deleteLeaveRequestById(uuid: String)

    @Query("SELECT * FROM summons WHERE staff_uuid = :staff_uuid AND is_cleared = 0")
    fun getActiveSummonsForStaffFlow(staff_uuid: String): Flow<List<SummonAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummon(summon: SummonAlert)

    @Query("UPDATE summons SET is_cleared = 1 WHERE staff_uuid = :staff_uuid")
    suspend fun clearSummonsForStaff(staff_uuid: String)

    @Query("SELECT * FROM user_details WHERE user_uuid = :user_uuid")
    fun getUserDetailsFlow(user_uuid: String): Flow<UserDetails?>

    @Query("SELECT * FROM user_details")
    suspend fun getAllUserDetailsDirect(): List<UserDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDetails(profile: UserDetails)
}

@Database(
    entities = [Employee::class, UserDetails::class, AttendanceLog::class, TodoItem::class, Message::class, SummonAlert::class, SystemAlert::class, LeaveRequest::class, Task::class],
    version = 17,
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

class OnSiteRepository(private val dao: AppDao) {
    val allTasks = dao.getAllTasksFlow()
    val allEmployees: Flow<List<Employee>> = dao.getAllEmployeesFlow()
    val allAttendanceLogs: Flow<List<AttendanceLog>> = dao.getAllAttendanceLogsFlow()
    val allTodoItems: Flow<List<TodoItem>> = dao.getAllTodoItemsFlow()
    val allMessages: Flow<List<Message>> = dao.getAllMessagesFlow()
    val allAlerts: Flow<List<SystemAlert>> = dao.getAllAlertsFlow()
    val allLeaveRequests: Flow<List<LeaveRequest>> = dao.getAllLeaveRequestsFlow()

    suspend fun insertTask(task: Task) = dao.insertTask(task)
    suspend fun insertTasks(tasks: List<Task>) = dao.insertTasks(tasks)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(uuid: String) = dao.deleteTaskById(uuid)
    suspend fun getAllTasksDirect(): List<Task> = dao.getAllTasksDirect()
    suspend fun getTaskById(uuid: String): Task? = dao.getTaskById(uuid)
    fun getTasksByAssignee(userUuid: String) = dao.getTasksByAssigneeFlow(userUuid)

    suspend fun getAllEmployeesDirect(): List<Employee> = dao.getAllEmployeesDirect()
    suspend fun getEmployeeById(uuid: String): Employee? = dao.getEmployeeById(uuid)
    suspend fun getAllAttendanceLogsDirect(): List<AttendanceLog> = dao.getAllAttendanceLogsDirect()
    suspend fun getAllTodoItemsDirect(): List<TodoItem> = dao.getAllTodoItemsDirect()
    suspend fun getAllMessagesDirect(): List<Message> = dao.getAllMessagesDirect()
    suspend fun getAllAlertsDirect(): List<SystemAlert> = dao.getAllAlertsDirect()
    suspend fun getAllLeaveRequestsDirect(): List<LeaveRequest> = dao.getAllLeaveRequestsDirect()
    suspend fun getAllUserDetailsDirect(): List<UserDetails> = dao.getAllUserDetailsDirect()

    fun getAttendanceLogsForEmployee(user_uuid: String): Flow<List<AttendanceLog>> {
        return dao.getAttendanceLogsForEmployeeFlow(user_uuid)
    }

    fun getTodoItemsForEmployee(user_uuid: String): Flow<List<TodoItem>> {
        return dao.getTodoItemsForEmployeeFlow(user_uuid)
    }
    
    suspend fun getTodoItemsForEmployeeDirect(user_uuid: String): List<TodoItem> {
        return dao.getTodoItemsForEmployeeDirect(user_uuid)
    }

    fun getActiveSummonsForStaff(user_uuid: String): Flow<List<SummonAlert>> {
        return dao.getActiveSummonsForStaffFlow(user_uuid)
    }

    suspend fun insertEmployee(employee: Employee) {
        dao.insertEmployees(listOf(employee))
    }

    suspend fun insertEmployees(employees: List<Employee>) {
        dao.insertEmployees(employees)
    }

    suspend fun updateEmployee(employee: Employee) {
        dao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(user_uuid: String) {
        dao.deleteEmployeeById(user_uuid)
    }

    suspend fun insertAttendanceLog(log: AttendanceLog) {
        dao.insertAttendanceLog(log)
    }

    suspend fun insertTodoItem(item: TodoItem) {
        dao.insertTodoItem(item)
    }

    suspend fun updateTodoItem(item: TodoItem) {
        dao.updateTodoItem(item)
    }

    suspend fun deleteTodoItem(uuid: String) {
        dao.deleteTodoItemById(uuid)
    }

    suspend fun insertMessage(msg: Message) {
        dao.insertMessage(msg)
    }

    suspend fun deleteMessagesBetween(user1: String, user2: String) {
        dao.deleteMessagesBetween(user1, user2)
    }

    suspend fun insertSystemAlerts(alerts: List<SystemAlert>) {
        dao.insertSystemAlerts(alerts)
    }

    suspend fun insertAlert(alert: SystemAlert) {
        dao.insertAlert(alert)
    }

    suspend fun markAlertAsRead(uuid: String) {
        dao.markAlertAsRead(uuid)
    }

    suspend fun insertLeaveRequest(request: LeaveRequest) {
        dao.insertLeaveRequest(request)
    }

    suspend fun updateLeaveRequestStatus(uuid: String, status: String, comment: String = "", manager_uuid: String = "") {
        val request = dao.getAllLeaveRequestsDirect().find { it.uuid == uuid }
        if (request != null) {
            dao.updateLeaveRequest(request.copy(status = status, comment = comment, manager_uuid = manager_uuid))
        }
    }

    suspend fun deleteLeaveRequest(uuid: String) {
        dao.deleteLeaveRequestById(uuid)
    }

    suspend fun insertSummon(summon: SummonAlert) {
        dao.insertSummon(summon)
    }

    suspend fun clearSummonAlert(uuid: String) {
        dao.clearSummonsForStaff(uuid)
    }

    fun getUserDetails(user_uuid: String): Flow<UserDetails?> {
        return dao.getUserDetailsFlow(user_uuid)
    }

    suspend fun saveUserDetails(profile: UserDetails) {
        dao.insertUserDetails(profile)
    }

    suspend fun markAttendanceLogsSynced() {
        dao.markAttendanceLogsSynced()
    }

    suspend fun markTodoItemsSynced() {
        dao.markTodoItemsSynced()
    }

    suspend fun markMessagesSynced() {
        dao.markMessagesSynced()
    }
}
