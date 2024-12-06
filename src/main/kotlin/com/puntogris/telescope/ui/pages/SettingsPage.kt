package com.puntogris.telescope.ui.pages

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.*
import com.intellij.util.io.*
import com.puntogris.telescope.domain.GlobalStorage
import com.puntogris.telescope.models.DslComponent
import com.puntogris.telescope.utils.configPath
import com.puntogris.telescope.utils.sendNotification
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*
import kotlin.io.path.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

private const val DEFAULT_VISION_MODEL_URL =
    "https://huggingface.co/mys/ggml_CLIP-ViT-B-32-laion2B-s34B-b79K/resolve/main/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f32.gguf"

private const val DEFAULT_TEXT_MODEL_URL =
    "https://huggingface.co/mys/ggml_CLIP-ViT-B-32-laion2B-s34B-b79K/resolve/main/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f16.gguf"

class SettingsPage(private val project: Project) : DslComponent {

    private lateinit var defaultButton: Cell<JBRadioButton>
    private lateinit var advancedButton: Cell<JBRadioButton>
    private var value = true

    override fun createContent(): JComponent {
        return panel {

            row {
                text("Settings")
            }
            group("AI models configuration") {
                row {
                    comment("Choose the configuration you would like to use, the default one should be preferred.")
                }
                buttonsGroup {
                    row {
                        defaultButton = radioButton("Default models (recommended)", true)
                    }
                    row {
                        advancedButton = radioButton("Advanced models", false)
                    }
                }.bind({ value }, { value = it })
                separator()
                defaultPanel().visibleIf(defaultButton.selected)
                advancedPanel().visibleIf(advancedButton.selected)
            }
        }.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    }

    private fun Panel.defaultPanel(): Panel = panel {
        row {
            text("This model was trained and optimized specifically for this plugin's tasks.")
        }
        row {
            button("Download models") {
                downloadModels()
            }.comment(
                "Models are about 100MB each and we will store at:\n${
                    configPath.resolve("models").absolutePathString()
                }"
            )
        }
    }

    private fun Panel.advancedPanel() = panel {
        var textModelComment: Cell<JEditorPane>? = null
        var visionModelComment: Cell<JEditorPane>? = null
        val textModelInit = GlobalStorage.getTextModelPath().ifEmpty { "None selected" }
        val visionModelInit = GlobalStorage.getVisionModelPath().ifEmpty { "None selected" }

        row {
            text("To experiment and if you know what you are doing")
        }
        row {
            button("Select text model") {
                chooseCustomModel(project).onSuccess {
                    GlobalStorage.setTextModelPath(it)
                    textModelComment?.text(it)
                }
            }
            textModelComment = comment(textModelInit)
        }
        row {
            button("Select vision model") {
                chooseCustomModel(project).onSuccess {
                    GlobalStorage.setVisionModelPath(it)
                    visionModelComment?.text(it)
                }
            }
            visionModelComment = comment(visionModelInit)
        }
        row {
            text("About models").bold()
        }
        row {
            text("This plugin uses OpenCLIP compatible models and converted to gguf format. Keep in mind that this models will need to be load into memory so bigger models will be more demanding.")
        }
        row {
            browserLink("Here are a few models available in HuggingFaces", "")
        }
    }

    private fun chooseCustomModel(project: Project): Result<String> {
        val fileDescription = FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        ).withFileFilter {
            it.extension == "gguf"
        }
        val file = FileChooser.chooseFile(fileDescription, project, null)

        return if (file != null) {
            Result.success(file.path)
        } else {
            Result.failure(IOException("File selection failed: No file selected"))
        }
    }

    private fun downloadModels() {
        runBackgroundableTask("Downloading text model", project, cancellable = true) {
            downloadFileWithProgress(DEFAULT_TEXT_MODEL_URL, configPath.resolve("models"), it)

            if (it.isCanceled) {
                sendNotification(project, "Telescope text model download canceled", NotificationType.WARNING)
            } else {
                sendNotification(project, "Telescope text model download completed", NotificationType.INFORMATION)
            }
        }

        runBackgroundableTask("Downloading vision model", project, cancellable = true) {
            downloadFileWithProgress(DEFAULT_VISION_MODEL_URL, configPath.resolve("models"), it)

            if (it.isCanceled) {
                sendNotification(project, "Telescope vision model download canceled", NotificationType.WARNING)
            } else {
                sendNotification(project, "Telescope vision model download completed", NotificationType.INFORMATION)
            }
        }
    }

    private fun downloadFileWithProgress(url: String, destinationDir: Path, indicator: ProgressIndicator) {
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
                return
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
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotification(project, "Telescope AI models download failed", NotificationType.ERROR)
        }
    }
}
