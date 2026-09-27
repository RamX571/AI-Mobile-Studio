package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.ProviderEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun AgentsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val agents by viewModel.agents.collectAsState()
    val providers by viewModel.providers.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var editingProvider by remember { mutableStateOf<ProviderEntity?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("agents_screen"),
        containerColor = Slate950,
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = DevCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI Agents & Model Providers",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate900,
                    contentColor = DevCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Agent Registry (${agents.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Model Providers (${providers.size})", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                Text(
                    text = "DISCOVERED CODING AGENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(agents, key = { it.id }) { agent ->
                        AgentCard(agent)
                    }
                }
            } else {
                Text(
                    text = "CONFIGURED MODEL PROVIDERS & SECRETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔒 Secrets stored strictly via hardware-backed Android Keystore AES-256.",
                    fontSize = 11.sp,
                    color = DevEmerald
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(providers, key = { it.id }) { provider ->
                        val maskedKey = viewModel.getMaskedKey(provider.id)
                        ProviderCard(
                            provider = provider,
                            maskedKey = maskedKey,
                            onConfigureKey = {
                                editingProvider = provider
                                apiKeyInput = ""
                            }
                        )
                    }
                }
            }
        }
    }

    if (editingProvider != null) {
        val p = editingProvider!!
        AlertDialog(
            onDismissRequest = { editingProvider = null },
            title = { Text("Configure ${p.name} API Key", color = Slate100) },
            text = {
                Column {
                    Text(
                        text = "Encrypted in Android KeyStore. Never written to source code or shared files.",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key") },
                        placeholder = { Text("sk-...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProviderKey(p.id, apiKeyInput.trim())
                        editingProvider = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan)
                ) {
                    Text("Save to KeyStore", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProvider = null }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun AgentCard(agent: AgentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = DevCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = agent.displayName, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 15.sp)
                }
                StatusBadge(text = agent.status, color = DevEmerald)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Executable: /usr/local/bin/${agent.executable}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Install: ${agent.installMethod}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge(text = agent.architecture.uppercase(), color = DevViolet)
                StatusBadge(text = "Auth: ${agent.authMethod}", color = Slate400)
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: ProviderEntity,
    maskedKey: String,
    onConfigureKey: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (provider.isConfigured) DevEmerald.copy(alpha = 0.5f) else Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = provider.name, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 15.sp)
                StatusBadge(
                    text = if (provider.isConfigured) "CONFIGURED" else "MISSING KEY",
                    color = if (provider.isConfigured) DevEmerald else DevRose
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Endpoint: ${provider.baseUrl}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Key: $maskedKey",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (provider.isConfigured) DevEmerald else Slate600
                )
                Button(
                    onClick = onConfigureKey,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Set Key", color = DevCyan, fontSize = 11.sp)
                }
            }
        }
    }
}
