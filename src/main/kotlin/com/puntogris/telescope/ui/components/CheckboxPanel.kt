package com.puntogris.telescope.ui.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.maximumHeight
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.GlobalStorage
import com.puntogris.telescope.domain.SETTINGS_TOPIC
import com.puntogris.telescope.domain.SettingsListener
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class CheckboxPanel(
    private val onRefreshClicked: () -> Unit,
    private val project: Project
) : JPanel() {

    private val embeddingCheckbox = JBCheckBox("Embeddings").apply {
        toolTipText = "To enable this enable on the settings page."
        isEnabled = Clip.isValidModel()
        isSelected = GlobalStorage.getEmbeddingsState()

        addActionListener {
            GlobalStorage.setEmbeddingsState(isSelected)
        }
    }

    private val fuzzyCheckbox = JBCheckBox("Fuzzy").apply {
        isSelected = GlobalStorage.getFuzzyState()

        addActionListener {
            GlobalStorage.setFuzzyState(isSelected)
        }
    }

    private val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
        add(JBLabel("Filters:"))
        add(fuzzyCheckbox)
        add(embeddingCheckbox)
    }

    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5)).apply {
        val button = JButton(AllIcons.Actions.Refresh)
        button.addActionListener { onRefreshClicked() }
        add(button)
    }

    init {
        layout = BorderLayout()
        maximumHeight = 30

        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)

        subscribe()
    }

    private fun subscribe() {
        val connection = project.messageBus.connect()
        connection.subscribe(SETTINGS_TOPIC, object : SettingsListener {
            override fun onModelPathUpdated(validPath: Boolean) {
                embeddingCheckbox.isEnabled = validPath
                embeddingCheckbox.isSelected = true
            }
        })
    }
}
