package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.SearchResult
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.DefaultListSelectionModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ListPanel(
    private val files: List<DrawableRes>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane(), ListSelectionListener {

    private val listModel = DefaultListModel<DrawableRes>()
    private var lastSelectedPath: String? = null

    private val list = JBList<DrawableRes>().apply {
        cellRenderer = FileListCellRenderer()
        model = listModel

        selectionModel = object : DefaultListSelectionModel() {
            override fun setSelectionInterval(index0: Int, index1: Int) {
                if (isSelectedIndex(index0)) {
                    // Clear selection briefly to allow repeated selection
                    clearSelection()
                }
                super.setSelectionInterval(index0, index1)
            }
        }
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
        if (e == null || e.valueIsAdjusting) {
            // Ignore interim events
            return
        }
        val selected = list.selectedValue ?: return
        var nextPath = ""

        when (selected) {
            is DrawableRes.Simple -> {
                if (lastSelectedPath != nextPath) {
                    nextPath = selected.path
                    onClick(selected.file)
                }
            }

            is DrawableRes.WithVariants -> {
                val paths = selected.variants.map { it.mainPath }
                if (lastSelectedPath in paths) {
                    val currentIndex = paths.indexOf(lastSelectedPath)
                    val nextIndex = if (currentIndex + 1 < paths.size) currentIndex + 1 else 0
                    val next = selected.variants[nextIndex]
                    nextPath = next.mainPath
                    onClick(next.file)
                } else {
                    val first = selected.variants.first()
                    nextPath = first.mainPath
                    onClick(first.file)
                }
            }
        }
        lastSelectedPath = nextPath
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<DrawableRes> {

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
            list: JList<out DrawableRes>,
            value: DrawableRes,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            nameLabel.text = value.name
            previewPanel.bind(value.file)

            when (value) {
                is DrawableRes.Simple -> {
                    variantsLabel.isVisible = false
                }

                is DrawableRes.WithVariants -> {
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
