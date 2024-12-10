package com.puntogris.telescope.ui.components

import javax.swing.JPanel

class PreviewPanel: JPanel() {

    fun bind(preview: Preview) {
        removeAll()
        add(preview.render())
    }
}
