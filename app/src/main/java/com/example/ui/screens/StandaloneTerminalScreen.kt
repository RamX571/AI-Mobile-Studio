package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun StandaloneTerminalScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.terminalLogs.collectAsState()
    var cmdInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(logs.length) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        modifier = modifier.testTag("standalone_terminal_screen"),
        containerColor = Slate950,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = DevCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terminal Session #1",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(text = "ARM64 PTY", color = DevEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.clearTerminal() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = Slate400)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            // Main Terminal Console Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(TerminalBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = logs,
                    color = TerminalText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Bar: Quick Commands & Keys
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.executeTerminalCommand("echo 'SIGINT (Ctrl+C)'") },
                    label = { Text("Ctrl+C", fontSize = 11.sp, color = DevRose) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
                AssistChip(
                    onClick = { viewModel.executeTerminalCommand("uname -m && id") },
                    label = { Text("arch & user", fontSize = 11.sp, color = DevCyan) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
                AssistChip(
                    onClick = { viewModel.executeTerminalCommand("ls -lah") },
                    label = { Text("ls -la", fontSize = 11.sp, color = DevViolet) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
                AssistChip(
                    onClick = { viewModel.executeTerminalCommand("ps") },
                    label = { Text("ps", fontSize = 11.sp, color = DevEmerald) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Slate900)
                )
            }

            // Command Prompt Field
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
                    placeholder = { Text("Enter Linux command...", fontSize = 12.sp, color = Slate600) },
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
                    modifier = Modifier.weight(1f).testTag("terminal_exec_field")
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
                    Icon(Icons.Default.Send, contentDescription = "Run", tint = DevCyan)
                }
            }
        }
    }
}
