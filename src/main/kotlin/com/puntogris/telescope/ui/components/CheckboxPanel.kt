package com.puntogris.telescope.ui.components

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.maximumHeight
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.GlobalStorage
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class CheckboxPanel(
    private val onRefreshClicked: () -> Unit
) : JPanel() {

    private val partialMatchCheckbox = JBCheckBox("Partial Match").apply {
        isSelected = GlobalStorage.getPartialMatchState()
        addActionListener {
            GlobalStorage.sePartialMatchState(isSelected)
        }
    }

    private val embeddingCheckbox = JBCheckBox("Embeddings").apply {
        toolTipText = "To enable this enable on the settings page."
        isEnabled = Clip.canEnableClip()
        isSelected = GlobalStorage.getEmbeddingsState()
        addActionListener {
            GlobalStorage.setEmbeddingsState(isSelected)
        }
    }

    private val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
        add(JBLabel("Filters:"))
        add(partialMatchCheckbox)
        add(embeddingCheckbox)
    }

    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
        val button = JButton(AllIcons.Actions.Refresh)
        button.addActionListener { onRefreshClicked() }
        add(button)
    }

    init {
        border = JBUI.Borders.empty(0, 6)
        layout = BorderLayout()
        maximumHeight = 30

        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)
    }
}
