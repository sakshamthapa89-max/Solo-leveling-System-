package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AgentExecutionResult
import com.example.ai.SathiAgentEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AgentLanguage
import com.example.data.model.ConversationMessage
import com.example.data.model.DailyTask
import com.example.data.model.EmailCategory
import com.example.data.model.EmailMessage
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TeamMember
import com.example.data.model.VoiceGender
import com.example.data.model.VoiceSettings
import com.example.voice.VoiceInputManager
import com.example.voice.VoiceOutputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceTonePreset
import com.example.service.BackgroundTaskLog
import com.example.service.SystemAiBackgroundService
import android.content.Context

enum class AppTab {
    VOICE,
    MEETINGS,
    EMAILS,
    TASKS,
    TEAM
}

class SathiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val teamDao = database.teamDao()
    private val meetingDao = database.meetingDao()
    private val emailDao = database.emailDao()
    private val taskDao = database.taskDao()
    private val conversationDao = database.conversationDao()

    private val voiceInputManager = VoiceInputManager(application)
    private val voiceOutputManager = VoiceOutputManager(application)
    private val agentEngine = SathiAgentEngine()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.VOICE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Voice Settings - English by default as system language
    private val _voiceSettings = MutableStateFlow(
        VoiceSettings(
            selectedVoiceId = "en_female_emily",
            gender = VoiceGender.FEMALE,
            language = AgentLanguage.ENGLISH,
            speechRate = 1.0f,
            pitch = 1.0f,
            tonePreset = VoiceTonePreset.NATURAL,
            autoSpeakResponses = true,
            personaName = "Emily"
        )
    )
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    // Agent Processing & Audio State
    val isListening: StateFlow<Boolean> = voiceInputManager.isListening
    val rmsDb: StateFlow<Float> = voiceInputManager.rmsDb
    val isSpeaking: StateFlow<Boolean> = voiceOutputManager.isSpeaking

    // Background Execution State ("System AI will do work in background")
    val isBackgroundActive: StateFlow<Boolean> = SystemAiBackgroundService.isRunning
    val backgroundLogs: StateFlow<List<BackgroundTaskLog>> = SystemAiBackgroundService.backgroundLogs
    val backgroundWorkStatus: StateFlow<String> = SystemAiBackgroundService.currentWorkStatus

    fun toggleBackgroundService(context: Context) {
        if (isBackgroundActive.value) {
            SystemAiBackgroundService.stopService(context)
        } else {
            SystemAiBackgroundService.startService(context)
        }
    }

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _latestVoiceTranscript = MutableStateFlow("")
    val latestVoiceTranscript: StateFlow<String> = _latestVoiceTranscript.asStateFlow()

    private val _lastActionNotice = MutableStateFlow<String?>(null)
    val lastActionNotice: StateFlow<String?> = _lastActionNotice.asStateFlow()

    // Room Data Streams
    val teamMembers: StateFlow<List<TeamMember>> = teamDao.getAllTeamMembers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val meetings: StateFlow<List<Meeting>> = meetingDao.getAllMeetings()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val emails: StateFlow<List<EmailMessage>> = emailDao.getAllEmails()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dailyTasks: StateFlow<List<DailyTask>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val messages: StateFlow<List<ConversationMessage>> = conversationDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        voiceInputManager.initialize()
    }

    fun selectVoiceProfile(profileId: String) {
        val profile = VoiceCatalog.getProfile(profileId) ?: return
        _voiceSettings.value = _voiceSettings.value.copy(
            selectedVoiceId = profile.id,
            personaName = profile.name,
            gender = profile.gender,
            language = profile.language,
            tonePreset = profile.tonePreset,
            pitch = 1.0f,
            speechRate = 1.0f
        )
    }

    fun setVoiceGender(gender: VoiceGender) {
        val current = _voiceSettings.value
        val profile = VoiceCatalog.getDefaultProfile(current.language, gender)
        _voiceSettings.value = current.copy(
            selectedVoiceId = profile.id,
            gender = gender,
            personaName = profile.name,
            tonePreset = profile.tonePreset
        )
    }

    fun setLanguage(language: AgentLanguage) {
        val current = _voiceSettings.value
        val profile = VoiceCatalog.getDefaultProfile(language, current.gender)
        _voiceSettings.value = current.copy(
            selectedVoiceId = profile.id,
            language = language,
            personaName = profile.name,
            tonePreset = profile.tonePreset
        )
    }

    fun setTonePreset(preset: VoiceTonePreset) {
        _voiceSettings.value = _voiceSettings.value.copy(
            tonePreset = preset
        )
    }

    fun setSpeechRate(rate: Float) {
        _voiceSettings.value = _voiceSettings.value.copy(speechRate = rate)
    }

    fun setPitch(pitch: Float) {
        _voiceSettings.value = _voiceSettings.value.copy(pitch = pitch)
    }

    fun resetVoiceSettings() {
        val current = _voiceSettings.value
        val profile = VoiceCatalog.getProfile(current.selectedVoiceId)
            ?: VoiceCatalog.getDefaultProfile(current.language, current.gender)
        _voiceSettings.value = current.copy(
            pitch = 1.0f,
            speechRate = 1.0f,
            tonePreset = profile.tonePreset
        )
    }

    fun previewVoice(sampleText: String? = null) {
        val settings = _voiceSettings.value
        val textToSpeak = sampleText ?: if (settings.language == AgentLanguage.NEPALI) {
            "नमस्ते! म ${settings.personaName} हुँ। यो मेरो आवाजको नमुना हो। म टिमको कार्य सम्हाल्न तयार छु।"
        } else {
            "Hello! I'm ${settings.personaName}. This is a preview of my voice. I am ready to manage meetings and emails for your team."
        }
        voiceOutputManager.speak(textToSpeak, settings)
    }

    fun toggleAutoSpeak() {
        val current = _voiceSettings.value
        _voiceSettings.value = current.copy(autoSpeakResponses = !current.autoSpeakResponses)
    }

    fun toggleMasterService() {
        val current = _voiceSettings.value
        val newState = !current.isServiceOnline
        _voiceSettings.value = current.copy(isServiceOnline = newState)
        if (!newState) {
            voiceOutputManager.stop()
            voiceInputManager.stopListening()
        }
    }

    fun setThinkingPower(level: com.example.data.model.ThinkingPowerLevel) {
        _voiceSettings.value = _voiceSettings.value.copy(thinkingPower = level)
    }

    // Background Notification Alert Settings
    fun toggleBackgroundAlerts() {
        val current = _voiceSettings.value
        val newState = !current.backgroundAlertsEnabled
        _voiceSettings.value = current.copy(backgroundAlertsEnabled = newState)
        syncAlertPreferences()
    }

    fun toggleAlertSoundOrVibrate() {
        val current = _voiceSettings.value
        val newState = !current.alertSoundOrVibrate
        _voiceSettings.value = current.copy(alertSoundOrVibrate = newState)
        syncAlertPreferences()
    }

    fun toggleAlertOnEmailTriage() {
        val current = _voiceSettings.value
        val newState = !current.alertOnEmailTriage
        _voiceSettings.value = current.copy(alertOnEmailTriage = newState)
        syncAlertPreferences()
    }

    fun toggleAlertOnScheduleSync() {
        val current = _voiceSettings.value
        val newState = !current.alertOnScheduleSync
        _voiceSettings.value = current.copy(alertOnScheduleSync = newState)
        syncAlertPreferences()
    }

    fun toggleAlertOnDailyWork() {
        val current = _voiceSettings.value
        val newState = !current.alertOnDailyWork
        _voiceSettings.value = current.copy(alertOnDailyWork = newState)
        syncAlertPreferences()
    }

    fun sendTestBackgroundAlert(context: Context) {
        SystemAiBackgroundService.sendTestAlert(context)
    }

    private fun syncAlertPreferences() {
        val s = _voiceSettings.value
        SystemAiBackgroundService.alertPreferences.value = com.example.service.BackgroundAlertPreferences(
            alertsEnabled = s.backgroundAlertsEnabled,
            soundOrVibrate = s.alertSoundOrVibrate,
            alertOnEmail = s.alertOnEmailTriage,
            alertOnMeeting = s.alertOnScheduleSync,
            alertOnWork = s.alertOnDailyWork
        )
    }

    // Voice Interaction
    fun startListening() {
        if (!_voiceSettings.value.isServiceOnline) {
            voiceOutputManager.speak(
                "System AI service is currently OFF. Please toggle the master switch ON to activate your personal AI.",
                _voiceSettings.value
            )
            return
        }
        if (isSpeaking.value) {
            voiceOutputManager.stop()
        }
        voiceInputManager.startListening(_voiceSettings.value.language) { recognizedText ->
            processUserVoiceInput(recognizedText)
        }
    }

    fun stopListening() {
        voiceInputManager.stopListening()
    }

    fun processUserVoiceInput(text: String) {
        if (text.isBlank()) return
        if (!_voiceSettings.value.isServiceOnline) {
            voiceOutputManager.speak("System AI service is turned OFF.", _voiceSettings.value)
            return
        }
        _latestVoiceTranscript.value = text

        viewModelScope.launch {
            // Save user message to chat history
            val userMsg = ConversationMessage(
                id = "user_${UUID.randomUUID()}",
                role = "user",
                text = text,
                languageCode = if (_voiceSettings.value.language == AgentLanguage.NEPALI) "ne" else "en",
                timestampEpoch = System.currentTimeMillis()
            )
            conversationDao.insertMessage(userMsg)

            _isThinking.value = true

            val currentTeam = teamDao.getAllTeamMembersList()
            val currentMeetings = meetingDao.getAllMeetingsList()
            val currentEmails = emailDao.getAllEmailsList()
            val currentTasks = taskDao.getAllTasksList()

            val result = agentEngine.processUserSpeech(
                userInput = text,
                currentLanguage = _voiceSettings.value.language,
                existingTeam = currentTeam,
                existingMeetings = currentMeetings,
                existingEmails = currentEmails,
                existingTasks = currentTasks,
                thinkingPower = _voiceSettings.value.thinkingPower
            )

            _isThinking.value = false

            when (result) {
                is AgentExecutionResult.MeetingCreated -> {
                    meetingDao.insertMeeting(result.meeting)
                    _lastActionNotice.value = "Meeting Scheduled: ${result.meeting.title}"
                    val assistantMsg = ConversationMessage(
                        id = "asst_${UUID.randomUUID()}",
                        role = "assistant",
                        text = result.spokenText,
                        languageCode = result.lang,
                        timestampEpoch = System.currentTimeMillis(),
                        actionType = "MEETING_SCHEDULED",
                        actionDetail = "${result.meeting.title} (${result.meeting.attendeeNames})",
                        thoughtProcess = result.thoughtProcess
                    )
                    conversationDao.insertMessage(assistantMsg)
                    speakIfEnabled(result.spokenText)
                }

                is AgentExecutionResult.EmailProcessed -> {
                    emailDao.insertEmail(result.email)
                    _lastActionNotice.value = "Email Drafted: ${result.email.subject}"
                    val assistantMsg = ConversationMessage(
                        id = "asst_${UUID.randomUUID()}",
                        role = "assistant",
                        text = result.spokenText,
                        languageCode = result.lang,
                        timestampEpoch = System.currentTimeMillis(),
                        actionType = "EMAIL_DRAFTED",
                        actionDetail = "To: ${result.email.recipientEmails} | ${result.email.subject}",
                        thoughtProcess = result.thoughtProcess
                    )
                    conversationDao.insertMessage(assistantMsg)
                    speakIfEnabled(result.spokenText)
                }

                is AgentExecutionResult.TaskCreated -> {
                    taskDao.insertTask(result.task)
                    _lastActionNotice.value = "Task Created: ${result.task.title}"
                    val assistantMsg = ConversationMessage(
                        id = "asst_${UUID.randomUUID()}",
                        role = "assistant",
                        text = result.spokenText,
                        languageCode = result.lang,
                        timestampEpoch = System.currentTimeMillis(),
                        actionType = "TASK_CREATED",
                        actionDetail = "${result.task.title} -> ${result.task.assignedToName}",
                        thoughtProcess = result.thoughtProcess
                    )
                    conversationDao.insertMessage(assistantMsg)
                    speakIfEnabled(result.spokenText)
                }

                is AgentExecutionResult.DailyBriefing -> {
                    _lastActionNotice.value = "Team Morning Briefing Generated"
                    val assistantMsg = ConversationMessage(
                        id = "asst_${UUID.randomUUID()}",
                        role = "assistant",
                        text = result.spokenText,
                        languageCode = result.lang,
                        timestampEpoch = System.currentTimeMillis(),
                        actionType = "BRIEFING",
                        actionDetail = "Complete Daily Status Briefing",
                        thoughtProcess = result.thoughtProcess
                    )
                    conversationDao.insertMessage(assistantMsg)
                    speakIfEnabled(result.spokenText)
                }

                is AgentExecutionResult.GeneralAnswer -> {
                    val assistantMsg = ConversationMessage(
                        id = "asst_${UUID.randomUUID()}",
                        role = "assistant",
                        text = result.spokenText,
                        languageCode = result.lang,
                        timestampEpoch = System.currentTimeMillis(),
                        thoughtProcess = result.thoughtProcess
                    )
                    conversationDao.insertMessage(assistantMsg)
                    speakIfEnabled(result.spokenText)
                }
            }
        }
    }

    private fun speakIfEnabled(text: String) {
        if (_voiceSettings.value.autoSpeakResponses) {
            voiceOutputManager.speak(text, _voiceSettings.value)
        }
    }

    fun speakText(text: String) {
        voiceOutputManager.speak(text, _voiceSettings.value)
    }

    fun stopSpeaking() {
        voiceOutputManager.stop()
    }

    // Manual Meeting Operations
    fun scheduleMeeting(title: String, agenda: String, epochTime: Long, duration: Int, attendees: String) {
        viewModelScope.launch {
            val meeting = Meeting(
                id = "meet_${UUID.randomUUID()}",
                title = title,
                agenda = agenda,
                scheduledTimeEpoch = epochTime,
                durationMinutes = duration,
                meetLink = "https://meet.google.com/sat-${UUID.randomUUID().toString().take(4)}-sync",
                attendeeEmails = "team@company.com",
                attendeeNames = attendees,
                status = MeetingStatus.SCHEDULED,
                createdByVoice = false
            )
            meetingDao.insertMeeting(meeting)
            val msg = if (_voiceSettings.value.language == AgentLanguage.NEPALI) {
                "मिटिङ '${title}' तालिकामा थपियो।"
            } else {
                "Meeting '$title' scheduled successfully."
            }
            speakIfEnabled(msg)
        }
    }

    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            meetingDao.deleteMeeting(meeting)
        }
    }

    // Email Operations
    fun sendOrSaveEmail(to: String, subject: String, body: String, isDraft: Boolean = false) {
        viewModelScope.launch {
            val email = EmailMessage(
                id = "email_${UUID.randomUUID()}",
                senderName = "Saksham Thapa",
                senderEmail = "sakshamthapa89@gmail.com",
                recipientEmails = to,
                subject = subject,
                body = body,
                summaryNepali = "सक्षम थापाद्वारा पठाइएको/तयार गरिएको इमेल।",
                summaryEnglish = "Email dispatched/drafted by Saksham Thapa.",
                category = EmailCategory.TEAM,
                isDraft = isDraft,
                timestampEpoch = System.currentTimeMillis()
            )
            emailDao.insertEmail(email)
            val msg = if (_voiceSettings.value.language == AgentLanguage.NEPALI) {
                if (isDraft) "इमेल ड्राफ्ट सुरक्षित गरियो।" else "इमेल सफलतापूर्वक पठाइयो।"
            } else {
                if (isDraft) "Email draft saved." else "Email sent successfully."
            }
            speakIfEnabled(msg)
        }
    }

    fun markEmailAsRead(id: String) {
        viewModelScope.launch {
            emailDao.markAsRead(id)
        }
    }

    fun deleteEmail(id: String) {
        viewModelScope.launch {
            emailDao.deleteEmailById(id)
        }
    }

    // Task Operations
    fun addTask(title: String, description: String, assigneeName: String, priority: TaskPriority) {
        viewModelScope.launch {
            val task = DailyTask(
                id = "task_${UUID.randomUUID()}",
                title = title,
                description = description,
                assignedToName = assigneeName,
                assignedToEmail = "team@company.com",
                priority = priority,
                status = TaskStatus.TODO,
                dueDateEpoch = System.currentTimeMillis() + 86400_000L,
                createdByVoice = false
            )
            taskDao.insertTask(task)
        }
    }

    fun toggleTaskStatus(task: DailyTask) {
        viewModelScope.launch {
            val nextStatus = when (task.status) {
                TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                TaskStatus.COMPLETED -> TaskStatus.TODO
            }
            taskDao.updateTask(task.copy(status = nextStatus))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskDao.deleteTaskById(taskId)
        }
    }

    // Morning Briefing Audio Action
    fun playDailyBriefing() {
        processUserVoiceInput(
            if (_voiceSettings.value.language == AgentLanguage.NEPALI)
                "आजको बिहानको ब्रिफिङ र तालिका सुनाउ"
            else
                "Give me today's team morning standup briefing"
        )
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            conversationDao.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputManager.destroy()
        voiceOutputManager.shutdown()
    }
}
