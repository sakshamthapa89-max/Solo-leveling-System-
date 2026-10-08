package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentLanguage
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.AppTab
import com.example.viewmodel.SathiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeetingsScreen(
    viewModel: SathiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val meetings by viewModel.meetings.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isNepali = voiceSettings.language == AgentLanguage.NEPALI

    var showAddDialog by remember { mutableStateOf(false) }

    // Check for scheduling conflicts (meetings within 30 min of each other)
    val hasConflict = remember(meetings) {
        if (meetings.size < 2) false
        else {
            val sorted = meetings.sortedBy { it.scheduledTimeEpoch }
            var conflict = false
            for (i in 0 until sorted.size - 1) {
                val endA = sorted[i].scheduledTimeEpoch + (sorted[i].durationMinutes * 60_000L)
                if (endA > sorted[i + 1].scheduledTimeEpoch) {
                    conflict = true
                    break
                }
            }
            conflict
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IndigoLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_meeting_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meeting")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Title & Quick Voice Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isNepali) "टिम मिटिङ तालिका" else "Team Meetings Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNepali) "${meetings.size} वटा मिटिङहरू सेड्युल गरिएको छ" else "${meetings.size} meetings scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Voice Scheduling Shortcut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VioletAccent.copy(alpha = 0.2f),
                    modifier = Modifier
                        .clickable { viewModel.selectTab(AppTab.VOICE) }
                        .testTag("voice_schedule_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = VioletAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNepali) "भ्वाइसबाट थप्नुहोस्" else "Voice Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conflict / Smart Availability Banner
            if (hasConflict) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Conflict Warning",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali)
                                "सावधानी: तालिकामा दुई मिटिङहरूको समय जुधेको छ! साथी AI ले समय पुनर्मिलाउन सुझाव दिन्छ।"
                            else
                                "Conflict detected: Two meetings overlap in time! Sathi AI suggests rescheduling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFED7AA)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "टिमको तालिका व्यवस्थित छ, कुनै समय जुधेको छैन।" else "All team schedules are aligned with no conflicts.",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Meetings List
            if (meetings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isNepali) "कुनै पनि मिटिङ सेड्युल गरिएको छैन" else "No meetings scheduled yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("meetings_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(meetings, key = { it.id }) { meeting ->
                        MeetingCard(
                            meeting = meeting,
                            onJoin = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meeting.meetLink))
                                context.startActivity(intent)
                            },
                            onDelete = { viewModel.deleteMeeting(meeting) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeetingDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, agenda, duration, attendees ->
                viewModel.scheduleMeeting(
                    title = title,
                    agenda = agenda,
                    epochTime = System.currentTimeMillis() + (3600_000L * 2),
                    duration = duration,
                    attendees = attendees
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MeetingCard(
    meeting: Meeting,
    onJoin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.US) }
    val formattedDate = remember(meeting.scheduledTimeEpoch) {
        dateFormat.format(Date(meeting.scheduledTimeEpoch))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Title, Voice badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (meeting.createdByVoice) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoLight.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "🎙️ AI Voice",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Meeting",
                            tint = RoseError.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Agenda
            if (meeting.agenda.isNotBlank()) {
                Text(
                    text = meeting.agenda,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Attendees & Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👥 ${meeting.attendeeNames} (${meeting.durationMinutes}m)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Join Meet Button
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("join_meet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Join Meet",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun AddMeetingDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, agenda: String, duration: Int, attendees: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var agenda by remember { mutableStateOf("") }
    var attendees by remember { mutableStateOf("Saksham Thapa, Pooja Karki") }
    var durationText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Team Meeting") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meeting Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meeting_title_input")
                )

                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text("Agenda / Topics") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = attendees,
                    onValueChange = { attendees = it },
                    label = { Text("Attendees") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationText.toIntOrNull() ?: 30
                    if (title.isNotBlank()) {
                        onConfirm(title, agenda, dur, attendees)
                    }
                },
                modifier = Modifier.testTag("confirm_create_meeting_button")
            ) {
                Text("Schedule Meeting")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
