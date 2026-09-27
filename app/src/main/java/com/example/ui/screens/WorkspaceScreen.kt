package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.DiffViewer
import com.example.ui.components.StatusBadge
import com.example.ui.components.ToolCallCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: StudioViewModel,
    onBackToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Chat", "Files", "Editor", "Terminal", "Preview", "Build", "Git")

    if (activeProject == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active project selected", color = Slate400)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBackToProjects,
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan)
                ) {
                    Text("Select a Project", color = Slate950)
                }
            }
        }
        return
    }

    val project = activeProject!!

    Scaffold(
        modifier = modifier.testTag("workspace_screen"),
        containerColor = Slate950,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = project.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(text = project.type, color = DevCyan)
                            }
                            Text(
                                text = "Agent: ${project.activeAgentId} • ${project.activeModelId}",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToProjects) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate100)
                        }
                    },
                    actions = {
                        StatusBadge(
                            text = project.permissionMode.replace("_", " "),
                            color = when (project.permissionMode) {
                                "TRUSTED" -> DevEmerald
                                "SAFE" -> DevCyan
                                else -> DevAmber
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
                )

                // Sub-Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate900,
                    contentColor = DevCyan,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = DevCyan
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) DevCyan else Slate400
                                )
                            },
                            modifier = Modifier.testTag("workspace_tab_$title")
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> WorkspaceChatTab(viewModel = viewModel, project = project)
                1 -> WorkspaceFilesTab(viewModel = viewModel, onOpenFile = { selectedTab = 2 })
                2 -> WorkspaceEditorTab(viewModel = viewModel)
                3 -> WorkspaceTerminalTab(viewModel = viewModel)
                4 -> WorkspacePreviewTab(viewModel = viewModel)
                5 -> WorkspaceBuildTab(viewModel = viewModel)
                6 -> WorkspaceGitTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WorkspaceChatTab(viewModel: StudioViewModel, project: ProjectEntity) {
    val messages by viewModel.conversationMessages.collectAsState()
    val isRunning by viewModel.isAgentRunning.collectAsState()
    val streamStatus by viewModel.agentStreamStatus.collectAsState()
    val pendingApprovals by viewModel.pendingApprovals.collectAsState()
    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Active Status & Approvals
        if (isRunning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DevCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DevCyan, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = streamStatus, color = DevCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Approvals bar
        if (pendingApprovals.isNotEmpty()) {
            pendingApprovals.forEach { call ->
                ToolCallCard(
                    toolCall = call,
                    onApprove = { approved -> viewModel.approveToolCall(call, approved) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Messages List
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(msg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick action chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            val suggestions = listOf("Create Expense Tracker", "Add Dark Mode", "Run Tests & Lint", "Start Server")
            suggestions.forEach { suggestion ->
                AssistChip(
                    onClick = { inputPrompt = suggestion },
                    label = { Text(suggestion, fontSize = 11.sp, color = DevCyanLight) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
            }
        }

        // Prompt Input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(12.dp))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            TextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask coding agent to build, edit, test...", fontSize = 13.sp, color = Slate600) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100
                ),
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_prompt_input")
            )

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank() && !isRunning) {
                        val p = inputPrompt.trim()
                        inputPrompt = ""
                        viewModel.sendMessageToAgent(p)
                    }
                },
                enabled = inputPrompt.isNotBlank() && !isRunning,
                modifier = Modifier.testTag("send_prompt_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputPrompt.isNotBlank() && !isRunning) DevCyan else Slate600
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: MessageEntity) {
    val isUser = message.role == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(if (isUser) Slate800 else Slate900)
                .border(1.dp, if (isUser) Slate700 else Slate800, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                if (!message.reasoningContent.isNullOrBlank()) {
                    Text(
                        text = "🧠 Reasoning Thought Process",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevViolet
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.reasoningContent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate950, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Slate100
                )
            }
        }
    }
}
