package com.puntogris.telescope.ui.pages

import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.actionListener
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.text
import com.puntogris.telescope.application.SettingsEvents
import com.puntogris.telescope.services.SyncService
import com.puntogris.telescope.storage.GlobalStorage
import com.puntogris.telescope.utils.GGUF
import com.puntogris.telescope.utils.configPath
import com.puntogris.telescope.utils.downloadFileWithProgress
import com.puntogris.telescope.utils.openFileChooser
import com.puntogris.telescope.utils.sendNotification
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JEditorPane
import kotlin.io.path.absolutePathString

private const val BASE_MODEL_URL = "https://huggingface.co/mys/ggml_CLIP-ViT-B-32-laion2B-s34B-b79K/resolve/main/"
private const val VISION_MODEL_URL = BASE_MODEL_URL + "CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-vision-model-q4_1.gguf"
private const val TEXT_MODEL_URL = BASE_MODEL_URL + "CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-text-model-q4_1.gguf"
private const val GITHUB_URL = "https://github.com/puntogris/telescope"
private const val PUNTOGRIS_URL = "https://www.puntogris.com"

class SettingsPage(private val project: Project) {

    private val syncService = SyncService.getInstance(project)

    private lateinit var defaultButton: Cell<JBRadioButton>
    private lateinit var advancedButton: Cell<JBRadioButton>
    private var useDefaultModels = GlobalStorage.getUseDefaultModels()

    fun createPage(): JComponent {
        return panel {
            row {
                text("Settings")
            }
            group("AI models configuration. (experimental)", indent = false) {
                row {
                    comment("Choose the configuration you would like to use, the default one should be preferred.")
                }
                buttonsGroup {
                    row {
                        defaultButton = radioButton("Default models (recommended)", true).actionListener { _, _ ->
                            GlobalStorage.setUseDefaultModels(true)
                        }
                    }
                    row {
                        advancedButton = radioButton("Advanced models", false).actionListener { _, _ ->
                            GlobalStorage.setUseDefaultModels(false)
                        }
                    }
                }.bind({ useDefaultModels }, { useDefaultModels = it })
                separator()
                defaultPanel().visibleIf(defaultButton.selected)
                advancedPanel().visibleIf(advancedButton.selected)
            }
            row {
                text("About telescope").bold()
            }
            row {
                browserLink("This plugin is open source, check the code at Github", GITHUB_URL)
            }
            row {
                browserLink("Made by puntogris", PUNTOGRIS_URL)
            }
        }.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    }

    private fun Panel.defaultPanel(): Panel = panel {
        var commentCell: Cell<JEditorPane>? = null
        val initialComment = if (areDefaultModelsPathsValid()) {
            "Models stored at:\n${configPath.resolve("models").absolutePathString()}"
        } else {
            "None downloaded"
        }
        row {
            text("This model was trained and optimized specifically for this plugin's tasks.")
        }
        row {
            button("Download models") {
                downloadDefaultModels {
                    commentCell?.text(
                        "Models stored at:\n${configPath.resolve("models").absolutePathString()}"
                    )
                    project.messageBus.syncPublisher(SettingsEvents.TOPIC).embeddingsModelDownloaded()
                    syncService.sync { }
                }
            }
            commentCell = comment(initialComment)
        }
    }

    private fun Panel.advancedPanel() = panel {
        var textModelComment: Cell<JEditorPane>? = null
        var visionModelComment: Cell<JEditorPane>? = null
        val textModelInit = GlobalStorage.getCustomTextModelPath().ifEmpty { "None selected" }
        val visionModelInit = GlobalStorage.getCustomVisionModelPath().ifEmpty { "None selected" }

        row {
            text("To experiment and if you know what you are doing")
        }
        row {
            button("Select text model") {
                openFileChooser(project, GGUF).onSuccess {
                    GlobalStorage.setCustomTextModelPath(it)
                    textModelComment?.text(it)
                }
            }
            textModelComment = comment(textModelInit)
        }
        row {
            button("Select vision model") {
                openFileChooser(project, GGUF).onSuccess {
                    GlobalStorage.setCustomVisionModelPath(it)
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
            browserLink("Get more info here", GITHUB_URL)
        }
    }

    private fun areDefaultModelsPathsValid(): Boolean {
        return GlobalStorage.getDefaultTextModelPath().isNotEmpty() &&
                GlobalStorage.getDefaultVisionModelPath().isNotEmpty()
    }

    private fun downloadDefaultModels(onSuccess: () -> Unit) {
        runBackgroundableTask("Downloading AI models", project, true) { indicator ->
            val modelsDir = configPath.resolve("models")

            indicator.text = "Downloading text model"
            downloadFileWithProgress(TEXT_MODEL_URL, modelsDir, indicator)
                .onSuccess {
                    GlobalStorage.setDefaultTextModelPath(it)
                }
                .onFailure {
                    sendNotification(project, "Text model download failed", NotificationType.ERROR)
                }

            indicator.text = "Downloading vision model"
            downloadFileWithProgress(VISION_MODEL_URL, modelsDir, indicator)
                .onSuccess {
                    GlobalStorage.setDefaultVisionModelPath(it)
                }
                .onFailure {
                    sendNotification(project, "Vision model download failed", NotificationType.ERROR)
                }

            if (indicator.isCanceled) {
                sendNotification(project, "AI models download canceled", NotificationType.WARNING)
            } else {
                onSuccess()
                sendNotification(project, "AI models download completed", NotificationType.INFORMATION)
            }
        }
    }
}
