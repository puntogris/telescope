package com.puntogris.telescope.ui

import com.intellij.ui.util.maximumHeight
import com.intellij.ui.util.maximumWidth
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.svg.SVGUserAgentAdapter
import org.apache.batik.util.XMLResourceDescriptor
import com.puntogris.telescope.domain.VectorDrawableConverter
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException
import java.io.StringReader

class XmlDrawable(xml: String) : JSVGCanvas(SVGUserAgentAdapter(), false, false) {

    init {
        maximumHeight = 50
        maximumWidth = 50
        setXml(xml)
    }

    fun setXml(xml: String) {
        if (xml.isEmpty()) {
            return
        }
        val svgString = VectorDrawableConverter().transform(xml)
        val parser = XMLResourceDescriptor.getXMLParserClassName()
        val factory = SAXSVGDocumentFactory(parser)
        factory.setErrorHandler(NoOpErrorHandler())
        setSVGDocument(factory.createSVGDocument(null, StringReader(svgString)))
    }

    private class NoOpErrorHandler : ErrorHandler {
        override fun warning(exception: SAXParseException) = Unit
        override fun error(exception: SAXParseException) = Unit
        override fun fatalError(exception: SAXParseException) = Unit
    }
}
