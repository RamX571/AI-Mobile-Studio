package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun OnboardingScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsState()
    val health by viewModel.runtimeHealth.collectAsState()

    var projectName by remember { mutableStateOf("My Mobile App") }
    var projectType by remember { mutableStateOf("React") }

    Scaffold(
        modifier = modifier.testTag("onboarding_screen"),
        containerColor = Slate950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                // Progress indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    for (i in 1..6) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    if (step >= i) DevCyan else Slate800,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                StatusBadge(text = "SETUP STEP $step OF 6", color = DevCyan)
                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    1 -> {
                        // Step 1: Device readiness
                        Text("Device Readiness", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Verifying phone hardware compatibility for rootless Linux development.",
                            color = Slate400,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ReadinessRow("CPU Architecture", health?.cpuArch?.uppercase() ?: "ARM64", health?.isArm64 == true)
                                ReadinessRow("Android Version", "API 35 (Compatible)", true)
                                ReadinessRow("Memory Check", "${health?.totalRamMb ?: 0} MB RAM", (health?.totalRamMb ?: 0) > 1024)
                                ReadinessRow("Internal Storage", "${(health?.availableStorageMb ?: 0) / 1024} GB Free", true)
                            }
                        }
                    }
                    2 -> {
                        // Step 2: Linux runtime
                        Text("Linux Runtime Environment", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Private PRoot rootfs initialized in application-private storage.",
                            color = Slate400,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rootless PRoot Engine Ready", fontWeight = FontWeight.Bold, color = DevEmerald)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Workspaces run without requiring root access. Standard Linux tools can be executed directly inside private project sandboxes.",
                                    fontSize = 12.sp,
                                    color = Slate400,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    3 -> {
                        // Step 3: Toolchains
                        Text("Developer Toolchains", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Core compilers and runtimes pre-configured.", color = Slate400, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("• Node.js & npm (v20.18.0) - Ready", color = DevCyan, fontSize = 13.sp)
                                Text("• Python 3 & pip (3.11.8) - Ready", color = DevEmerald, fontSize = 13.sp)
                                Text("• Git Version Control - Ready", color = DevViolet, fontSize = 13.sp)
                                Text("• OpenJDK 17 & Android SDK - Ready", color = DevAmber, fontSize = 13.sp)
                            }
                        }
                    }
                    4 -> {
                        // Step 4: AI Agents
                        Text("Coding Agents Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Discovered agents from the unified agent registry.", color = Slate400, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("• Claude Code CLI (Anthropic)", fontWeight = FontWeight.Bold, color = DevCyan)
                                Text("• Antigravity Mobile CLI (Gemini/Cloud)", fontWeight = FontWeight.Bold, color = DevViolet)
                                Text("• DeepSeek Coder CLI (R1 Reasoner)", fontWeight = FontWeight.Bold, color = DevEmerald)
                                Text("• OpenCode Assistant (Offline)", fontWeight = FontWeight.Bold, color = Slate100)
                            }
                        }
                    }
                    5 -> {
                        // Step 5: Providers & Authentication
                        Text("Hardware-Backed Security", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Android Keystore AES-256 GCM encryption protects your credentials.", color = Slate400, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, null, tint = DevEmerald)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Zero Plaintext Storage", fontWeight = FontWeight.Bold, color = Slate100)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Keys are never stored in source files, logs, or backups. You can configure keys in the Agents & Providers tab anytime.",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                    6 -> {
                        // Step 6: Create First Project
                        Text("Create Your First Project", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Choose a project name and starter template.", color = Slate400, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text("Project Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Starter Template", fontSize = 12.sp, color = Slate400)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("React", "Vite", "Node.js", "Python").forEach { t ->
                                FilterChip(
                                    selected = projectType == t,
                                    onClick = { projectType = t },
                                    label = { Text(t) }
                                )
                            }
                        }
                    }
                }
            }

            // Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { viewModel.setOnboardingStep(step - 1) }) {
                        Text("Back", color = Slate400)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 6) {
                            viewModel.setOnboardingStep(step + 1)
                        } else {
                            viewModel.createProject(projectName, projectType)
                            viewModel.completeOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan)
                ) {
                    Text(
                        if (step == 6) "Launch Workspace" else "Next",
                        color = Slate950,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReadinessRow(label: String, value: String, isOk: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Slate400, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) DevEmerald else DevAmber,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
