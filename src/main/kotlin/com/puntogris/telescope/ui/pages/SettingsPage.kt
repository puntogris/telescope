package com.puntogris.telescope.ui.pages

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBLabel
import com.intellij.util.messages.Topic
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.ui.components.Hyperlink
import java.awt.*
import java.awt.datatransfer.DataFlavor
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


const val AI_MODEL_PATH_KEY = "AI_MODEL_PATH_KEY"
interface FlagChangedListener {
    fun onFlagChanged(newFlag: Boolean)
}

val FLAG_CHANGED_TOPIC = Topic.create("FlagChanged", FlagChangedListener::class.java)

class SettingsPage(private val project: Project) : JPanel() {


    private val documentListener = object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            setEvent(e)
        }

        override fun removeUpdate(e: DocumentEvent?) {
            setEvent(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            setEvent(e)
        }
    }

    private fun setEvent(e: DocumentEvent?) {
        if (e == null) {
            PropertiesComponent.getInstance().setValue(AI_MODEL_PATH_KEY, "")
        } else {
            PropertiesComponent.getInstance().setValue(AI_MODEL_PATH_KEY, e.document.getText(0, e.document.length))
        }
        val isValid = Clip.testLoad()
        project.messageBus.syncPublisher(FLAG_CHANGED_TOPIC).onFlagChanged(isValid)
        thisLogger().warn("logeto_setting_$isValid")
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        add(title())
        add(subtitle())
        add(Box.createRigidArea(Dimension(0, 10)))
        add(inputComponent(project))
        add(Box.createRigidArea(Dimension(0, 10)))

        val downloadLabel = JLabel("If you don't want to use a custom one i recommend you this one at only 85MB")
        val downloadButton = JButton("Download default and apply")
        add(downloadLabel)
        add(downloadButton)

        add(Box.createRigidArea(Dimension(0, 10)))

        val info = JLabel("This uses CLIP compatible models, you can check more about them here:")
        add(info)
        add(Hyperlink("https://stackoverflow.com/questions/12589494/align-text-in-jlabel-to-the-right"))
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

    private fun inputComponent(project: Project): JPanel {
        val inputPanel = JPanel(BorderLayout())
        val pathInput = JBTextField(30)
        pathInput.text = PropertiesComponent.getInstance().getValue(AI_MODEL_PATH_KEY, "")
        pathInput.document.addDocumentListener(documentListener)


        val panel = JPanel(BorderLayout(5, 0)).apply {
            alignmentX = LEFT_ALIGNMENT

            val height = pathInput.preferredSize.height
            maximumSize = Dimension(Int.MAX_VALUE, height)
            preferredSize = Dimension(preferredSize.width, height)
        }
        panel.alignmentX = LEFT_ALIGNMENT

        val pathLabel = JBLabel("Model absolute path:")
        inputPanel.add(pathLabel, BorderLayout.LINE_START)

        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.X_AXIS)
        val pasteButton = JButton(AllIcons.Actions.MenuPaste)
        val filesButton = JButton(AllIcons.Actions.AddFile)

        filesButton.addActionListener {
            chooseFile(project,
                onFileSelected = {
                    pathInput.text = it
                }
            )
        }
        rightPanel.add(pasteButton)
        rightPanel.add(filesButton)
        pasteButton.addActionListener {
            try {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                val content = clipboard.getData(DataFlavor.stringFlavor) as String
                pathInput.text = content
            } catch (ignored: Exception) {
            }
        }

        panel.add(inputPanel, BorderLayout.WEST)
        panel.add(pathInput, BorderLayout.CENTER)
        panel.add(rightPanel, BorderLayout.EAST)

        return panel
    }

    private fun chooseFile(project: Project, onFileSelected: (String) -> Unit) {
        val fileDescription = FileChooserDescriptor(true, false, false, false, false, false)
        fileDescription.withFileFilter {
            it.extension == "gguf"
        }
        val file = FileChooser.chooseFile(fileDescription, project, null)

        if (file != null) {
            onFileSelected(file.path)
        }
    }
}