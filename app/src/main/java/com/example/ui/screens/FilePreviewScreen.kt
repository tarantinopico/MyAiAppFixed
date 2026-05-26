package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.GeneratedFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
    file: GeneratedFile,
    onBack: () -> Unit
) {
    var isRenderMode by remember { mutableStateOf(file.format.lowercase() == "html") }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    
                    if (file.format.lowercase() == "html") {
                        IconButton(onClick = { isRenderMode = !isRenderMode }) {
                            Icon(if (isRenderMode) Icons.Default.Code else Icons.Default.Preview, contentDescription = "Toggle")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                if (isRenderMode && file.format.lowercase() == "html") {
                    HtmlWebView(content = file.content)
                } else {
                    // Simple Code Viewer
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = file.content,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HtmlWebView(content: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
        }
    )
}
