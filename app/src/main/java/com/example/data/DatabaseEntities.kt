package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val uuid: String,
    val email: String,
    val role: String, // "Employee", "Manager"
    @Json(name = "is_mfa_enabled") val is_mfa_enabled: Boolean = false,
    // Note: Android UI sometimes relies on Employee model for display,
    // so we optionally hold details here if joined locally.
    val first_name: String? = null,
    val last_name: String? = null,
    val designation: String? = null,
    val profile_image: String? = null,
    val contact: String? = null,
    val password: String = "1234"
)

@Entity(tableName = "user_details")
data class UserDetails(
    @PrimaryKey val uuid: String,
    val user_uuid: String,
    val first_name: String,
    val middle_name: String?,
    val last_name: String,
    val father_name: String,
    val mother_name: String,
    val dob: String,
    val gender: String,
    val blood_group: String,
    val contact: String,
    val official_email: String,
    val personal_email: String,
    val permanent_address: String,
    val current_address: String,
    val emergency_contact: String = "",
    val profile_image: String?,
    val designation: String
)

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val user_uuid: String,
    val type: String, // "Check-In", "Check-Out"
    val status: String, // "On-Site", "Out-of-Bounds", "Late"
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val user_uuid: String,
    val title: String,
    val description: String,
    val is_completed: Boolean = false,
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis(),
    val is_personal: Boolean = true,
    val assigned_by: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val sender_uuid: String,
    val recipient_uuid: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val isSynced: Boolean = false,
    val attachmentType: String? = null,
    val attachmentData: String? = null,
    val attachmentName: String? = null,
    val voiceDuration: Int = 0
)

@Entity(tableName = "summons")
data class SummonAlert(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val staff_uuid: String,
    val summoner_uuid: String,
    val location: String,
    val urgency: String = "Normal",
    val is_cleared: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_alerts")
data class SystemAlert(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String,
    val is_read: Boolean = false,
    val attachmentType: String? = null,
    val attachmentData: String? = null,
    val attachmentName: String? = null,
    val voiceDuration: Int = 0
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val user_uuid: String,
    val start_date: String,
    val end_date: String,
    val reason: String,
    val status: String = "Pending", // "Pending", "Accepted", "Rejected"
    val timestamp: Long = System.currentTimeMillis(),
    val manager_uuid: String = "",
    val comment: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val created_by_user_uuid: String,
    val assigned_to_user_uuid: String,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "task_details")
data class TaskDetails(
    @PrimaryKey val uuid: String = java.util.UUID.randomUUID().toString(),
    val task_uuid: String,
    val title: String,
    val description: String,
    val status: String = "Pending",
    val priority: String = "Medium",
    val due_date: Long,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
