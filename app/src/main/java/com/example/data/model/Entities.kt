package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val avatarColorHex: String = "#3B82F6",
    val preferredLanguage: String = "Nepali",
    val isAvailable: Boolean = true,
    val phone: String = "+977 9800000000"
)

enum class MeetingStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey
    val id: String,
    val title: String,
    val agenda: String,
    val scheduledTimeEpoch: Long,
    val durationMinutes: Int = 30,
    val meetLink: String = "https://meet.google.com/abc-defg-hij",
    val attendeeEmails: String, // Comma-separated or JSON list
    val attendeeNames: String,
    val status: MeetingStatus = MeetingStatus.SCHEDULED,
    val createdByVoice: Boolean = true
)

enum class EmailCategory {
    PRIORITY,
    TEAM,
    CLIENT,
    INVITATION
}

@Entity(tableName = "emails")
data class EmailMessage(
    @PrimaryKey
    val id: String,
    val senderName: String,
    val senderEmail: String,
    val recipientEmails: String,
    val subject: String,
    val body: String,
    val summaryNepali: String,
    val summaryEnglish: String,
    val category: EmailCategory = EmailCategory.TEAM,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isDraft: Boolean = false,
    val timestampEpoch: Long = System.currentTimeMillis()
)

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val assignedToName: String,
    val assignedToEmail: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val dueDateEpoch: Long = System.currentTimeMillis() + 86400000L,
    val createdByVoice: Boolean = true
)

@Entity(tableName = "conversation_messages")
data class ConversationMessage(
    @PrimaryKey
    val id: String,
    val role: String, // "user" or "assistant"
    val text: String,
    val languageCode: String = "en", // "en" or "ne"
    val timestampEpoch: Long = System.currentTimeMillis(),
    val actionType: String? = null, // "MEETING_SCHEDULED", "EMAIL_DRAFTED", "TASK_CREATED", "BRIEFING"
    val actionDetail: String? = null,
    val thoughtProcess: String? = null // High thinking power cognitive chain-of-thought
)
