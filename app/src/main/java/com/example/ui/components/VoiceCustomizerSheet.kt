package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentLanguage
import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceGender
import com.example.data.model.VoiceProfile
import com.example.data.model.VoiceTonePreset
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.SathiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCustomizerSheet(
    viewModel: SathiViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    var genderFilter by remember { mutableStateOf<VoiceGender?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("voice_customizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI Voice Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize voices, speech rate & tone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.resetVoiceSettings() },
                        modifier = Modifier.testTag("reset_voice_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CyanAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reset", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language & Gender Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (voiceSettings.language == AgentLanguage.ENGLISH) IndigoLight else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { viewModel.setLanguage(AgentLanguage.ENGLISH) }
                ) {
                    Text(
                        text = "🇬🇧 English",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (voiceSettings.language == AgentLanguage.ENGLISH) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (voiceSettings.language == AgentLanguage.NEPALI) IndigoLight else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { viewModel.setLanguage(AgentLanguage.NEPALI) }
                ) {
                    Text(
                        text = "🇳🇵 Nepali (नेपाली)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (voiceSettings.language == AgentLanguage.NEPALI) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Gender Filter (All / Female / Male)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (genderFilter == null) CyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { genderFilter = null }
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (genderFilter == null) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (genderFilter == VoiceGender.FEMALE) Color(0xFFEC4899).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { genderFilter = VoiceGender.FEMALE }
                    ) {
                        Text(
                            text = "♀ Female",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (genderFilter == VoiceGender.FEMALE) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (genderFilter == VoiceGender.MALE) CyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { genderFilter = VoiceGender.MALE }
                    ) {
                        Text(
                            text = "♂ Male",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (genderFilter == VoiceGender.MALE) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice Profiles Horizontal Carousel / Grid
            Text(
                text = "Select Persona Voice (${if (voiceSettings.language == AgentLanguage.ENGLISH) "English" else "Nepali"}):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentLangProfiles = remember(voiceSettings.language, genderFilter) {
                VoiceCatalog.allProfiles.filter {
                    it.language == voiceSettings.language && (genderFilter == null || it.gender == genderFilter)
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentLangProfiles, key = { it.id }) { profile ->
                    val isSelected = voiceSettings.selectedVoiceId == profile.id
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { viewModel.selectVoiceProfile(profile.id) }
                            .testTag("voice_card_${profile.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                IndigoLight.copy(alpha = 0.25f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(2.dp, CyanAccent) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = profile.avatarEmoji, fontSize = 24.sp)
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = if (profile.gender == VoiceGender.FEMALE) "Female" else "Male",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Test preview button on card
                            Button(
                                onClick = {
                                    val text = if (profile.language == AgentLanguage.NEPALI)
                                        "नमस्ते! म ${profile.name} हुँ।"
                                    else
                                        "Hi! I'm ${profile.name}."
                                    viewModel.previewVoice(text)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Preview",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tone Presets Chips
            Text(
                text = "Tone & Timbre Presets:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VoiceTonePreset.values()) { preset ->
                    val isToneSelected = voiceSettings.tonePreset == preset
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isToneSelected) VioletAccent else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.setTonePreset(preset) }
                            .testTag("tone_chip_${preset.name}")
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToneSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToneSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fine-Tuning Controls: Speech Rate & Pitch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Speech Rate (Speed)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speech Rate (Speed)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format("%.2f", voiceSettings.speechRate)}x",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }

                    // Speed Quick Presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0.75f to "0.75x Slow", 1.0f to "1.0x Normal", 1.25f to "1.25x Fast", 1.5f to "1.5x Rapid").forEach { (speed, label) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (kotlin.math.abs(voiceSettings.speechRate - speed) < 0.05f)
                                    CyanAccent.copy(alpha = 0.25f)
                                else
                                    MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { viewModel.setSpeechRate(speed) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (kotlin.math.abs(voiceSettings.speechRate - speed) < 0.05f) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = voiceSettings.speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 29,
                        modifier = Modifier.fillMaxWidth().testTag("speech_rate_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Pitch (Tone frequency)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = VioletAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pitch & Tone Modulation",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format("%.2f", voiceSettings.pitch)}x",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = VioletAccent
                        )
                    }

                    // Pitch Quick Presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0.8f to "0.8x Deep", 1.0f to "1.0x Natural", 1.2f to "1.2x Bright", 1.4f to "1.4x High").forEach { (pitchVal, label) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (kotlin.math.abs(voiceSettings.pitch - pitchVal) < 0.05f)
                                    VioletAccent.copy(alpha = 0.25f)
                                else
                                    MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { viewModel.setPitch(pitchVal) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (kotlin.math.abs(voiceSettings.pitch - pitchVal) < 0.05f) VioletAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = voiceSettings.pitch,
                        onValueChange = { viewModel.setPitch(it) },
                        valueRange = 0.5f..1.8f,
                        steps = 25,
                        modifier = Modifier.fillMaxWidth().testTag("pitch_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Full Preview Button
            Button(
                onClick = { viewModel.previewVoice() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) VioletAccent else CyanAccent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("preview_voice_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpeaking) "Speaking Preview..." else "Listen to ${voiceSettings.personaName}'s Voice",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
