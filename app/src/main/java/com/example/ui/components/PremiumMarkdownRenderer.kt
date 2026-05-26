package com.example.ui.components

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun PremiumMarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                if (textColor != Color.Unspecified) {
                    setTextColor(textColor.toArgb())
                }
                textSize = 16f
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown)
            if (textColor != Color.Unspecified) {
                textView.setTextColor(textColor.toArgb())
            }
        }
    )
}
