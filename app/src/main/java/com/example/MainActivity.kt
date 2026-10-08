package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentLanguage
import com.example.data.model.VoiceGender
import com.example.ui.screens.DailyWorkScreen
import com.example.ui.screens.EmailsScreen
import com.example.ui.screens.MeetingsScreen
import com.example.ui.screens.TeamDirectoryScreen
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.AppTab
import com.example.viewmodel.SathiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SathiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SathiMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SathiMainApp(viewModel: SathiViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isBackgroundActive by viewModel.isBackgroundActive.collectAsState()
    val isNepali = voiceSettings.language == AgentLanguage.NEPALI

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "System AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBackgroundActive) com.example.ui.theme.EmeraldSuccess.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isBackgroundActive) "● Background Active" else "Autonomous Agent",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isBackgroundActive) com.example.ui.theme.EmeraldSuccess else CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Background Mode Toggle Quick Icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBackgroundActive) com.example.ui.theme.EmeraldSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.toggleBackgroundService(context) }
                            .padding(end = 6.dp)
                            .testTag("topbar_background_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBackgroundActive) "⚡ Background ON" else "⚡ Run in BG",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBackgroundActive) com.example.ui.theme.EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick language toggle in TopBar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                val next = if (voiceSettings.language == AgentLanguage.NEPALI)
                                    AgentLanguage.ENGLISH
                                else
                                    AgentLanguage.NEPALI
                                viewModel.setLanguage(next)
                            }
                            .testTag("topbar_language_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = VioletAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isNepali) "🇳🇵 NE" else "🇬🇧 EN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // 1. Voice Agent
                NavigationBarItem(
                    selected = currentTab == AppTab.VOICE,
                    onClick = { viewModel.selectTab(AppTab.VOICE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice"
                        )
                    },
                    label = {
                        Text(
                            text = if (isNepali) "भ्वाइस" else "Voice",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyanAccent
                    ),
                    modifier = Modifier.testTag("nav_tab_voice")
                )

                // 2. Meetings
                NavigationBarItem(
                    selected = currentTab == AppTab.MEETINGS,
                    onClick = { viewModel.selectTab(AppTab.MEETINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Meetings"
                        )
                    },
                    label = {
                        Text(
                            text = if (isNepali) "मिटिङ" else "Meetings",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = IndigoLight
                    ),
                    modifier = Modifier.testTag("nav_tab_meetings")
                )

                // 3. Emails
                NavigationBarItem(
                    selected = currentTab == AppTab.EMAILS,
                    onClick = { viewModel.selectTab(AppTab.EMAILS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Emails"
                        )
                    },
                    label = {
                        Text(
                            text = if (isNepali) "इमेल" else "Emails",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = VioletAccent
                    ),
                    modifier = Modifier.testTag("nav_tab_emails")
                )

                // 4. Daily Tasks
                NavigationBarItem(
                    selected = currentTab == AppTab.TASKS,
                    onClick = { viewModel.selectTab(AppTab.TASKS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Tasks"
                        )
                    },
                    label = {
                        Text(
                            text = if (isNepali) "दैनिक कार्य" else "Tasks",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = IndigoLight
                    ),
                    modifier = Modifier.testTag("nav_tab_tasks")
                )

                // 5. Team & Persona
                NavigationBarItem(
                    selected = currentTab == AppTab.TEAM,
                    onClick = { viewModel.selectTab(AppTab.TEAM) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Team"
                        )
                    },
                    label = {
                        Text(
                            text = if (isNepali) "टिम" else "Team",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyanAccent
                    ),
                    modifier = Modifier.testTag("nav_tab_team")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.VOICE -> VoiceStudioScreen(viewModel = viewModel)
                AppTab.MEETINGS -> MeetingsScreen(viewModel = viewModel)
                AppTab.EMAILS -> EmailsScreen(viewModel = viewModel)
                AppTab.TASKS -> DailyWorkScreen(viewModel = viewModel)
                AppTab.TEAM -> TeamDirectoryScreen(viewModel = viewModel)
            }
        }
    }
}
