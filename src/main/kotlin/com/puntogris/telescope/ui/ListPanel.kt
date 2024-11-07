package com.puntogris.telescope.ui

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.ui.components.JBList
import com.intellij.ui.util.maximumHeight
import com.intellij.ui.util.maximumWidth
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*

class ListPanel(
    private val files: List<VirtualFile>,
    private val onClick: (VirtualFile) -> Unit
) : JBList<VirtualFile>() {

    private val listModel = DefaultListModel<VirtualFile>()

    private var lastSelectedPath = ""

    init {
        cellRenderer = FileListCellRenderer()
        model = listModel
        files.forEach { file ->
            listModel.addElement(file)
        }

        //TODO we should improve this and only trigger for the selected
        addListSelectionListener {
            if (lastSelectedPath != selectedValue.path) {
                onClick(selectedValue)
                lastSelectedPath = selectedValue.path
            }
        }
    }

    fun filter(query: String) {
        listModel.clear()
        files
            .filter { it.name.contains(query) }
            .forEach { listModel.addElement(it) }
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<VirtualFile> {
        private val iconLabel: XmlDrawable = XmlDrawable("")
        private val textLabel: JLabel = JLabel()

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


          //  iconLabel.setXml(value.readText())
            textLabel.text = value.name

            background = if (isSelected) list.selectionBackground else list.background
            foreground = if (isSelected) list.selectionForeground else list.foreground
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

            return this
        }
    }
}