package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.puntogris.telescope.domain.VectorDrawableConverter
import java.awt.*
import javax.swing.*

private const val XML = "xml"
private const val WEBP = "webp"
private const val PNG = "png"
private const val JPEG = "jpeg"

class ListPanel(
    private val files: List<VirtualFile>,
    private val onClick: (VirtualFile) -> Unit
) : JBScrollPane() {

    private val listModel = DefaultListModel<VirtualFile>()
    private var lastSelectedPath = ""

    private val list = JBList<VirtualFile>().apply {
        cellRenderer = FileListCellRenderer()
        model = listModel
        addListSelectionListener {
            val selectedValue = selectedValue
            if (lastSelectedPath != selectedValue?.path) {
                selectedValue?.let { onClick(it) }
                lastSelectedPath = selectedValue?.path.orEmpty()
            }
        }
    }

    init {
        listModel.addAll(files)
        setViewportView(list)
    }

    fun filter(query: String) {
        listModel.clear()
        files.filter { it.name.contains(query, ignoreCase = true) }
            .forEach { listModel.addElement(it) }
    }

    private class FileListCellRenderer : JPanel(), ListCellRenderer<VirtualFile> {

        private val textLabel = JBLabel()
        private val svgLabel = SVGPanel()

        init {
            layout = FlowLayout(FlowLayout.LEFT, 5, 5)
            add(svgLabel)
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

            //TODO we should map items outside, this should only show them

            try {
                val xml = value.readText()
                when (value.extension) {
                    XML -> {
                        if (xml.startsWith("<vector")) {
                            val svg = replaceColors(VectorDrawableConverter().transform(xml))
                            svgLabel.set(svg)
                        }
                    }

                    WEBP -> {}
                    PNG, JPEG -> {}
                }
            } catch (e: Exception) {
                // Handle errors (e.g., invalid XML or unsupported format)
            }

            background = if (isSelected) list.selectionBackground else list.background
            foreground = if (isSelected) list.selectionForeground else list.foreground
            textLabel.foreground = foreground

            isOpaque = true

            border = if (cellHasFocus) {
                BorderFactory.createLineBorder(list.selectionBackground)
            } else {
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            }

            return this
        }

        private fun replaceColors(svgText: String): String {
            val fillRegex = """fill="[^"]*"""".toRegex()
            val strokeRegex = """stroke="[^"]*"""".toRegex()

            return svgText
                .replace(fillRegex, """fill="#000000"""")
                .replace(strokeRegex, """stroke="#000000"""")
        }
    }
}
