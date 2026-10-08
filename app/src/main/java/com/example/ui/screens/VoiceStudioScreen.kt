package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AgentLanguage
import com.example.data.model.ConversationMessage
import com.example.data.model.VoiceGender
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.VoiceControlPill
import com.example.ui.components.VoiceCustomizerSheet
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.SathiViewModel

@Composable
fun VoiceStudioScreen(
    viewModel: SathiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val latestTranscript by viewModel.latestVoiceTranscript.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isBackgroundActive by viewModel.isBackgroundActive.collectAsState()
    val backgroundLogs by viewModel.backgroundLogs.collectAsState()
    val backgroundStatus by viewModel.backgroundWorkStatus.collectAsState()
    val listState = rememberLazyListState()

    var showVoiceCustomizer by remember { mutableStateOf(false) }
    var showBackgroundLogs by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Auto-scroll chat to latest
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Master Service ON/OFF Banner ("this AI on off serves & Make sure this AI is personal ai perfect ai")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("master_service_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (voiceSettings.isServiceOnline)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    Color(0xFF1E293B).copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                if (voiceSettings.isServiceOnline) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF475569)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (voiceSettings.isServiceOnline) EmeraldSuccess else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "System AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (voiceSettings.isServiceOnline) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF334155)
                            ) {
                                Text(
                                    text = if (voiceSettings.isServiceOnline) "ONLINE" else "STANDBY (OFF)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (voiceSettings.isServiceOnline) EmeraldSuccess else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (voiceSettings.isServiceOnline)
                                "Personal AI for Saksham • 🧠 High Thinking Power"
                            else
                                "Personal AI is in Standby mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = voiceSettings.isServiceOnline,
                    onCheckedChange = { viewModel.toggleMasterService() },
                    modifier = Modifier.testTag("master_service_switch")
                )
            }
        }

        // Voice Control Pill with Customizer Sheet Launcher
        VoiceControlPill(
            settings = voiceSettings,
            isListening = isListening,
            isSpeaking = isSpeaking,
            isThinking = isThinking,
            onToggleGender = {
                val next = if (voiceSettings.gender == VoiceGender.FEMALE) VoiceGender.MALE else VoiceGender.FEMALE
                viewModel.setVoiceGender(next)
            },
            onToggleLanguage = {
                val next = if (voiceSettings.language == AgentLanguage.NEPALI) AgentLanguage.ENGLISH else AgentLanguage.NEPALI
                viewModel.setLanguage(next)
            },
            onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
            onOpenCustomizer = { showVoiceCustomizer = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Center Dynamic Interactive Orb & Waveform
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulse Animation on Mic Orb
                val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.25f else if (isSpeaking) 1.15f else 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    // Outer glowing halo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (isListening) CyanAccent.copy(alpha = 0.45f)
                                        else if (isSpeaking) VioletAccent.copy(alpha = 0.45f)
                                        else IndigoLight.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Big Interactive Mic Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isListening) listOf(RoseError, Color(0xFFE11D48))
                                    else if (isSpeaking) listOf(VioletAccent, IndigoLight)
                                    else listOf(IndigoLight, CyanAccent)
                                )
                            )
                            .clickable {
                                if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else if (isListening) {
                                    viewModel.stopListening()
                                } else {
                                    if (hasMicPermission) {
                                        viewModel.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("microphone_orb_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isThinking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else if (isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Listening" else "Start Talking",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Audio Waveform
                AudioWaveformVisualizer(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    rmsLevel = rmsDb,
                    height = 36.dp
                )

                // Spoken transcript or instruction
                val promptNotice = when {
                    isListening -> if (voiceSettings.language == AgentLanguage.NEPALI) "सुन्दैछु... बोल्नुहोस्" else "Listening... speak now"
                    isSpeaking -> "System AI is speaking..."
                    isThinking -> "System AI is reasoning & executing..."
                    else -> if (voiceSettings.language == AgentLanguage.NEPALI) "माइक थिच्नुहोस् र बोल्नुहोस्" else "Tap microphone to speak to System AI"
                }

                Text(
                    text = promptNotice,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isListening) CyanAccent else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System AI Background Work Monitor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("background_work_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBackgroundActive)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = if (isBackgroundActive) androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)) else null
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isBackgroundActive) EmeraldSuccess else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "System AI Background Work",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBackgroundActive) "Active • Running while you do other work" else "Paused • Tap to run in background",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isBackgroundActive) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isBackgroundActive,
                        onCheckedChange = { viewModel.toggleBackgroundService(context) },
                        modifier = Modifier.testTag("toggle_background_work_switch")
                    )
                }

                if (isBackgroundActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = backgroundStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (showBackgroundLogs) "Hide Logs" else "Logs (${backgroundLogs.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showBackgroundLogs = !showBackgroundLogs }
                                    .padding(start = 6.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showBackgroundLogs) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            backgroundLogs.take(4).forEach { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${log.title}: ${log.detail}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Command Suggestions Row
        Text(
            text = if (voiceSettings.language == AgentLanguage.NEPALI) "द्रुत भ्वाइस कमान्डहरू (Quick Commands):" else "Suggested Voice Commands:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        val quickPrompts = if (voiceSettings.language == AgentLanguage.NEPALI) {
            listOf(
                "📅 भोलि दिउँसो २ बजे टिम मिटिङ राख",
                "✉️ नयाँ इमेलहरूको सारांश सुनाउ",
                "🎙️ आजको बिहानी ब्रिफिङ सुनाउ",
                "📝 नयाँ कार्य थप र सक्षमलाई जिम्मा देउ"
            )
        } else {
            listOf(
                "📅 Schedule sprint review tomorrow at 2 PM",
                "✉️ Draft email reply approving specs",
                "🎙️ Give me today's morning briefing",
                "📝 Assign QA validation task to Pooja"
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable {
                            val cleanText = prompt.substringAfter(" ")
                            viewModel.processUserVoiceInput(cleanText)
                        }
                        .testTag("quick_command_chip")
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conversation & Action History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("conversation_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ConversationBubble(
                    message = msg,
                    onSpeak = { viewModel.speakText(msg.text) }
                )
            }
        }
    }

    if (showVoiceCustomizer) {
        VoiceCustomizerSheet(
            viewModel = viewModel,
            onDismiss = { showVoiceCustomizer = false }
        )
    }
}

@Composable
fun ConversationBubble(
    message: ConversationMessage,
    onSpeak: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) IndigoLight else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header: Role & Audio play button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isUser) "You (Voice)" else "System AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White.copy(alpha = 0.85f) else CyanAccent
                    )

                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen Audio",
                            tint = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )

                // Action Confirmation Card if Sathi executed an action
                if (!message.actionType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUser) Color.Black.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (message.actionType) {
                                "MEETING_SCHEDULED" -> Icons.Default.CalendarMonth
                                "EMAIL_DRAFTED" -> Icons.Default.Email
                                "TASK_CREATED" -> Icons.AutoMirrored.Filled.Assignment
                                else -> Icons.Default.PlayArrow
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.actionDetail ?: "Action Executed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}
