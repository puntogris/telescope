package com.puntogris.telescope.ui.components

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.models.DrawableRes
import java.awt.BorderLayout
import javax.swing.JPanel

class ResDescriptionPanel : JPanel() {

    private val nameLabel = JBLabel().apply {
        alignmentX = LEFT_ALIGNMENT
        setBorder(JBUI.Borders.emptyLeft(5))
    }
    private val variantsLabel = JBLabel().apply {
        font = font.deriveFont(12f)
        alignmentX = LEFT_ALIGNMENT
    }

    init {
        layout = BorderLayout(0, 10)
        alignmentX = LEFT_ALIGNMENT

        add(nameLabel, BorderLayout.PAGE_START)
        add(variantsLabel, BorderLayout.PAGE_END)
    }

    fun bind(res: DrawableRes) {
        nameLabel.text = res.name

        when (res) {
            is DrawableRes.Simple -> {
                variantsLabel.isVisible = false
            }

            is DrawableRes.WithVariants -> {
                val variants = res.variants.joinToString(" - ") { it.parentDirName }
                variantsLabel.text = "<html><a href=''>$variants</a></html>"
                variantsLabel.isVisible = true
            }
        }
    }
}