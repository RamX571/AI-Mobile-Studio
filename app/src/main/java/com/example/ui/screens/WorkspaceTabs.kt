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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.git.DiffLine
import com.example.git.DiffType
import com.example.ui.components.DiffViewer
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel
import java.io.File

@Composable
fun WorkspaceFilesTab(
    viewModel: StudioViewModel,
    onOpenFile: () -> Unit
) {
    val files by viewModel.projectFiles.collectAsState()
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("WORKSPACE FILES (${files.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Row {
                IconButton(onClick = { showNewFileDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "New File", tint = DevCyan)
                }
                IconButton(onClick = { viewModel.loadProjectFiles() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Slate400)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (files.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No files in source directory", color = Slate600)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(files) { fileItem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate900)
                            .clickable {
                                if (!fileItem.isDirectory) {
                                    viewModel.loadFileForEditing(fileItem.relativePath)
                                    onOpenFile()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = if (fileItem.isDirectory) Icons.Default.Folder else when (fileItem.extension) {
                                "jsx", "js", "ts", "tsx" -> Icons.Default.Javascript
                                "html", "css" -> Icons.Default.Web
                                "py" -> Icons.Default.Psychology
                                "kt", "java" -> Icons.Default.Code
                                "json" -> Icons.Default.DataArray
                                else -> Icons.Default.InsertDriveFile
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (fileItem.isDirectory) DevAmber else DevCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fileItem.relativePath,
                                color = Slate100,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!fileItem.isDirectory) {
                                Text(
                                    text = "${fileItem.sizeBytes} B",
                                    color = Slate600,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteFile(fileItem.relativePath) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DevRose, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = Slate100) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. src/components/Header.jsx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            viewModel.createFile(newFileName.trim())
                            showNewFileDialog = false
                            newFileName = ""
                            onOpenFile()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan)
                ) {
                    Text("Create", color = Slate950)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel", color = Slate400) }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun WorkspaceEditorTab(viewModel: StudioViewModel) {
    val selectedPath by viewModel.selectedFilePath.collectAsState()
    val content by viewModel.editorContent.collectAsState()
    val isDirty by viewModel.isEditorDirty.collectAsState()

    var showDiffModal by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Editor Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedPath ?: "No file open",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                if (isDirty) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(DevAmber, RoundedCornerShape(4.dp)))
                }
            }

            Row {
                Button(
                    onClick = { viewModel.saveCurrentFile() },
                    enabled = isDirty,
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                    modifier = Modifier.height(34.dp).testTag("save_file_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 12.sp, color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // AI Quick Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { viewModel.sendMessageToAgent("Explain the implementation in $selectedPath") },
                label = { Text("AI Explain", fontSize = 11.sp, color = DevCyan) },
                leadingIcon = { Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = DevCyan) }
            )
            AssistChip(
                onClick = { viewModel.sendMessageToAgent("Fix bugs and enhance type safety in $selectedPath") },
                label = { Text("AI Fix", fontSize = 11.sp, color = DevEmerald) },
                leadingIcon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(14.dp), tint = DevEmerald) }
            )
            AssistChip(
                onClick = { viewModel.sendMessageToAgent("Refactor and optimize code in $selectedPath") },
                label = { Text("AI Refactor", fontSize = 11.sp, color = DevViolet) },
                leadingIcon = { Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp), tint = DevViolet) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Editor Canvas with Line Numbers
        val lines = remember(content) { content.lines() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(TerminalBackground, RoundedCornerShape(8.dp))
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Line numbers gutter
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    val count = maxOf(lines.size, 1)
                    for (i in 1..count) {
                        Text(
                            text = i.toString().padStart(3, ' '),
                            color = Slate600,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Text Content
                BasicTextField(
                    value = content,
                    onValueChange = { viewModel.updateEditorContent(it) },
                    textStyle = TextStyle(
                        color = Slate100,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(DevCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_field")
                )
            }
        }
    }
}

@Composable
fun WorkspaceTerminalTab(viewModel: StudioViewModel) {
    val logs by viewModel.terminalLogs.collectAsState()
    var cmdInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(logs.length) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ROOTLESS LINUX TERMINAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Row {
                TextButton(onClick = { viewModel.clearTerminal() }) {
                    Text("Clear", color = Slate400, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Terminal Output Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(TerminalBackground, RoundedCornerShape(8.dp))
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = logs,
                color = TerminalText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick shell command chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            listOf("npm run dev", "python3 main.py", "ls -lah", "git status").forEach { chipCmd ->
                AssistChip(
                    onClick = { viewModel.executeTerminalCommand(chipCmd) },
                    label = { Text(chipCmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DevCyanLight) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
            }
        }

        // Terminal Input Line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(8.dp))
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("$ ", color = DevCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            TextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                placeholder = { Text("Enter bash command...", fontSize = 12.sp, color = Slate600) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100
                ),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                modifier = Modifier.weight(1f).testTag("terminal_input_field")
            )
            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        val c = cmdInput.trim()
                        cmdInput = ""
                        viewModel.executeTerminalCommand(c)
                    }
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = DevCyan)
            }
        }
    }
}

@Composable
fun WorkspacePreviewTab(viewModel: StudioViewModel) {
    val activePorts by viewModel.activePorts.collectAsState()
    var selectedPort by remember { mutableStateOf(3000) }
    var webViewKey by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val currentUrl = "http://127.0.0.1:$selectedPort"

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Preview Header Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PORT:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.width(6.dp))
                listOf(3000, 4173, 5173, 8000, 8080).forEach { port ->
                    FilterChip(
                        selected = selectedPort == port,
                        onClick = { selectedPort = port; webViewKey++ },
                        label = { Text(port.toString(), fontSize = 10.sp) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Row {
                IconButton(onClick = { webViewKey++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = DevCyan)
                }
                IconButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                        context.startActivity(browserIntent)
                    }
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Open External", tint = Slate400)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Real Android WebView preview container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Slate800))
        ) {
            key(webViewKey) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            webViewClient = WebViewClient()
                            loadUrl(currentUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun WorkspaceBuildTab(viewModel: StudioViewModel) {
    val builds by viewModel.projectBuilds.collectAsState()
    val buildLogs by viewModel.buildLogs.collectAsState()
    val latestApk by viewModel.latestApk.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("ON-DEVICE BUILD ENGINE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100)
                Text("Gradle & aapt2 Compilation Pipeline", fontSize = 11.sp, color = DevCyan)
            }
            Button(
                onClick = { viewModel.triggerBuild("ANDROID_DEBUG") },
                colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                modifier = Modifier.testTag("trigger_build_button")
            ) {
                Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assemble APK", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Latest APK Metadata & Package Installer Card
        if (latestApk != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(DevEmerald)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, null, tint = DevEmerald, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Generated APK Ready", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 14.sp)
                                Text(latestApk!!.packageName, fontSize = 11.sp, color = Slate400, fontFamily = FontFamily.Monospace)
                            }
                        }
                        StatusBadge(text = "${latestApk!!.sizeBytes / 1024} KB", color = DevEmerald)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.installApk(latestApk!!.apkFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                            modifier = Modifier.weight(1f).testTag("install_apk_button")
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Install via Package Installer", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.launchInstalledApp(latestApk!!.packageName) },
                            modifier = Modifier.weight(0.6f)
                        ) {
                            Text("Launch App", color = DevCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Live Build Console Logs
        Text("COMPILATION LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(TerminalBackground, RoundedCornerShape(8.dp))
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = if (buildLogs.isBlank()) "Build logs will appear here when compilation starts." else buildLogs,
                color = TerminalText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun WorkspaceGitTab(viewModel: StudioViewModel) {
    val gitStatus by viewModel.gitStatusText.collectAsState()
    val checkpoints by viewModel.projectCheckpoints.collectAsState()
    var commitMessage by remember { mutableStateOf("") }
    var checkpointTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())) {
        Text("GIT VERSION CONTROL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Spacer(modifier = Modifier.height(8.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Branch: main", fontWeight = FontWeight.Bold, color = DevCyan, fontSize = 13.sp)
                    IconButton(onClick = { viewModel.refreshGitStatus() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Slate400)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = gitStatus,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate200,
                    modifier = Modifier.fillMaxWidth().background(Slate950, RoundedCornerShape(6.dp)).padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("Commit message...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (commitMessage.isNotBlank()) {
                            viewModel.runGitCommit(commitMessage.trim())
                            commitMessage = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Commit Changes", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checkpoints Snapshot Manager
        Text("PROJECT CHECKPOINTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Spacer(modifier = Modifier.height(8.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Create Snapshot Checkpoint", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = checkpointTitle,
                        onValueChange = { checkpointTitle = it },
                        placeholder = { Text("Checkpoint name (e.g. Before AI refactor)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (checkpointTitle.isNotBlank()) {
                                viewModel.createCheckpoint(checkpointTitle.trim())
                                checkpointTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DevViolet)
                    ) {
                        Text("Snapshot", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (checkpoints.isEmpty()) {
                    Text("No snapshots created yet.", color = Slate600, fontSize = 12.sp)
                } else {
                    checkpoints.forEach { cp ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(cp.title, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 12.sp)
                                Text("${cp.filesChangedCount} files snapshotted", color = Slate400, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.restoreCheckpoint(cp) },
                                colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Restore", color = Slate950, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
