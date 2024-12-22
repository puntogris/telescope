package com.puntogris.telescope.ui.components

import com.android.tools.idea.ui.resourcemanager.widget.RowAssetView
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.SearchResult
import javax.swing.DefaultListModel
import javax.swing.DefaultListSelectionModel
import javax.swing.JList
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ListPanel(
    private var files: List<DrawableRes>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane(), ListSelectionListener {

    private val listModel = DefaultListModel<DrawableRes>()
    private var lastSelectedPath: String? = null

    private val list = AssetList().apply {
        layoutOrientation = JList.VERTICAL
        cellRenderer = DrawableCellRenderer()
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
        border = JBUI.Borders.empty(0, 6)
        listModel.addAll(files)
        setViewportView(list)
    }

    fun update(newFiles: List<DrawableRes>) {
        SwingUtilities.invokeLater {
            files = newFiles
            listModel.clear()
            listModel.addAll(files)
        }
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
}

class AssetList : JBList<DrawableRes>() {
    val assetView = RowAssetView()

    init {
        layoutOrientation = JList.VERTICAL
    }
}