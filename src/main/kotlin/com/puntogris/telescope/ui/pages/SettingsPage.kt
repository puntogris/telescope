package com.puntogris.telescope.ui.pages

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBLabel
import com.puntogris.telescope.ui.components.Hyperlink
import java.awt.*
import java.awt.datatransfer.DataFlavor
import javax.swing.*

class SettingsPage : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        add(title())
        add(subtitle())
        add(Box.createRigidArea(Dimension(0, 10)))
        add(inputComponent())
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

    private fun inputComponent(): JPanel {
        val inputPanel = JPanel(BorderLayout())
        val pathInput = JBTextField(30)

        val panel = JPanel(BorderLayout(5, 0)).apply {
            alignmentX = LEFT_ALIGNMENT

            val height = pathInput.preferredSize.height
            maximumSize = Dimension(Int.MAX_VALUE, height)
            preferredSize = Dimension(preferredSize.width, height)
        }
        panel.alignmentX = LEFT_ALIGNMENT

        val pathLabel = JBLabel("Model absolute path:")
        inputPanel.add(pathLabel, BorderLayout.LINE_START)

        val pasteButton = JButton(AllIcons.Actions.MenuPaste)
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
        panel.add(pasteButton, BorderLayout.EAST)

        return panel
    }
}