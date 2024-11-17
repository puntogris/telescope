package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JPanel

class PreviewPanel: JPanel() {

    fun bind(file: VirtualFile) {
        removeAll()
        add(Preview.from(file).render())
    }
}
