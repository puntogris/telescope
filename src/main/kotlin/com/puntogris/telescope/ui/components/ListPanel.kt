package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.models.DrawableDir
import com.puntogris.telescope.models.SearchResult
import java.awt.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ListPanel(
    private val files: List<DrawableDir>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane(), ListSelectionListener {

    private val listModel = DefaultListModel<DrawableDir>()
    private var lastSelectedPath: String? = null

    private val list = JBList<DrawableDir>().apply {
        cellRenderer = FileListCellRenderer()
        model = listModel
        addListSelectionListener(this@ListPanel)
    }

    init {
        listModel.addAll(files)
        setViewportView(list)
    }

    fun filter(result: List<SearchResult>) {
        val newData = result.mapNotNull { r -> files.find { f -> f.path == r.uri } }
        SwingUtilities.invokeLater {
            listModel.clear()
            listModel.addAll(newData)
        }
    }

    fun reset() {
        SwingUtilities.invokeLater {
            listModel.clear()
            listModel.addAll(files)
        }
    }


    override fun valueChanged(e: ListSelectionEvent?) {
        val selected = list.selectedValue ?: return
        val selectedPath = selected.path

        if (lastSelectedPath != selectedPath) {
            lastSelectedPath = selectedPath
            list.selectedValue?.let {
                onClick(it.file)
            }
        }
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<DrawableDir> {

        private val nameLabel = JBLabel().apply {
            alignmentX = LEFT_ALIGNMENT
            setBorder(JBUI.Borders.emptyLeft(5))
        }
        private val variantsLabel = JBLabel().apply {
            font = font.deriveFont(12f)
            alignmentX = LEFT_ALIGNMENT
        }
        private val previewPanel = PreviewPanel()
        private val rightPanel = JPanel().apply {
            layout = BorderLayout(0, 10)
            alignmentX = LEFT_ALIGNMENT

        }

        init {
            layout = FlowLayout(FlowLayout.LEFT, 5, 5)
            rightPanel.add(nameLabel, BorderLayout.PAGE_START)
            rightPanel.add(variantsLabel, BorderLayout.PAGE_END)
            add(previewPanel)
            add(rightPanel)
        }

        override fun getListCellRendererComponent(
            list: JList<out DrawableDir>,
            value: DrawableDir,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            nameLabel.text = value.name
            previewPanel.bind(value.file)

            when (value) {
                is DrawableDir.Simple -> {
                    variantsLabel.isVisible = false
                }

                is DrawableDir.WithVariants -> {
                    val variants = value.variants.joinToString(" - ") { it.parentDirName }
                    variantsLabel.text = "<html><a href=''>$variants</a></html>"
                    variantsLabel.isVisible = true
                }
            }

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
