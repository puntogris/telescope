package com.puntogris.telescope.ui.pages

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.puntogris.telescope.ui.components.Hyperlink
import com.puntogris.telescope.ui.components.PathComponent
import java.awt.*
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import javax.swing.*
import kotlin.io.path.absolutePathString

class SettingsPage(private val project: Project) : JPanel() {

    private val pathComponent = PathComponent(project)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        add(title())
        add(subtitle())
        add(Box.createRigidArea(Dimension(0, 10)))
        add(pathComponent)
        add(Box.createRigidArea(Dimension(0, 10)))

        val downloadLabel = JLabel("If you don't want to use a custom one i recommend you this one at only 85MB")
        val downloadButton = JButton("Download default and apply")

        downloadButton.addActionListener {
            startDownload()
        }
        add(downloadLabel)
        add(downloadButton)

        add(Box.createRigidArea(Dimension(0, 10)))

        val info = JLabel("This uses CLIP compatible models, you can check more about them here:")
        add(info)
        add(Hyperlink("https://huggingface.co/models?other=clip-cpp-gguf"))
    }

    private fun title(): JLabel {
        return JBLabel("Settings").apply {
            font = font.deriveFont(18f)
            alignmentX = LEFT_ALIGNMENT
        }
    }

    private fun subtitle(): JLabel {
        return JBLabel("Set the path to your AI model for generating image embeddings.").apply {
            alignmentX = LEFT_ALIGNMENT
        }
    }

    private fun chooseFolder(project: Project): String {
        val fileDescription = FileChooserDescriptor(false, true, false, false, false, false)
        fileDescription.withFileFilter {
            it.extension == "gguf"
        }
        return FileChooser.chooseFile(fileDescription, project, null)?.path ?: ""
    }

    private fun startDownload() {
        val folder = chooseFolder(project)
        if (folder.isEmpty()) {
            showMessageDialog("Folder invalid!")
            return
        }
        val url =
            "https://huggingface.co/mys/ggml_CLIP-ViT-B-32-laion2B-s34B-b79K/resolve/main/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f16.gguf"
        val name = "CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f16.gguf"
        val path = "${folder}/${name}"

        object : Task.Backgroundable(project, "Downloading model", true) {
            override fun run(indicator: ProgressIndicator) {
                val fileUrl = URI(url).toURL()
                val destinationPath = Paths.get(path)

                downloadFileWithProgress(fileUrl, destinationPath, indicator)
            }
        }.queue()
    }

    private fun downloadFileWithProgress(url: URL, destinationPath: Path, indicator: ProgressIndicator) {
        try {
            val inputStream = url.openStream()
            val outputStream = Files.newOutputStream(destinationPath)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0
            val contentLength = url.openConnection().contentLengthLong

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

            if (indicator.isCanceled) {
                showMessageDialog("Download Canceled!")
            } else {
                showMessageDialog("Download Complete!")
                pathComponent.updatePath(destinationPath.absolutePathString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            showMessageDialog("Error downloading file!")
        }
    }

    private fun showMessageDialog(message: String) {
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(this, message)
        }
    }
}
