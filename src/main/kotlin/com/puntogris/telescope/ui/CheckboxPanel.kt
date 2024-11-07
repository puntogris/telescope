package com.puntogris.telescope.ui

import com.intellij.ui.util.maximumHeight
import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

class CheckboxPanel : JPanel() {

    init {
        layout = FlowLayout(FlowLayout.LEFT, 5, 5)
        maximumHeight = 30

        val label = JLabel("Filters:")
        val fuzzyCheckbox = JCheckBox("Fuzzy")
        val embeddingCheckbox = JCheckBox("Embeddings")

        fuzzyCheckbox.isSelected = true
        embeddingCheckbox.isSelected = true

        fuzzyCheckbox.addActionListener {
            if (fuzzyCheckbox.isSelected) {

            } else {

            }
        }

        embeddingCheckbox.addActionListener {
            if (embeddingCheckbox.isSelected) {

            } else {

            }
        }

        add(label)
        add(fuzzyCheckbox)
        add(embeddingCheckbox)
    }
}
