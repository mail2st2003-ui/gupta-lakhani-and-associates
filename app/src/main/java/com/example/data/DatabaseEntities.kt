package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // "Employee", "Manager"
    val email: String,
    val department: String,
    val status: String = "Absent", // "Present", "Absent", "Late"
    val lastCheckedIn: Long? = null,
    val password: String = "1234",
    val profilePhoto: String? = null
)

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val timestamp: Long,
    val type: String, // "Check-In", "Check-Out"
    val status: String, // "On-Site", "Out-of-Bounds", "Late"
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean = false
)

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val title: String,
    val description: String,
    val priority: String, // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val isApproved: Boolean = false, // Manager can approve high priority tasks
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val status: String = "Incomplete", // "Incomplete", "Complete", "Have a Doubt"
    val isPersonal: Boolean = false,
    val assignedBy: String = ""
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val recipientId: String, // "Group" (all staff) or specific employee ID
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val isSynced: Boolean = false,
    val attachmentType: String? = null, // "voice", "photo", "document"
    val attachmentData: String? = null, // Base64 or content description/text
    val attachmentName: String? = null, // e.g. "Voice_Note.mp3", "doc.pdf"
    val voiceDuration: Int = 0 // duration in seconds if voice
)

@Entity(tableName = "summons")
data class SummonAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: String,
    val partnerName: String,
    val staffId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCleared: Boolean = false
)

@Entity(tableName = "system_alerts")
data class SystemAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String, // "Urgent", "Info"
    val isRead: Boolean = false,
    val senderName: String = "Management",
    val attachmentType: String? = null, // "voice", "photo", "document"
    val attachmentData: String? = null, // Base64 or content description/text
    val attachmentName: String? = null, // e.g. "Voice_Note.mp3", "doc.pdf"
    val voiceDuration: Int = 0 // duration in seconds if voice
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val employeeRole: String,
    val recipientIds: String, // e.g. "CA-GU-01" or "CA-GU-01,CA-LA-02" or "All"
    val recipientNames: String, // e.g. "CA Anuj Gupta" or "CA Anuj Gupta, CA Meera Lakhani" or "All CAs"
    val reason: String,
    val startDate: String,
    val endDate: String,
    val status: String = "Pending", // "Pending", "Accepted", "Rejected"
    val timestamp: Long = System.currentTimeMillis(),
    val responseComment: String = "",
    val respondedBy: String = ""
)

@Entity(tableName = "detailed_profiles")
data class DetailedProfile(
    @PrimaryKey val id: String,
    val age: Int,
    val dob: String,
    val fathersName: String,
    val mothersName: String,
    val address: String,
    val email: String,
    val phone: String,
    val emergencyContact: String,
    val doj: String,
    val bloodGroup: String
)

