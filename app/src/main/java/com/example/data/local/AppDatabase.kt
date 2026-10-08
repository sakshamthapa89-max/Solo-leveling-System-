package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ConversationMessage
import com.example.data.model.DailyTask
import com.example.data.model.EmailCategory
import com.example.data.model.EmailMessage
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TeamMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TeamMember::class,
        Meeting::class,
        EmailMessage::class,
        DailyTask::class,
        ConversationMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun meetingDao(): MeetingDao
    abstract fun emailDao(): EmailDao
    abstract fun taskDao(): TaskDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sathi_team_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val oneHour = 3600_000L
            val oneDay = 86400_000L

            // 1. Initial Team Members
            val initialTeam = listOf(
                TeamMember(
                    id = "team_1",
                    name = "Saksham Thapa",
                    email = "sakshamthapa89@gmail.com",
                    role = "Team Lead & Product Manager",
                    avatarColorHex = "#3B82F6",
                    preferredLanguage = "Nepali / English",
                    isAvailable = true,
                    phone = "+977 9841234567"
                ),
                TeamMember(
                    id = "team_2",
                    name = "Pooja Karki",
                    email = "pooja.karki@company.com",
                    role = "Senior Frontend & UI Engineer",
                    avatarColorHex = "#EC4899",
                    preferredLanguage = "Nepali",
                    isAvailable = true,
                    phone = "+977 9851098765"
                ),
                TeamMember(
                    id = "team_3",
                    name = "Aarav Sharma",
                    email = "aarav.sharma@company.com",
                    role = "Backend & Cloud Architect",
                    avatarColorHex = "#10B981",
                    preferredLanguage = "English",
                    isAvailable = true,
                    phone = "+977 9812345678"
                ),
                TeamMember(
                    id = "team_4",
                    name = "Rohan Shrestha",
                    email = "rohan.shrestha@company.com",
                    role = "QA & DevOps Lead",
                    avatarColorHex = "#F59E0B",
                    preferredLanguage = "Nepali",
                    isAvailable = false,
                    phone = "+977 9860112233"
                )
            )
            database.teamDao().insertTeamMembers(initialTeam)

            // 2. Initial Scheduled Meetings
            val initialMeetings = listOf(
                Meeting(
                    id = "meet_1",
                    title = "Team Daily Standup & Sprint Sync",
                    agenda = "Review yesterday's progress, sprint blockers, and deployment timeline.",
                    scheduledTimeEpoch = now + (2 * oneHour), // 2 hours from now
                    durationMinutes = 30,
                    meetLink = "https://meet.google.com/npl-team-sync",
                    attendeeEmails = "sakshamthapa89@gmail.com, pooja.karki@company.com, aarav.sharma@company.com",
                    attendeeNames = "Saksham, Pooja, Aarav",
                    status = MeetingStatus.SCHEDULED,
                    createdByVoice = true
                ),
                Meeting(
                    id = "meet_2",
                    title = "Architecture Review: Voice AI Integration",
                    agenda = "Evaluate Gemini low-latency audio model and offline speech pipeline in Nepali.",
                    scheduledTimeEpoch = now + oneDay + (4 * oneHour), // Tomorrow afternoon
                    durationMinutes = 45,
                    meetLink = "https://meet.google.com/arc-voice-eval",
                    attendeeEmails = "sakshamthapa89@gmail.com, aarav.sharma@company.com",
                    attendeeNames = "Saksham, Aarav",
                    status = MeetingStatus.SCHEDULED,
                    createdByVoice = false
                )
            )
            for (m in initialMeetings) {
                database.meetingDao().insertMeeting(m)
            }

            // 3. Initial Team Emails
            val initialEmails = listOf(
                EmailMessage(
                    id = "email_1",
                    senderName = "Saksham Thapa",
                    senderEmail = "sakshamthapa89@gmail.com",
                    recipientEmails = "team@company.com",
                    subject = "Q4 Product Launch & AI Voice Feature Rollout",
                    body = "Hi team, we are kicking off the bilingual Nepali-English voice assistant sprint. Please review the architecture doc and schedule meetings with our core developers this week.",
                    summaryNepali = "Q4 प्रोडक्ट लन्च र नेपाली-अंग्रेजी भ्वाइस असिस्टेन्ट फिचरको तयारी सुरु भएको छ। आर्किटेक्चर डकुमेन्ट हेर्न अनुरोध।",
                    summaryEnglish = "Kickoff of bilingual voice assistant sprint. Team lead requests reviewing the architecture doc and scheduling meetings.",
                    category = EmailCategory.PRIORITY,
                    isRead = false,
                    isStarred = true,
                    timestampEpoch = now - (30 * 60_000L) // 30 mins ago
                ),
                EmailMessage(
                    id = "email_2",
                    senderName = "Pooja Karki",
                    senderEmail = "pooja.karki@company.com",
                    recipientEmails = "sakshamthapa89@gmail.com",
                    subject = "UI Components Ready for Android Emulator Preview",
                    body = "The Jetpack Compose design tokens, waveform animations, and female/male voice selector components are tested and ready for review.",
                    summaryNepali = "कम्पोज डिजाइन टोकन्स, वेभफर्म एनिमेसन र भ्वाइस सेलेक्टर कम्पोनेन्ट्स तयार छन्।",
                    summaryEnglish = "Compose design tokens, waveform animations, and voice selector components are completed and ready for review.",
                    category = EmailCategory.TEAM,
                    isRead = false,
                    isStarred = false,
                    timestampEpoch = now - (2 * oneHour)
                ),
                EmailMessage(
                    id = "email_3",
                    senderName = "Himalayan Tech Client",
                    senderEmail = "partners@himalayantech.np",
                    recipientEmails = "sakshamthapa89@gmail.com",
                    subject = "Meeting Request: AI Automation Partnership in Nepal",
                    body = "Namaste Saksham, we would love to discuss integrating your Nepali speech agent for our customer support workflows. Could we set up a 30-min call this Friday?",
                    summaryNepali = "हिमालयन टेकले नेपाली भ्वाइस एजेन्ट पार्टनरसिपको लागि यो शुक्रबार ३० मिनेटको कलको प्रस्ताव गरेको छ।",
                    summaryEnglish = "Himalayan Tech proposes a 30-min partnership call this Friday to integrate the Nepali speech agent.",
                    category = EmailCategory.CLIENT,
                    isRead = true,
                    isStarred = true,
                    timestampEpoch = now - (5 * oneHour)
                )
            )
            database.emailDao().insertEmails(initialEmails)

            // 4. Initial Daily Tasks
            val initialTasks = listOf(
                DailyTask(
                    id = "task_1",
                    title = "Confirm audio permissions and test microphone capture",
                    description = "Validate RECORD_AUDIO permission flow and ensure clean audio speech waveform.",
                    assignedToName = "Pooja Karki",
                    assignedToEmail = "pooja.karki@company.com",
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.IN_PROGRESS,
                    dueDateEpoch = now + (6 * oneHour)
                ),
                DailyTask(
                    id = "task_2",
                    title = "Deploy Gemini API endpoints for schedule & email extraction",
                    description = "Configure gemini-3.5-flash with bilingual system prompt for automatic meeting parsing.",
                    assignedToName = "Aarav Sharma",
                    assignedToEmail = "aarav.sharma@company.com",
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.TODO,
                    dueDateEpoch = now + (12 * oneHour)
                ),
                DailyTask(
                    id = "task_3",
                    title = "Prepare weekly team status report",
                    description = "Compile progress from meeting minutes and pending emails.",
                    assignedToName = "Saksham Thapa",
                    assignedToEmail = "sakshamthapa89@gmail.com",
                    priority = TaskPriority.MEDIUM,
                    status = TaskStatus.TODO,
                    dueDateEpoch = now + oneDay
                )
            )
            database.taskDao().insertTasks(initialTasks)

            // 5. Initial Welcome Conversation Message
            database.conversationDao().insertMessage(
                ConversationMessage(
                    id = "welcome_1",
                    role = "assistant",
                    text = "Hello! I am System AI - your autonomous team voice assistant. I automatically schedule meetings, manage emails, and coordinate daily tasks in the background while you do other work. You can speak to me in English or Nepali!",
                    languageCode = "en",
                    timestampEpoch = now,
                    actionType = "BRIEFING",
                    actionDetail = "System AI active: Background monitoring and voice assistant enabled."
                )
            )
        }
    }
}
