package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    var selectedGlobalPerm by remember { mutableStateOf("ASK_SENSITIVE") }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        containerColor = Slate950,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = DevCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Studio Settings & Security",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Permission Model Section
            Text("AI EXECUTION PERMISSION MODEL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PermModeOption(
                        title = "Ask Before Sensitive (Recommended)",
                        description = "Prompts for approval before installing packages, deleting files, running arbitrary scripts, or installing APKs.",
                        selected = selectedGlobalPerm == "ASK_SENSITIVE",
                        color = DevCyan,
                        onClick = { selectedGlobalPerm = "ASK_SENSITIVE" }
                    )
                    Divider(color = Slate800)
                    PermModeOption(
                        title = "Safe Mode (Strict Sandbox)",
                        description = "Blocks all shell commands, system modifications, network requests, and external tool installations.",
                        selected = selectedGlobalPerm == "SAFE",
                        color = DevEmerald,
                        onClick = { selectedGlobalPerm = "SAFE" }
                    )
                    Divider(color = Slate800)
                    PermModeOption(
                        title = "Trusted Project (Unrestricted)",
                        description = "Runs all agent tool calls and commands automatically without confirmation prompts.",
                        selected = selectedGlobalPerm == "TRUSTED",
                        color = DevAmber,
                        onClick = { selectedGlobalPerm = "TRUSTED" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Application Workspace Storage
            Text("WORKSPACE STORAGE & PATHS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Private Internal Storage", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "/data/user/0/com.aistudio.aimobilestudio.qvxrzt/files/app-workspace/",
                        fontSize = 11.sp,
                        color = DevCyan,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Projects are stored in application-private sandbox directories for security and performance.",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Onboarding Re-run
            Text("ONBOARDING & SETUP WIZARD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column {
                        Text("System Setup Wizard", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                        Text("Re-run the 6-step initialization checks", fontSize = 12.sp, color = Slate400)
                    }
                    Button(
                        onClick = { viewModel.setOnboardingStep(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                    ) {
                        Text("Launch", color = DevCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI Mobile Development Studio • v1.0.0 (ARM64 Native)",
                fontSize = 11.sp,
                color = Slate600,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PermModeOption(
    title: String,
    description: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = color, unselectedColor = Slate600)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = if (selected) color else Slate100, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = Slate400, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
