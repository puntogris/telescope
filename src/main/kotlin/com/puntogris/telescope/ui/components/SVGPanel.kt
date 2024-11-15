package com.puntogris.telescope.ui.components

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.StringReader
import javax.swing.JPanel

class SVGPanel(private val size: Double = 50.0) : JPanel() {
    private var svgDiagram: SVGDiagram? = null

    fun set(svgContent: String) {
        val svgUniverse = SVGUniverse()
        val svgUri = svgUniverse.loadSVG(StringReader(svgContent), "svg")
        svgDiagram = svgUniverse.getDiagram(svgUri)
        preferredSize = Dimension(50, 50)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        svgDiagram?.let {
            val scale = size / maxOf(it.width, it.height)
            val translateX = (size - it.width * scale) / 2
            val translateY = (size - it.height * scale) / 2

            g2d.translate(translateX, translateY)
            g2d.scale(scale, scale)
            it.render(g2d)

            g2d.scale(1 / scale, 1 / scale)
            g2d.translate(-translateX, -translateY)
        }
    }
}