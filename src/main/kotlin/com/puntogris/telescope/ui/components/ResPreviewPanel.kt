package com.puntogris.telescope.ui.components

import javax.swing.JPanel

class ResPreviewPanel: JPanel() {

    fun bind(preview: ResourcePreview) {
        removeAll()
        add(preview.render())
    }
}
