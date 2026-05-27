package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EventType
import com.example.domain.model.SystemEvent
import com.example.domain.model.GeneratedFile

import androidx.compose.ui.platform.LocalContext
import com.example.data.storage.FileExportManager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import com.example.ui.components.premium.DynamicGlassCard
import com.example.ui.components.premium.bounceClick

@Composable
fun SystemEventItem(event: SystemEvent, onPreviewFile: (GeneratedFile) -> Unit) {
    val context = LocalContext.current
    val exportManager = remember { FileExportManager(context) }
    var isExpanded by remember { mutableStateOf(false) }
    
    val canExpand = event.type == EventType.SEARCH_RESULT || event.type == EventType.SEARCH_START

    DynamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(if (canExpand) Modifier.bounceClick { isExpanded = !isExpanded } else Modifier),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (icon, tint) = when (event.type) {
                    EventType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
                    EventType.TOOL_CALL -> Icons.Default.Build to MaterialTheme.colorScheme.tertiary
                    EventType.FILE_GENERATION -> Icons.AutoMirrored.Filled.InsertDriveFile to MaterialTheme.colorScheme.secondary
                    EventType.WORKFLOW_START -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.onSurface
                    EventType.WORKFLOW_END -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                    EventType.ERROR -> Icons.Default.Warning to MaterialTheme.colorScheme.error
                    EventType.SEARCH_START -> Icons.Default.Search to MaterialTheme.colorScheme.primary
                    EventType.SEARCH_RESULT -> Icons.Default.Link to MaterialTheme.colorScheme.tertiary
                    EventType.SEARCH_END -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                }
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(tint.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isExpanded && canExpand) "Research Context" else event.message.substringBefore("\n"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                if (canExpand) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Box(modifier = Modifier.padding(16.dp)) {
                    PremiumMarkdownRenderer(markdown = event.message)
                }
            }
            
            if (event.files.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 56.dp, end = 12.dp, bottom = 12.dp)) {
                    event.files.forEach { file ->
                        GeneratedFileCard(
                            file = file, 
                            onPreview = { onPreviewFile(file) }, 
                            onDownload = { exportManager.shareFile(file) },
                            onOpen = { exportManager.openFile(file) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedFileCard(
    file: com.example.domain.model.GeneratedFile,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onOpen: () -> Unit
) {
    DynamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = file.format.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPreview, modifier = Modifier.bounceClick { onPreview() }) {
                Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDownload, modifier = Modifier.bounceClick { onDownload() }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onOpen, modifier = Modifier.bounceClick { onOpen() }) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Externally", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
