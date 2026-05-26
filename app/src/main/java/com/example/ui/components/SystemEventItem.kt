package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun SystemEventItem(event: SystemEvent, onPreviewFile: (GeneratedFile) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (icon, tint) = when (event.type) {
                EventType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
                EventType.TOOL_CALL -> Icons.Default.Build to MaterialTheme.colorScheme.tertiary
                EventType.FILE_GENERATION -> Icons.Default.InsertDriveFile to MaterialTheme.colorScheme.secondary
                EventType.WORKFLOW_START -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.onSurface
                EventType.WORKFLOW_END -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                EventType.ERROR -> Icons.Default.Warning to MaterialTheme.colorScheme.error
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
                    text = event.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.files.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    event.files.forEach { file ->
                        GeneratedFileCard(
                            file = file, 
                            onPreview = { onPreviewFile(file) }, 
                            onDownload = {}
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
    onDownload: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
            IconButton(onClick = onPreview) {
                Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
