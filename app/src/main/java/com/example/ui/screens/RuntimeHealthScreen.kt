package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun RuntimeHealthScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val health by viewModel.runtimeHealth.collectAsState()
    val toolchains by viewModel.toolchains.collectAsState()
    val activePorts by viewModel.activePorts.collectAsState()

    Scaffold(
        modifier = modifier.testTag("runtime_health_screen"),
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
                    Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = DevCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Runtime Health & System",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }
                IconButton(onClick = { viewModel.refreshHealth() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = DevCyan)
                }
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
            // Security Notice Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DevAmber)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = DevAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Security & Isolation Architecture", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PRoot is a userspace compatibility layer using ptrace/syscall translation, not a hardware virtualization boundary. Workspaces run inside application-private storage.",
                            fontSize = 11.sp,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Metrics Grid
            Text("DEVICE & RUNTIME METRICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = "CPU Arch",
                    value = health?.cpuArch?.uppercase() ?: "ARM64",
                    subtext = if (health?.isArm64 == true) "Native 64-bit" else "Emulated",
                    color = DevCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "RAM Available",
                    value = "${health?.freeRamMb ?: 0} MB",
                    subtext = "Total: ${health?.totalRamMb ?: 0} MB",
                    color = DevEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = "Free Storage",
                    value = "${(health?.availableStorageMb ?: 0) / 1024} GB",
                    subtext = "Workspace Private FS",
                    color = DevViolet,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Background Svc",
                    value = if (health?.isForegroundServiceRunning == true) "ACTIVE" else "READY",
                    subtext = "Task Persistence",
                    color = if (health?.isForegroundServiceRunning == true) DevEmerald else Slate400,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Ports & Local Servers
            Text("ACTIVE LOCAL SERVERS & NETWORK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Slate900), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (activePorts.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lan, contentDescription = null, tint = Slate600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No background web servers currently listening on localhost.", fontSize = 12.sp, color = Slate400)
                        }
                    } else {
                        activePorts.forEach { port ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(DevEmerald, RoundedCornerShape(4.dp)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("localhost:${port.portNumber}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("(${port.serviceName})", fontSize = 11.sp, color = Slate400)
                                }
                                StatusBadge(text = port.protocol, color = DevCyan)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toolchains Manager
            Text("DEVELOPMENT TOOLCHAINS (${toolchains.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                toolchains.forEach { tool ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tool.name, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(text = tool.category, color = Slate400)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Version: ${tool.installedVersion ?: "Not Installed"} (${tool.sizeMb} MB)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (tool.isInstalled) DevEmerald else Slate600
                                )
                            }
                            StatusBadge(
                                text = if (tool.isInstalled) "INSTALLED" else "AVAILABLE",
                                color = if (tool.isInstalled) DevEmerald else DevCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = Slate600)
        }
    }
}
