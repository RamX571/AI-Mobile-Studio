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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: StudioViewModel,
    onNavigateToWorkspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val health by viewModel.runtimeHealth.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("projects_screen"),
        containerColor = Slate950,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = DevCyan,
                contentColor = Slate950,
                icon = { Icon(Icons.Default.Add, contentDescription = "New Project") },
                text = { Text("New Project", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_project_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Studio Header & System Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Developer Workspace",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "On-Device PRoot Linux • ARM64 Native",
                                fontSize = 12.sp,
                                color = DevCyan
                            )
                        }
                        StatusBadge(
                            text = if (health?.isArm64 == true) "ARM64 OK" else "COMPAT",
                            color = DevEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatusBadge(
                            text = "${projects.size} Projects",
                            color = DevCyan
                        )
                        StatusBadge(
                            text = "${health?.activeProcessCount ?: 0} Active Proc",
                            color = DevViolet
                        )
                        StatusBadge(
                            text = "${health?.availableStorageMb ?: 0} MB Free",
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "YOUR PROJECTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (projects.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No projects yet",
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create a React, Next.js, Node, Python, or Android workspace to begin.",
                            color = Slate600,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DevCyan)
                        ) {
                            Text("Create First Project", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onClick = {
                                viewModel.openProject(project.id)
                                onNavigateToWorkspace()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type, perm ->
                viewModel.createProject(name, type, perm)
                showCreateDialog = false
                onNavigateToWorkspace()
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    val dateStr = remember(project.lastOpenedAt) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(project.lastOpenedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_item_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (project.type.lowercase()) {
                        "react", "vite", "next.js" -> Icons.Default.Web
                        "python" -> Icons.Default.Psychology
                        "android/kotlin", "android/java" -> Icons.Default.PhoneAndroid
                        "c/c++" -> Icons.Default.Terminal
                        else -> Icons.Default.Folder
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Slate850, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = DevCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate100
                        )
                        Text(
                            text = "${project.type} • Active agent: ${project.activeAgentId}",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }

                StatusBadge(text = project.status, color = if (project.status == "IDLE") DevEmerald else DevAmber)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Opened $dateStr",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open Workspace",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DevCyan
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = DevCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var projectName by remember { mutableStateOf("My Mobile App") }
    var selectedType by remember { mutableStateOf("React") }
    var selectedPerm by remember { mutableStateOf("ASK_SENSITIVE") }

    val projectTypes = listOf(
        "React",
        "Next.js",
        "Vite",
        "Node.js",
        "Static HTML/CSS/JS",
        "Python",
        "Android/Kotlin",
        "Android/Java",
        "C/C++",
        "PHP",
        "Generic Linux"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Project Workspace",
                fontWeight = FontWeight.Bold,
                color = Slate100
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Project Template & Toolchain", fontSize = 12.sp, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        projectTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Security & Permission Mode", fontSize = 12.sp, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SAFE", "ASK_SENSITIVE", "TRUSTED").forEach { mode ->
                        FilterChip(
                            selected = selectedPerm == mode,
                            onClick = { selectedPerm = mode },
                            label = { Text(mode.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        onCreate(projectName.trim(), selectedType, selectedPerm)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                modifier = Modifier.testTag("submit_create_project_button")
            ) {
                Text("Create Workspace", color = Slate950, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = Slate900
    )
}
