package com.puntogris.telescope.ui.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.GlobalStorage
import com.puntogris.telescope.domain.SETTINGS_TOPIC
import com.puntogris.telescope.utils.documentText
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private const val GGUF_EXTENSION = "gguf"

class PathComponent(private val project: Project) : JPanel(), DocumentListener {

    private val pathInput = JBTextField().apply {
        text = GlobalStorage.getModelPath()
        document.addDocumentListener(this@PathComponent)
    }

    private val inputPanel = JPanel(BorderLayout()).apply {
        add(JBLabel("Model absolute path:"), BorderLayout.LINE_START)
    }

    private val pasteButton = JButton(AllIcons.Actions.MenuPaste).apply {
        addActionListener {
            try {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                val content = clipboard.getData(DataFlavor.stringFlavor) as String
                pathInput.text = content
            } catch (ignored: Exception) {
            }
        }
    }

    private val filesButton = JButton(AllIcons.Actions.AddFile).apply {
        addActionListener {
            chooseFile(onFileSelected = ::updatePath)
        }
    }

    private val rightPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(pasteButton)
        add(filesButton)
    }

    init {
        val height = pathInput.preferredSize.height
        maximumSize = Dimension(Int.MAX_VALUE, height)
        preferredSize = Dimension(preferredSize.width, height)
        alignmentX = LEFT_ALIGNMENT
        layout = BorderLayout(5, 0)

        add(inputPanel, BorderLayout.WEST)
        add(pathInput, BorderLayout.CENTER)
        add(rightPanel, BorderLayout.EAST)
    }

    private fun chooseFile(onFileSelected: (String) -> Unit) {
        val fileDescription = FileChooserDescriptor(true, false, false, false, false, false)
        fileDescription.withFileFilter {
            it.extension == GGUF_EXTENSION
        }
        val file = FileChooser.chooseFile(fileDescription, project, null)

        if (file != null) {
            onFileSelected(file.path)
        }
    }

    fun updatePath(path: String) {
        pathInput.text = path
    }

    private fun setEvent(e: DocumentEvent?) {
        GlobalStorage.setModelPath(e?.documentText.orEmpty())
        val isValid = Clip.isValidModel()
        project.messageBus.syncPublisher(SETTINGS_TOPIC).onModelPathUpdated(isValid)
    }

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
