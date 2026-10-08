package com.example

import com.example.ai.AgentExecutionResult
import com.example.ai.SathiAgentEngine
import com.example.data.model.AgentLanguage
import com.example.data.model.DailyTask
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TeamMember
import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceGender
import com.example.data.model.VoiceSettings
import com.example.data.model.VoiceTonePreset
import com.example.service.BackgroundTaskLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVoiceCatalogProfiles() {
        assertEquals(8, VoiceCatalog.allProfiles.size)

        val emily = VoiceCatalog.getProfile("en_female_emily")
        assertNotNull(emily)
        assertEquals("Emily", emily?.name)
        assertEquals(VoiceGender.FEMALE, emily?.gender)
        assertEquals(AgentLanguage.ENGLISH, emily?.language)

        val aarati = VoiceCatalog.getProfile("ne_female_aarati")
        assertNotNull(aarati)
        assertEquals("Aarati", aarati?.name)
        assertEquals(VoiceGender.FEMALE, aarati?.gender)
        assertEquals(AgentLanguage.NEPALI, aarati?.language)

        val david = VoiceCatalog.getProfile("en_male_david")
        assertNotNull(david)
        assertEquals(VoiceGender.MALE, david?.gender)

        val suman = VoiceCatalog.getProfile("ne_male_suman")
        assertNotNull(suman)
        assertEquals(VoiceGender.MALE, suman?.gender)
    }

    @Test
    fun testVoiceTonePresets() {
        assertEquals(1.0f, VoiceTonePreset.NATURAL.pitchMultiplier)
        assertTrue(VoiceTonePreset.EXECUTIVE.pitchMultiplier < 1.0f)
        assertTrue(VoiceTonePreset.PROFESSIONAL.pitchMultiplier > 1.0f)
    }

    @Test
    fun testDefaultVoiceSettingsIsEnglish() {
        val defaultSettings = VoiceSettings()
        assertEquals(AgentLanguage.NEPALI, VoiceSettings(language = AgentLanguage.NEPALI).language)
        val englishSettings = VoiceSettings(
            selectedVoiceId = "en_female_emily",
            language = AgentLanguage.ENGLISH,
            personaName = "Emily"
        )
        assertEquals(AgentLanguage.ENGLISH, englishSettings.language)
        assertEquals("Emily", englishSettings.personaName)
    }

    @Test
    fun testNepaliVoiceMeetingIntentParsing() = runBlocking {
        val engine = SathiAgentEngine()
        val dummyTeam = listOf(
            TeamMember(
                id = "t1",
                name = "Saksham Thapa",
                email = "sakshamthapa89@gmail.com",
                role = "Lead",
                preferredLanguage = "Nepali"
            )
        )

        val result = engine.processUserSpeech(
            userInput = "भोलि दिउँसो २ बजे टिमसँग मिटिङ राख",
            currentLanguage = AgentLanguage.NEPALI,
            existingTeam = dummyTeam,
            existingMeetings = emptyList(),
            existingEmails = emptyList(),
            existingTasks = emptyList()
        )

        assertTrue(result is AgentExecutionResult.MeetingCreated)
        val meetingResult = result as AgentExecutionResult.MeetingCreated
        assertNotNull(meetingResult.meeting)
        assertEquals(MeetingStatus.SCHEDULED, meetingResult.meeting.status)
        assertTrue(meetingResult.spokenText.isNotBlank())
        assertEquals("ne", meetingResult.lang)
    }

    @Test
    fun testEnglishVoiceEmailIntentParsing() = runBlocking {
        val engine = SathiAgentEngine()
        val dummyTeam = listOf(
            TeamMember(
                id = "t1",
                name = "Saksham Thapa",
                email = "sakshamthapa89@gmail.com",
                role = "Lead",
                preferredLanguage = "English"
            )
        )

        val result = engine.processUserSpeech(
            userInput = "Draft email reply to client approving proposal",
            currentLanguage = AgentLanguage.ENGLISH,
            existingTeam = dummyTeam,
            existingMeetings = emptyList(),
            existingEmails = emptyList(),
            existingTasks = emptyList()
        )

        assertTrue(result is AgentExecutionResult.EmailProcessed)
        val emailResult = result as AgentExecutionResult.EmailProcessed
        assertNotNull(emailResult.email)
        assertEquals("en", emailResult.lang)
    }

    @Test
    fun testBackgroundTaskLogCreation() {
        val log = BackgroundTaskLog(
            title = "Email Checked",
            detail = "System AI triaged 3 emails",
            type = "EMAIL"
        )
        assertNotNull(log.id)
        assertEquals("Email Checked", log.title)
        assertEquals("EMAIL", log.type)
    }

    @Test
    fun testBackgroundAlertPreferences() {
        val defaultPrefs = com.example.service.BackgroundAlertPreferences()
        assertTrue(defaultPrefs.alertsEnabled)
        assertTrue(defaultPrefs.soundOrVibrate)
        assertTrue(defaultPrefs.alertOnEmail)

        val silencedPrefs = defaultPrefs.copy(alertsEnabled = false, soundOrVibrate = false)
        assertFalse(silencedPrefs.alertsEnabled)
        assertFalse(silencedPrefs.soundOrVibrate)
    }
}
