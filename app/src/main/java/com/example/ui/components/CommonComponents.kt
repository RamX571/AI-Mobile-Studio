package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ToolCallEntity
import com.example.git.DiffLine
import com.example.git.DiffType
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    text: String,
    color: Color = DevCyan,
    bgColor: Color = Slate850,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ToolCallCard(
    toolCall: ToolCallEntity,
    onApprove: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (toolCall.toolName.lowercase()) {
        "editfile", "fileedit" -> Icons.Default.Edit to DevCyan
        "readfile", "readprojectconfig" -> Icons.Default.Description to DevViolet
        "executetests", "runtests" -> Icons.Default.CheckCircle to DevEmerald
        "executecommand", "runcommand" -> Icons.Default.Terminal to DevAmber
        else -> Icons.Default.Build to DevCyan
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("tool_call_${toolCall.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (toolCall.status == "WAITING_APPROVAL") DevAmber else Slate800
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toolCall.toolName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate100
                    )
                }
                StatusBadge(
                    text = toolCall.status,
                    color = when (toolCall.status) {
                        "COMPLETED" -> DevEmerald
                        "WAITING_APPROVAL" -> DevAmber
                        "FAILED" -> DevRose
                        else -> DevCyan
                    }
                )
            }

            if (toolCall.inputJson.isNotBlank() && toolCall.inputJson != "{}") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = toolCall.inputJson,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate400,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }

            if (!toolCall.outputJson.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = toolCall.outputJson,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DevEmerald,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }

            if (toolCall.status == "WAITING_APPROVAL") {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "⚠️ Sensitive operation requires explicit permission.",
                    fontSize = 12.sp,
                    color = DevAmber,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onApprove(false) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("deny_tool_call_${toolCall.id}")
                    ) {
                        Text("Deny", color = DevRose)
                    }
                    Button(
                        onClick = { onApprove(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                        modifier = Modifier.testTag("approve_tool_call_${toolCall.id}")
                    ) {
                        Text("Approve & Run", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DiffViewer(diffLines: List<DiffLine>, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate950, RoundedCornerShape(8.dp))
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .horizontalScroll(scrollState)
    ) {
        if (diffLines.isEmpty()) {
            Text(
                text = "No file differences detected.",
                color = Slate400,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            diffLines.forEach { line ->
                val (bg, textColor) = when (line.type) {
                    DiffType.ADDED -> Color(0xFF064E3B).copy(alpha = 0.5f) to Color(0xFF6EE7B7)
                    DiffType.REMOVED -> Color(0xFF881337).copy(alpha = 0.5f) to Color(0xFFFDA4AF)
                    DiffType.HEADER -> Slate800 to DevCyan
                    DiffType.SAME -> Color.Transparent to Slate200
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    val lineNumStr = (line.newLineNum ?: line.oldLineNum)?.toString() ?: ""
                    Text(
                        text = lineNumStr.padStart(3, ' '),
                        color = Slate600,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
