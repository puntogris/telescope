package com.puntogris.telescope.utils

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.io.isFile
import com.intellij.util.io.size
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists

fun openFileChooser(project: Project, vararg extension: String): Result<String> {
    val fileDescription = FileChooserDescriptor(
        true,
        false,
        false,
        false,
        false,
        false
    ).apply {
        if (extension.isNotEmpty()) {
            withFileFilter { it.extension in extension }
        }
    }

    val file = FileChooser.chooseFile(fileDescription, project, null)

    return if (file != null) {
        Result.success(file.path)
    } else {
        Result.failure(IOException("File selection failed: No file selected"))
    }
}

fun downloadFileWithProgress(
    url: String,
    destinationDir: Path,
    indicator: ProgressIndicator
): Result<String> {
    try {
        val downloadUrl = URI(url).toURL()

        if (destinationDir.notExists()) {
            destinationDir.createDirectories()
        }

        val fileName = downloadUrl.path.substringAfterLast("/")
        val destinationFile = destinationDir.resolve(fileName)

        var localFileSize = 0L

        if (destinationFile.exists() && destinationFile.isFile()) {
            localFileSize = destinationFile.size()
        }

        val inputStream = downloadUrl.openStream()
        val contentLength = downloadUrl.openConnection().contentLengthLong

        if (localFileSize == contentLength) {
            inputStream.close()
            return Result.success(destinationDir.absolutePathString())
        }

        var bytesRead: Int
        var totalBytesRead = 0
        val buffer = ByteArray(4096)
        val outputStream = Files.newOutputStream(destinationFile)

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            if (indicator.isCanceled) {
                break
            }

            outputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            val progress = totalBytesRead.toDouble() / contentLength
            indicator.fraction = progress
        }

        inputStream.close()
        outputStream.close()
        return Result.success(destinationDir.absolutePathString())
    } catch (e: Exception) {
        return Result.failure(e)
    }
}