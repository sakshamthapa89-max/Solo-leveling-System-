package com.example.data.model

enum class VoiceGender {
    FEMALE,
    MALE
}

enum class AgentLanguage(val code: String, val displayName: String, val nativeName: String) {
    NEPALI("ne", "Nepali", "नेपाली"),
    ENGLISH("en", "English", "English")
}

enum class VoiceTonePreset(val label: String, val pitchMultiplier: Float, val rateMultiplier: Float) {
    NATURAL("Natural & Balanced", 1.0f, 1.0f),
    WARM("Warm & Friendly", 0.95f, 0.95f),
    PROFESSIONAL("Professional & Crisp", 1.10f, 1.05f),
    EXECUTIVE("Executive & Deep", 0.82f, 0.92f),
    ENERGETIC("Energetic & Fast", 1.18f, 1.22f),
    CUSTOM("Custom", 1.0f, 1.0f)
}

enum class ThinkingPowerLevel(val label: String, val description: String, val levelTag: String) {
    HIGH("High Thinking Power", "Deep cognitive multi-step reasoning, schedule optimization & strategic problem solving", "high"),
    BALANCED("Balanced Thinking", "Optimal blend of reasoning depth and fast responsiveness", "medium"),
    TURBO("Turbo / Fast", "Instant reflexes and rapid conversational replies", "low")
}

data class VoiceProfile(
    val id: String,
    val name: String,
    val nativeName: String,
    val gender: VoiceGender,
    val language: AgentLanguage,
    val description: String,
    val defaultPitch: Float,
    val defaultRate: Float,
    val avatarEmoji: String,
    val tonePreset: VoiceTonePreset
)

object VoiceCatalog {
    val allProfiles: List<VoiceProfile> = listOf(
        // English Female Voices (Default System)
        VoiceProfile(
            id = "en_female_emily",
            name = "Emily",
            nativeName = "Emily",
            gender = VoiceGender.FEMALE,
            language = AgentLanguage.ENGLISH,
            description = "Natural, conversational & engaging tone",
            defaultPitch = 1.25f,
            defaultRate = 1.0f,
            avatarEmoji = "🌸",
            tonePreset = VoiceTonePreset.NATURAL
        ),
        VoiceProfile(
            id = "en_female_sophia",
            name = "Sophia",
            nativeName = "Sophia",
            gender = VoiceGender.FEMALE,
            language = AgentLanguage.ENGLISH,
            description = "Polished, confident executive presenter",
            defaultPitch = 1.35f,
            defaultRate = 1.08f,
            avatarEmoji = "💎",
            tonePreset = VoiceTonePreset.PROFESSIONAL
        ),

        // English Male Voices
        VoiceProfile(
            id = "en_male_david",
            name = "David",
            nativeName = "David",
            gender = VoiceGender.MALE,
            language = AgentLanguage.ENGLISH,
            description = "Clear, articulate & authoritative leader",
            defaultPitch = 0.82f,
            defaultRate = 0.98f,
            avatarEmoji = "👔",
            tonePreset = VoiceTonePreset.EXECUTIVE
        ),
        VoiceProfile(
            id = "en_male_alexander",
            name = "Alexander",
            nativeName = "Alexander",
            gender = VoiceGender.MALE,
            language = AgentLanguage.ENGLISH,
            description = "Deep, resonant & warm broadcast tone",
            defaultPitch = 0.74f,
            defaultRate = 0.95f,
            avatarEmoji = "🎙️",
            tonePreset = VoiceTonePreset.WARM
        ),

        // Nepali Female Voices
        VoiceProfile(
            id = "ne_female_aarati",
            name = "Aarati",
            nativeName = "आरती",
            gender = VoiceGender.FEMALE,
            language = AgentLanguage.NEPALI,
            description = "न्यानो, मिलनसार र स्पष्ट आवाज (Warm & expressive)",
            defaultPitch = 1.28f,
            defaultRate = 1.0f,
            avatarEmoji = "🌸",
            tonePreset = VoiceTonePreset.WARM
        ),
        VoiceProfile(
            id = "ne_female_prerana",
            name = "Prerana",
            nativeName = "प्रेरणा",
            gender = VoiceGender.FEMALE,
            language = AgentLanguage.NEPALI,
            description = "व्यावसायिक, छिटो र प्रभावकारी आवाज (Crisp & executive)",
            defaultPitch = 1.38f,
            defaultRate = 1.1f,
            avatarEmoji = "🌺",
            tonePreset = VoiceTonePreset.PROFESSIONAL
        ),

        // Nepali Male Voices
        VoiceProfile(
            id = "ne_male_suman",
            name = "Suman",
            nativeName = "सुमन",
            gender = VoiceGender.MALE,
            language = AgentLanguage.NEPALI,
            description = "गम्भीर, भरपर्दो र शान्त आवाज (Steady & reassuring)",
            defaultPitch = 0.82f,
            defaultRate = 0.98f,
            avatarEmoji = "🏔️",
            tonePreset = VoiceTonePreset.NATURAL
        ),
        VoiceProfile(
            id = "ne_male_bikash",
            name = "Bikash",
            nativeName = "विकास",
            gender = VoiceGender.MALE,
            language = AgentLanguage.NEPALI,
            description = "उर्जावान र गहिरो कार्यकारी आवाज (Deep & authoritative)",
            defaultPitch = 0.72f,
            defaultRate = 1.05f,
            avatarEmoji = "⚡",
            tonePreset = VoiceTonePreset.EXECUTIVE
        )
    )

    fun getProfile(id: String): VoiceProfile? = allProfiles.find { it.id == id }

    fun getDefaultProfile(lang: AgentLanguage, gender: VoiceGender): VoiceProfile {
        return allProfiles.firstOrNull { it.language == lang && it.gender == gender }
            ?: allProfiles.first()
    }
}

data class VoiceSettings(
    val selectedVoiceId: String = "en_female_emily",
    val gender: VoiceGender = VoiceGender.FEMALE,
    val language: AgentLanguage = AgentLanguage.ENGLISH,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val tonePreset: VoiceTonePreset = VoiceTonePreset.NATURAL,
    val autoSpeakResponses: Boolean = true,
    val personaName: String = "Emily",
    val systemVoiceName: String? = null,
    val isServiceOnline: Boolean = true, // Master ON / OFF Service Switch
    val thinkingPower: ThinkingPowerLevel = ThinkingPowerLevel.HIGH, // High Thinking Power
    val backgroundAlertsEnabled: Boolean = true, // Master notification alerts switch
    val alertSoundOrVibrate: Boolean = true, // Sound and vibration on task completion
    val alertOnEmailTriage: Boolean = true, // Alert when background email triage runs
    val alertOnScheduleSync: Boolean = true, // Alert when meeting schedule is monitored
    val alertOnDailyWork: Boolean = true // Alert when daily work tasks are optimized
)
