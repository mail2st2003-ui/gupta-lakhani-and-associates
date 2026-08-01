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
    val first_name: String? = null,
    val middle_name: String? = null,
    val last_name: String? = null,
    val father_name: String? = null,
    val mother_name: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val blood_group: String? = null,
    val contact: String? = null,
    val official_email: String? = null,
    val personal_email: String? = null,
    val permanent_address: String? = null,
    val current_address: String? = null,
    val emergency_contact: String? = null,
    val profile_image: String? = null,
    val designation: String? = null
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
    val voiceDuration: Int = 0,
    @Json(name = "created_at") val created_at: String? = null
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
    val title: String = "",
    val description: String = "",
    val created_by: String = "",
    val assigned_to: String = "",
    val assigned_by: String = "",
    val created_at: String = "",
    val assigned_at: String = "",
    val status: String = "Pending",
    val priority: String = "Medium",
    val is_completed: Boolean = false,
    val isSynced: Boolean = false
)


