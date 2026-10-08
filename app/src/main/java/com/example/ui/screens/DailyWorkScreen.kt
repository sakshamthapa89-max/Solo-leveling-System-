package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentLanguage
import com.example.data.model.DailyTask
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.AppTab
import com.example.viewmodel.SathiViewModel

@Composable
fun DailyWorkScreen(
    viewModel: SathiViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.dailyTasks.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isNepali = voiceSettings.language == AgentLanguage.NEPALI

    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedStatusFilter) {
        if (selectedStatusFilter == null) tasks else tasks.filter { it.status == selectedStatusFilter }
    }

    val completedCount = remember(tasks) { tasks.count { it.status == TaskStatus.COMPLETED } }
    val progressPercent = if (tasks.isEmpty()) 0f else (completedCount.toFloat() / tasks.size.toFloat())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = EmeraldSuccess,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isNepali) "दैनिक कार्य तथा वर्कफ्लो" else "Daily Work & Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNepali) "$completedCount/${tasks.size} कार्य सम्पन्न" else "$completedCount of ${tasks.size} completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { viewModel.selectTab(AppTab.VOICE) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNepali) "भ्वाइसबाट कार्य थप्नुहोस्" else "Voice Dictate",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Executive Audio Standup / Morning Briefing Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNepali) "आजको बिहानी ब्रिफिङ (Morning Briefing)" else "Team Daily Standup Briefing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isNepali)
                                "साथीको आवाजमा आजका मिटिङ र प्राथमिकता सुन्नुहोस्"
                            else
                                "Listen to today's schedule, priority emails & team tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Play/Stop Audio Briefing button
                    Button(
                        onClick = {
                            if (isSpeaking) viewModel.stopSpeaking() else viewModel.playDailyBriefing()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeaking) VioletAccent else CyanAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("play_briefing_audio_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpeaking) "Stop" else "Listen",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Tabs
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = if (isNepali) "सबै (${tasks.size})" else "All (${tasks.size})",
                        isSelected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null }
                    )
                }
                item {
                    CategoryChip(
                        label = if (isNepali) "बाँकी (To-Do)" else "To-Do",
                        isSelected = selectedStatusFilter == TaskStatus.TODO,
                        onClick = { selectedStatusFilter = TaskStatus.TODO }
                    )
                }
                item {
                    CategoryChip(
                        label = if (isNepali) "चालू (In Progress)" else "In Progress",
                        isSelected = selectedStatusFilter == TaskStatus.IN_PROGRESS,
                        onClick = { selectedStatusFilter = TaskStatus.IN_PROGRESS }
                    )
                }
                item {
                    CategoryChip(
                        label = if (isNepali) "सम्पन्न (Done)" else "Done",
                        isSelected = selectedStatusFilter == TaskStatus.COMPLETED,
                        onClick = { selectedStatusFilter = TaskStatus.COMPLETED }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isNepali) "यस सूचीमा कुनै कार्य छैन" else "No tasks in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("daily_tasks_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggleStatus = { viewModel.toggleTaskStatus(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            isNepali = isNepali,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, assignee, prio ->
                viewModel.addTask(title, desc, assignee, prio)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: DailyTask,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox / Status toggle button
            IconButton(
                onClick = onToggleStatus,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("toggle_task_status_button")
            ) {
                Icon(
                    imageVector = when (task.status) {
                        TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                        TaskStatus.IN_PROGRESS -> Icons.Default.PlayArrow
                        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = "Toggle Status",
                    tint = when (task.status) {
                        TaskStatus.COMPLETED -> EmeraldSuccess
                        TaskStatus.IN_PROGRESS -> CyanAccent
                        TaskStatus.TODO -> Color(0xFF94A3B8)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Assignee & Priority Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "👤 ${task.assignedToName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Priority
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (task.priority) {
                            TaskPriority.HIGH -> RoseError.copy(alpha = 0.2f)
                            TaskPriority.MEDIUM -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            TaskPriority.LOW -> EmeraldSuccess.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (task.priority) {
                                TaskPriority.HIGH -> RoseError
                                TaskPriority.MEDIUM -> Color(0xFFF59E0B)
                                TaskPriority.LOW -> EmeraldSuccess
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.createdByVoice) {
                        Text(
                            text = "🎙️ AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = RoseError.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    isNepali: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, assignee: String, priority: TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("Saksham Thapa") }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNepali) "नयाँ कार्य थप्नुहोस्" else "New Daily Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("task_description_input")
                )

                OutlinedTextField(
                    value = assignee,
                    onValueChange = { assignee = it },
                    label = { Text("Assignee Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW).forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (priority == p) IndigoLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { priority = p }
                        ) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (priority == p) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, assignee, priority)
                    }
                },
                modifier = Modifier.testTag("confirm_create_task_button")
            ) {
                Text(if (isNepali) "कार्य थप्नुहोस्" else "Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
