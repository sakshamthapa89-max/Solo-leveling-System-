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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentLanguage
import com.example.data.model.EmailCategory
import com.example.data.model.EmailMessage
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.AppTab
import com.example.viewmodel.SathiViewModel

@Composable
fun EmailsScreen(
    viewModel: SathiViewModel,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.emails.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isNepali = voiceSettings.language == AgentLanguage.NEPALI

    var selectedCategory by remember { mutableStateOf<EmailCategory?>(null) }
    var showDraftsOnly by remember { mutableStateOf(false) }
    var showComposeDialog by remember { mutableStateOf(false) }
    var replyingToEmail by remember { mutableStateOf<EmailMessage?>(null) }

    val filteredEmails = remember(emails, selectedCategory, showDraftsOnly) {
        emails.filter { email ->
            val matchesCategory = selectedCategory == null || email.category == selectedCategory
            val matchesDraft = if (showDraftsOnly) email.isDraft else true
            matchesCategory && matchesDraft
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showComposeDialog = true },
                containerColor = VioletAccent,
                contentColor = Color.White,
                modifier = Modifier.testTag("compose_email_fab")
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = "Compose Email")
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
                        text = if (isNepali) "टिम इमेल हब" else "Team Email Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNepali) "स्वचालित सारांश र भ्वाइस रिप्लाई" else "AI Summaries & Voice Dispatch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyanAccent.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { viewModel.selectTab(AppTab.VOICE) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNepali) "भ्वाइस ड्राफ्ट" else "Voice Reply",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = if (isNepali) "सबै (${emails.size})" else "All (${emails.size})",
                        isSelected = selectedCategory == null && !showDraftsOnly,
                        onClick = {
                            selectedCategory = null
                            showDraftsOnly = false
                        }
                    )
                }
                item {
                    CategoryChip(
                        label = "Priority",
                        isSelected = selectedCategory == EmailCategory.PRIORITY,
                        onClick = {
                            selectedCategory = EmailCategory.PRIORITY
                            showDraftsOnly = false
                        }
                    )
                }
                item {
                    CategoryChip(
                        label = "Team",
                        isSelected = selectedCategory == EmailCategory.TEAM,
                        onClick = {
                            selectedCategory = EmailCategory.TEAM
                            showDraftsOnly = false
                        }
                    )
                }
                item {
                    CategoryChip(
                        label = "Client",
                        isSelected = selectedCategory == EmailCategory.CLIENT,
                        onClick = {
                            selectedCategory = EmailCategory.CLIENT
                            showDraftsOnly = false
                        }
                    )
                }
                item {
                    CategoryChip(
                        label = "Drafts",
                        isSelected = showDraftsOnly,
                        onClick = {
                            showDraftsOnly = true
                            selectedCategory = null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Email Items List
            if (filteredEmails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isNepali) "यस श्रेणीमा कुनै इमेल फेला परेन" else "No emails found in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("emails_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEmails, key = { it.id }) { email ->
                        EmailItemCard(
                            email = email,
                            isNepali = isNepali,
                            onSpeakSummary = { summary -> viewModel.speakText(summary) },
                            onMarkRead = { viewModel.markEmailAsRead(email.id) },
                            onDelete = { viewModel.deleteEmail(email.id) },
                            onReply = { replyingToEmail = email }
                        )
                    }
                }
            }
        }
    }

    // Compose or Reply Dialog
    if (showComposeDialog || replyingToEmail != null) {
        val targetEmail = replyingToEmail
        ComposeEmailDialog(
            initialTo = targetEmail?.senderEmail ?: "team@company.com",
            initialSubject = if (targetEmail != null) "Re: ${targetEmail.subject}" else "",
            isNepali = isNepali,
            onDismiss = {
                showComposeDialog = false
                replyingToEmail = null
            },
            onSend = { to, subject, body, isDraft ->
                viewModel.sendOrSaveEmail(to, subject, body, isDraft)
                showComposeDialog = false
                replyingToEmail = null
            }
        )
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) IndigoLight else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun EmailItemCard(
    email: EmailMessage,
    isNepali: Boolean,
    onSpeakSummary: (String) -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onReply: () -> Unit
) {
    var expandedSummary by remember { mutableStateOf(false) }
    var summaryLangNepali by remember { mutableStateOf(isNepali) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("email_card_${email.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!email.isRead)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Sender, Category, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (email.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = email.senderName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (email.category) {
                        EmailCategory.PRIORITY -> RoseError.copy(alpha = 0.2f)
                        EmailCategory.TEAM -> CyanAccent.copy(alpha = 0.2f)
                        EmailCategory.CLIENT -> EmeraldSuccess.copy(alpha = 0.2f)
                        EmailCategory.INVITATION -> VioletAccent.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = email.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (email.category) {
                            EmailCategory.PRIORITY -> RoseError
                            EmailCategory.TEAM -> CyanAccent
                            EmailCategory.CLIENT -> EmeraldSuccess
                            EmailCategory.INVITATION -> VioletAccent
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subject
            Text(
                text = email.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body preview
            Text(
                text = email.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expandedSummary) 8 else 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Summary Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ AI Summary (${if (summaryLangNepali) "नेपाली" else "English"})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Toggle Nepali/English summary
                            Text(
                                text = if (summaryLangNepali) "Translate EN" else "नेपालीमा हेर्नुहोस्",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier
                                    .clickable { summaryLangNepali = !summaryLangNepali }
                                    .padding(2.dp)
                            )
                        }

                        // Listen audio summary
                        IconButton(
                            onClick = {
                                val text = if (summaryLangNepali) email.summaryNepali else email.summaryEnglish
                                onSpeakSummary(text)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen Summary",
                                tint = IndigoLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (summaryLangNepali) email.summaryNepali else email.summaryEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Reply, Read, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Button(
                        onClick = onReply,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoLight),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("reply_email_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNepali) "जवाफ दिनुहोस्" else "Smart Reply",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!email.isRead) {
                        IconButton(
                            onClick = onMarkRead,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Mark as read",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = RoseError.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComposeEmailDialog(
    initialTo: String,
    initialSubject: String,
    isNepali: Boolean,
    onDismiss: () -> Unit,
    onSend: (to: String, subject: String, body: String, isDraft: Boolean) -> Unit
) {
    var to by remember { mutableStateOf(initialTo) }
    var subject by remember { mutableStateOf(initialSubject) }
    var body by remember {
        mutableStateOf(
            if (isNepali)
                "नमस्ते,\n\nहामीले परियोजनाको प्रगति समीक्षा गरेका छौं र सबै कार्यहरू तालिका अनुसार अगाडि बढिरहेका छन्।\n\nसद्भाव,\nसक्षम थापा"
            else
                "Hi,\n\nWe have reviewed the project specifications and confirmed the timeline. Looking forward to our sync.\n\nBest,\nSaksham Thapa"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNepali) "इमेल लेख्नुहोस् (Compose)" else "Compose Team Email") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("email_to_input")
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("email_subject_input")
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message Body") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("email_body_input")
                )

                // Quick AI tone templates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            body = if (isNepali) "हुन्छ, म यो प्रस्ताव अनुमोदन गर्दछु। काम अगाडि बढाउनुहोला।" else "Approved. Please proceed with the rollout."
                        }
                    ) {
                        Text(
                            text = "👍 Approve",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VioletAccent.copy(alpha = 0.15f),
                        modifier = Modifier.clickable {
                            body = if (isNepali) "यस विषयमा ३० मिनेटको भर्चुअल छलफल गरौं। भोलि कस्तो हुन्छ?" else "Let's set up a 30-min call to finalize the details."
                        }
                    ) {
                        Text(
                            text = "📅 Request Call",
                            style = MaterialTheme.typography.labelSmall,
                            color = VioletAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(to, subject, body, false) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoLight),
                modifier = Modifier.testTag("send_email_button")
            ) {
                Text(if (isNepali) "इमेल पठाउनुहोस्" else "Send Email")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onSend(to, subject, body, true) }
            ) {
                Text(if (isNepali) "ड्राफ्ट राख्नुहोस्" else "Save Draft")
            }
        }
    )
}
