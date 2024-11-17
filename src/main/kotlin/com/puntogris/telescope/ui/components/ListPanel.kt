package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.puntogris.telescope.models.PreviewPanel
import java.awt.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ListPanel(
    private val files: List<VirtualFile>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane(), ListSelectionListener {

    private val listModel = DefaultListModel<VirtualFile>()
    private var lastSelectedPath: String? = null

    private val list = JBList<VirtualFile>().apply {
        cellRenderer = FileListCellRenderer()
        model = listModel
        addListSelectionListener(this@ListPanel)
    }

    init {
        listModel.addAll(files)
        setViewportView(list)
    }

    fun setFiles(files: List<VirtualFile>) {
        listModel.clear()
        listModel.addAll(files)
    }

    fun filter(query: String) {
        listModel.clear()
        listModel.addAll(
            files.filter { it.name.contains(query, ignoreCase = true) }
        )
    }

    override fun valueChanged(e: ListSelectionEvent?) {
        val selected = list.selectedValue ?: return
        val selectedPath = selected.path

        if (lastSelectedPath != selectedPath) {
            lastSelectedPath = selectedPath
            list.selectedValue?.let(onClick)
        }
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<VirtualFile> {
        private val textLabel = JBLabel()
        private val previewPanel = PreviewPanel()

        init {
            layout = FlowLayout(FlowLayout.LEFT, 5, 5)
            add(previewPanel)
            add(textLabel)
        }

        override fun getListCellRendererComponent(
            list: JList<out VirtualFile>,
            value: VirtualFile,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            textLabel.text = value.name
            previewPanel.bind(value)

            isOpaque = true

            border = if (isSelected) {
                BorderFactory.createLineBorder(list.selectionBackground)
            } else {
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            }

            return this
        }
    }

}
