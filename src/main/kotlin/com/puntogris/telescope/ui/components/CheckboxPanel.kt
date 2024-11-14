package com.puntogris.telescope.ui.components

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.maximumHeight
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel


private const val FUZZY_CHECKBOX_STATE_KEY = "FUZZY_CHECKBOX_STATE_KEY"
private const val EMBEDDING_CHECKBOX_STATE_KEY = "EMBEDDING_CHECKBOX_STATE_KEY"

class CheckboxPanel(
    private val onRefreshClicked: () -> Unit
) : JPanel() {

    init {
        layout = BorderLayout()

        val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))
        val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5))
        rightPanel.add(JButton(AllIcons.Actions.Refresh).apply {
            addActionListener {
                onRefreshClicked()
            }
        })

        maximumHeight = 30

        val label = JBLabel("Filters:")
        val fuzzyCheckbox = JBCheckBox("Fuzzy")
        fuzzyCheckbox.isSelected = PropertiesComponent.getInstance().getBoolean(FUZZY_CHECKBOX_STATE_KEY, false)

        val embeddingCheckbox = JBCheckBox("Embeddings")
        embeddingCheckbox.toolTipText = "To enable this enable on the settings page."
        embeddingCheckbox.isSelected = PropertiesComponent.getInstance().getBoolean(EMBEDDING_CHECKBOX_STATE_KEY, false)

        fuzzyCheckbox.addActionListener {
            if (fuzzyCheckbox.isSelected) {
            } else {
            }
            PropertiesComponent.getInstance().setValue(FUZZY_CHECKBOX_STATE_KEY, embeddingCheckbox.isSelected)
        }

        embeddingCheckbox.addActionListener {
            if (embeddingCheckbox.isSelected) {
            } else {
            }
            PropertiesComponent.getInstance().setValue(EMBEDDING_CHECKBOX_STATE_KEY, embeddingCheckbox.isSelected)
        }

        leftPanel.add(label)
        leftPanel.add(fuzzyCheckbox)
        leftPanel.add(embeddingCheckbox)

        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)
    }
}
