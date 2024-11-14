package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*

class ListPanel(
    private val files: List<VirtualFile>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane() {

    private val listModel = DefaultListModel<VirtualFile>()

    private var lastSelectedPath = ""

    private val list = JBList<VirtualFile>()
    init {
        list.cellRenderer = FileListCellRenderer()
        list.model = listModel

        files.forEach { file ->
            listModel.addElement(file)
        }

        //TODO we should improve this and only trigger for the selected
        list.addListSelectionListener {
            if (lastSelectedPath != list.selectedValue.path) {
                onClick(list.selectedValue)
                lastSelectedPath = list.selectedValue.path
            }
        }
        setViewportView(list)
    }

    fun filter(query: String) {
        listModel.clear()
        files
            .filter { it.name.contains(query) }
            .forEach { listModel.addElement(it) }
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<VirtualFile> {
        private val iconLabel: XmlDrawable = XmlDrawable("")
        private val textLabel: JLabel = JBLabel()

        init {
            layout = FlowLayout(FlowLayout.LEFT, 5, 5)
            add(iconLabel)
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

            background = if (isSelected) list.selectionBackground else list.background
            foreground = if (isSelected) list.selectionForeground else list.foreground
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

            return this
        }
    }
}