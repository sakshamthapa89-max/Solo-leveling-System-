package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.model.AgentLanguage
import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceGender
import com.example.data.model.VoiceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceOutputManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtterance = MutableStateFlow("")
    val currentUtterance: StateFlow<String> = _currentUtterance.asStateFlow()

    private val _installedVoices = MutableStateFlow<List<Voice>>(emptyList())
    val installedVoices: StateFlow<List<Voice>> = _installedVoices.asStateFlow()

    private val nepaliLocale = Locale.forLanguageTag("ne-NP")
    private val hindiLocale = Locale.forLanguageTag("hi-IN") // Phonetic equivalent for Devanagari script if ne_NP isn't packaged
    private val englishLocale = Locale.US

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .build()
            )

            try {
                val voices = tts?.voices?.toList() ?: emptyList()
                _installedVoices.value = voices
            } catch (_: Exception) {}

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtterance.value = ""
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtterance.value = ""
                }
            })
        }
    }

    fun speak(
        text: String,
        settings: VoiceSettings,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null || text.isBlank()) return

        stop()
        _currentUtterance.value = text

        // 1. Configure Target Language Locale
        val targetLocale = if (settings.language == AgentLanguage.NEPALI) {
            val res = tts?.isLanguageAvailable(nepaliLocale)
            if (res == TextToSpeech.LANG_AVAILABLE || res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                nepaliLocale
            } else {
                val hiRes = tts?.isLanguageAvailable(hindiLocale)
                if (hiRes == TextToSpeech.LANG_AVAILABLE || hiRes == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    hindiLocale
                } else {
                    Locale.getDefault()
                }
            }
        } else {
            englishLocale
        }

        try {
            tts?.language = targetLocale
        } catch (_: Exception) {}

        // 2. Select & Modulate Voice Pitch and Speech Rate
        applyVoiceCharacteristics(settings, targetLocale)

        // 3. Dispatch Utterance
        val utteranceId = "sathi_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun applyVoiceCharacteristics(settings: VoiceSettings, locale: Locale) {
        // A. Match system voice if specified or by gender
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val matchedVoice = if (settings.systemVoiceName != null) {
                    voices.firstOrNull { it.name == settings.systemVoiceName }
                } else {
                    voices.firstOrNull { voice ->
                        val voiceLocale = voice.locale
                        val matchesLang = voiceLocale.language == locale.language
                        val isFemaleDesired = settings.gender == VoiceGender.FEMALE
                        val voiceName = voice.name.lowercase()

                        val genderMatches = if (isFemaleDesired) {
                            voiceName.contains("female") || voiceName.contains("girl") || voiceName.contains("woman") || voiceName.contains("-f-") || voiceName.contains("_f_")
                        } else {
                            voiceName.contains("male") || voiceName.contains("guy") || voiceName.contains("man") || voiceName.contains("-m-") || voiceName.contains("_m_")
                        }
                        matchesLang && genderMatches
                    } ?: voices.firstOrNull { it.locale.language == locale.language }
                }

                matchedVoice?.let {
                    tts?.voice = it
                }
            }
        } catch (_: Exception) {}

        // B. Apply Pitch: Profile default * Tone Preset multiplier * Custom pitch adjustment
        val profile = VoiceCatalog.getProfile(settings.selectedVoiceId)
        val basePitch = profile?.defaultPitch ?: if (settings.gender == VoiceGender.FEMALE) 1.25f else 0.80f
        val tonePitchMultiplier = settings.tonePreset.pitchMultiplier
        val effectivePitch = (basePitch * tonePitchMultiplier * (settings.pitch / 1.0f)).coerceIn(0.5f, 2.0f)

        // C. Apply Rate: Profile default * Tone Preset multiplier * Custom speech rate
        val baseRate = profile?.defaultRate ?: 1.0f
        val toneRateMultiplier = settings.tonePreset.rateMultiplier
        val effectiveRate = (baseRate * toneRateMultiplier * settings.speechRate).coerceIn(0.5f, 2.0f)

        tts?.setPitch(effectivePitch)
        tts?.setSpeechRate(effectiveRate)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _currentUtterance.value = ""
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
