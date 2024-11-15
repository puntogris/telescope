package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import com.puntogris.telescope.domain.VectorDrawableConverter
import java.awt.*
import java.io.StringReader
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
        private val textLabel: JLabel = JBLabel()
        private val svgLabel = SVGPanel()

        init {
            layout = FlowLayout(FlowLayout.LEFT, 5, 5)
            isOpaque = true // Make sure the panel is opaque
            add(svgLabel)
            add(textLabel)
        }

        class SVGPanel : JPanel() {
            private var svgDiagram: SVGDiagram? = null

            fun set(svgContent: String) {
                // Load the SVG into the SVGUniverse
                val svgUniverse = SVGUniverse()
                val svgUri = svgUniverse.loadSVG(StringReader(svgContent), "svg")
                svgDiagram = svgUniverse.getDiagram(svgUri)

                // Set panel size to the maximum bounding box (50x50)
                preferredSize = Dimension(50, 50)
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D

                svgDiagram?.let {
                    // Calculate scaling factor to maintain aspect ratio
                    val scale = 50.0 / maxOf(it.width, it.height)

                    // Center the image by translating
                    val translateX = (50 - it.width * scale) / 2
                    val translateY = (50 - it.height * scale) / 2
                    g2d.translate(translateX, translateY)

                    // Apply scaling
                    g2d.scale(scale, scale)
                    it.render(g2d)

                    // Reset scaling and translation to avoid affecting other drawings
                    g2d.scale(1 / scale, 1 / scale)
                    g2d.translate(-translateX, -translateY)
                }
            }
        }

        override fun getListCellRendererComponent(
            list: JList<out VirtualFile>,
            value: VirtualFile,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            textLabel.text = value.name


            try {
                val xml = value.readText()
                if (value.extension == "xml" && xml.startsWith("<vector")) {
                    val svg = replaceColors(VectorDrawableConverter().transform(xml))
                    svgLabel.set(svg)
                }
            } catch (t: Throwable) {
                // TODO map the most amount of drawable and for the rest show error icon
            }

            // Handle selection state
            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
            } else {
                background = list.background
                foreground = list.foreground
            }

            textLabel.foreground = foreground

            // Ensure the panel uses the correct background
            isOpaque = true

            // Set proper borders
            border = if (cellHasFocus) {
                BorderFactory.createLineBorder(list.selectionBackground)
            } else {
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            }

            return this
        }

        //TODO we should find at least the res colors we know in each module
        // save them in a map, maybe part of the sync process or list
        fun replaceColors(svgText: String): String {
            val fillRegex = """fill="[^"]*"""".toRegex()
            val strokeRegex = """stroke="[^"]*"""".toRegex()

            return svgText
                .replace(fillRegex, """fill="#000000"""")
                .replace(strokeRegex, """stroke="#000000"""")
        }
    }
}