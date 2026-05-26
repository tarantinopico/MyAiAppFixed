package com.example.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.GeneratedFile
import java.io.File
import java.io.FileOutputStream

class FileExportManager(private val context: Context) {

    private val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }

    fun shareFile(file: GeneratedFile) {
        try {
            val localFile = File(exportDir, file.name)
            FileOutputStream(localFile).use { it.write(file.content.toByteArray()) }
            
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                if (file.format == "html") type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun openFile(file: GeneratedFile) {
         try {
            val localFile = File(exportDir, file.name)
            FileOutputStream(localFile).use { it.write(file.content.toByteArray()) }
            
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/*")
                if (file.format == "html") setDataAndType(uri, "text/html")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open ${file.name}"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun shareConversation(conversationTitle: String, messages: List<com.example.domain.model.ChatMessage>) {
        try {
            val content = java.lang.StringBuilder()
            content.append("# ${conversationTitle}\n\n")
            messages.forEach {
                content.append("**${it.role.name}**\n")
                content.append("${it.content}\n\n")
            }
            
            val localFile = File(exportDir, "${conversationTitle.replace(" ", "_")}.md")
            FileOutputStream(localFile).use { it.write(content.toString().toByteArray()) }
            
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Conversation"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
