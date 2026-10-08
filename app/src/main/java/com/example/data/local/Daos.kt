package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConversationMessage
import com.example.data.model.DailyTask
import com.example.data.model.EmailMessage
import com.example.data.model.Meeting
import com.example.data.model.TeamMember
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM team_members")
    fun getAllTeamMembers(): Flow<List<TeamMember>>

    @Query("SELECT * FROM team_members")
    suspend fun getAllTeamMembersList(): List<TeamMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMembers(members: List<TeamMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(member: TeamMember)
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY scheduledTimeEpoch ASC")
    fun getAllMeetings(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings ORDER BY scheduledTimeEpoch ASC")
    suspend fun getAllMeetingsList(): List<Meeting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting)

    @Update
    suspend fun updateMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: String)
}

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails ORDER BY timestampEpoch DESC")
    fun getAllEmails(): Flow<List<EmailMessage>>

    @Query("SELECT * FROM emails ORDER BY timestampEpoch DESC")
    suspend fun getAllEmailsList(): List<EmailMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailMessage>)

    @Update
    suspend fun updateEmail(email: EmailMessage)

    @Query("UPDATE emails SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM daily_tasks ORDER BY dueDateEpoch ASC")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks ORDER BY dueDateEpoch ASC")
    suspend fun getAllTasksList(): List<DailyTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_messages ORDER BY timestampEpoch ASC")
    fun getAllMessages(): Flow<List<ConversationMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage)

    @Query("DELETE FROM conversation_messages")
    suspend fun clearHistory()
}
