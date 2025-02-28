package com.puntogris.telescope.application

import com.puntogris.telescope.models.Colors
import com.puntogris.telescope.models.Dependencies
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class VectorToSvg {

    private val attributesMap = mapOf(
        "android:pathData" to "d",
        "android:fillColor" to "fill",
        "android:strokeLineJoin" to "stroke-linejoin",
        "android:strokeLineCap" to "stroke-linecap",
        "android:strokeMiterLimit" to "stroke-miterlimit",
        "android:strokeWidth" to "stroke-width",
        "android:strokeColor" to "stroke",
        "android:fillType" to "fill-rule",
        "android:fillAlpha" to "fill-opacity",
        "android:strokeAlpha" to "stroke-opacity"
    )

    private val groupAttrsMap = mapOf(
        "android:name" to "id",
        "android:pivotX" to TransformAttribute("pivotX"),
        "android:pivotY" to TransformAttribute("pivotY"),
        "android:rotation" to TransformAttribute("rotation"),
        "android:scaleX" to TransformAttribute("scaleX"),
        "android:scaleY" to TransformAttribute("scaleY"),
        "android:translateX" to TransformAttribute("translateX"),
        "android:translateY" to TransformAttribute("translateY")
    )

    private val gradientAttrsMap = mapOf(
        "android:startX" to "x1",
        "android:startY" to "y1",
        "android:endX" to "x2",
        "android:endY" to "y2",
        "android:centerX" to "cx",
        "android:centerY" to "cy",
        "android:gradientRadius" to "r"
    )

    private val gradientItemAttrsMap = mapOf(
        "android:color" to "stop-color",
        "android:offset" to "offset"
    )

    private var module: String = ""

    operator fun invoke(content: String, module: String, colors: Colors, dependencies: Dependencies): String {
        this.module = module
        Globals.setModuleColors(colors)
        Globals.setModuleDependencies(dependencies)
        return transform(content)
    }

    private fun parsePath(root: Document, pathNode: Element): Element {
        val svgPath = root.createElement("path")
        svgPath.setAttribute("fill", "none")

        pathNode.attributes.let { attrs ->
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                val svgAttrName = attributesMap[attr.nodeName]
                if (svgAttrName != null) {
                    val svgAttrValue = when (attr.nodeName) {
                        "android:fillType" -> attr.nodeValue.lowercase()
                        "android:fillColor", "android:strokeColor" -> convertColorResToHex(attr.nodeValue)
                        else -> attr.nodeValue
                    }
                    svgPath.setAttribute(svgAttrName, svgAttrValue)
                }
            }
        }

        return svgPath
    }

    private fun parseGradient(root: Document, gradientNode: Element): Element {
        val type = gradientNode.getAttribute("android:type")

        val svgGradient = when (type) {
            "linear" -> root.createElement("linearGradient")
            "radial" -> root.createElement("radialGradient")
            "sweep" -> throw IllegalArgumentException("Sweep gradient is not compatible with SVG")
            else -> throw IllegalArgumentException("Invalid gradient type")
        }

        svgGradient.setAttribute("gradientUnits", "userSpaceOnUse")

        gradientNode.attributes.let { attrs ->
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                gradientAttrsMap[attr.nodeName]?.let { svgAttrName ->
                    svgGradient.setAttribute(svgAttrName, attr.nodeValue)
                }
            }
        }

        for (i in 0 until gradientNode.childNodes.length) {
            val node = gradientNode.childNodes.item(i)
            if (node is Element && node.tagName == "item") {
                val svgGradientStop = root.createElement("stop")

                node.attributes.let { attrs ->
                    for (j in 0 until attrs.length) {
                        val attr = attrs.item(j)
                        gradientItemAttrsMap[attr.nodeName]?.let { svgAttrName ->
                            val svgAttrValue = if (attr.nodeName == "android:color") {
                                convertColorResToHex(attr.nodeValue)
                            } else {
                                attr.nodeValue
                            }
                            svgGradientStop.setAttribute(svgAttrName, svgAttrValue)
                        }
                    }
                }

                svgGradient.appendChild(svgGradientStop)
            }
        }

        return svgGradient
    }

    private fun transformNode(node: Node, parent: Node, root: Document, defs: Element? = null): Element? {
        if (node !is Element) return null

        when (node.tagName) {
            "path" -> {
                val svgPath = parsePath(root, node)

                for (i in 0 until node.childNodes.length) {
                    val childNode = node.childNodes.item(i)
                    if (childNode is Element && childNode.tagName == "aapt:attr") {
                        when (childNode.getAttribute("name")) {
                            "android:fillColor", "android:strokeColor" -> {
                                for (j in 0 until childNode.childNodes.length) {
                                    val gradientNode = childNode.childNodes.item(j)
                                    if (gradientNode is Element && gradientNode.tagName == "gradient") {
                                        val svgGradient = parseGradient(root, gradientNode)
                                        defs?.let { defsNode ->
                                            val size = defsNode.childNodes.length
                                            val gradientId = "gradient_$size"
                                            svgGradient.setAttribute("id", gradientId)
                                            defsNode.appendChild(svgGradient)

                                            val svgAttrName = attributesMap[childNode.getAttribute("name")]
                                            svgPath.setAttribute(svgAttrName!!, "url(#$gradientId)")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return svgPath
            }

            "group" -> {
                val groupNode = root.createElement("g")
                val transforms = mutableMapOf<String, String>()

                node.attributes.let { attrs ->
                    for (i in 0 until attrs.length) {
                        val attr = attrs.item(i)
                        when (val svgAttr = groupAttrsMap[attr.nodeName]) {
                            is TransformAttribute -> {
                                transforms[svgAttr.transform] = attr.nodeValue
                            }

                            is String -> {
                                groupNode.setAttribute(svgAttr, attr.nodeValue)
                            }
                        }
                    }
                }

                if (transforms.isNotEmpty()) {
                    val transformList = mutableListOf<String>()

                    val translateX = transforms["translateX"]
                    val translateY = transforms["translateY"]
                    if (translateX != "0" || translateY != "0") {
                        transformList.add("translate($translateX, $translateY)")
                    }

                    transforms["rotation"]?.let { rotation ->
                        if (rotation != "0") {
                            transformList.add("rotate($rotation)")
                        }
                    }

                    val scaleX = transforms["scaleX"] ?: "1"
                    val scaleY = transforms["scaleY"] ?: "1"
                    if (scaleX != "1" || scaleY != "1") {
                        transformList.add("scale($scaleX, $scaleY)")
                    }

                    if (transformList.isNotEmpty()) {
                        groupNode.setAttribute("transform", transformList.joinToString(" "))
                    }
                }

                var prevClipPathId: String? = null

                for (i in 0 until node.childNodes.length) {
                    val childNode = node.childNodes.item(i)
                    transformNode(childNode, node, root, defs)?.let { childPath ->
                        val clipPathNode =
                            (childPath as? Element)?.getAttribute("clip-path-node")?.toBooleanStrictOrNull()
                        if (clipPathNode == true) {
                            defs?.let { defsNode ->
                                val size = defsNode.childNodes.length
                                prevClipPathId = "clip_path_$size"
                                childPath.setAttribute("id", prevClipPathId)
                                defsNode.appendChild(childPath)
                            }
                        } else {
                            prevClipPathId?.let { id ->
                                childPath.setAttribute("clip-path", "url(#$id)")
                                prevClipPathId = null
                            }
                            groupNode.appendChild(childPath)
                        }
                    }
                }

                return groupNode
            }

            "clip-path" -> {
                val pathData = node.getAttribute("android:pathData")
                val svgClipPathNode = root.createElement("clipPath")
                val path = root.createElement("path")

                path.setAttribute("d", pathData)
                svgClipPathNode.appendChild(path)
                svgClipPathNode.setAttribute("clip-path-node", "true")

                return svgClipPathNode
            }
        }

        return null
    }

    private fun removeDimenSuffix(dimen: String?): String? {
        if (dimen == null) return null

        val trimmed = dimen.trim()
        if (trimmed.isEmpty()) return trimmed

        return try {
            trimmed.toDouble().toString()
        } catch (e: NumberFormatException) {
            if (trimmed.length > 2) {
                trimmed.substring(0, trimmed.length - 2)
            } else {
                trimmed
            }
        }
    }

    private fun convertColorResToHex(color: String): String {
        if (color.startsWith("@")) {
            return getColorFromRes(color)
        }
        val digits = color.removePrefix("#")

        return when {
            digits.length != 4 && digits.length != 8 -> color
            digits.length == 4 -> {
                val (alpha, red, green, blue) = digits.toCharArray()
                "#$red$green$blue$alpha"
            }

            else -> {
                // TODO svg doesnt support RGBA in hex apparently, we should use fill-opacity,..?
                val alpha = digits.substring(0, 2)
                val red = digits.substring(2, 4)
                val green = digits.substring(4, 6)
                val blue = digits.substring(6, 8)
                "#$red$green$blue"
            }
        }
    }

    //TODO check if the alpha colors are mapped correctly
    private fun getColorFromRes(color: String): String {
        val hex = if (color.startsWith("@android:color/")) {
            Globals.searchInAndroidColors(color.substringAfter("/"))
        } else {
            Globals.searchInModuleColors(color.substringAfter("/"), module)
        }
        val c =  hex ?: "#000000"

        return convertColorResToHex(c)
    }

    private fun transform(content: String, options: Map<String, Any> = emptyMap()): String {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(content.byteInputStream())

        val override = options["override"] as? Map<String, String>
        if (override != null) {
            fun traverseNodes(node: Node, callback: (Node) -> Unit) {
                callback(node)
                for (i in 0 until node.childNodes.length) {
                    traverseNodes(node.childNodes.item(i), callback)
                }
            }

            traverseNodes(doc) { node ->
                if (node is Element) {
                    for (i in 0 until node.attributes.length) {
                        val attr = node.attributes.item(i)
                        override[attr.nodeValue]?.let { overrideValue ->
                            node.setAttribute(attr.nodeName, overrideValue)
                        }
                    }
                }
            }
        }

        val vectorDrawables = doc.getElementsByTagName("vector")
        require(vectorDrawables.length == 1) { "VectorDrawable is invalid" }

        val vectorDrawable = vectorDrawables.item(0) as Element
        val viewportWidth = vectorDrawable.getAttribute("android:viewportWidth")
        val viewportHeight = vectorDrawable.getAttribute("android:viewportHeight")
        val outputWidth = removeDimenSuffix(vectorDrawable.getAttribute("android:width"))
        val outputHeight = removeDimenSuffix(vectorDrawable.getAttribute("android:height"))

        val svgNode = doc.createElement("svg").apply {
            setAttribute("id", "vector")
            setAttribute("xmlns", "http://www.w3.org/2000/svg")
            setAttribute("width", outputWidth ?: viewportWidth)
            setAttribute("height", outputHeight ?: viewportHeight)
            setAttribute("viewBox", "0 0 $viewportWidth $viewportHeight")
        }

        val childrenNodes = mutableListOf<Element>()
        for (i in 0 until doc.documentElement.childNodes.length) {
            val node = doc.documentElement.childNodes.item(i)
            if (node is Element) {
                childrenNodes.add(node)
            }
        }

        val defsNode = doc.createElement("defs")
        val nodes = childrenNodes.mapNotNull { node ->
            transformNode(node, doc.documentElement, doc, defsNode)
        }

        if (defsNode.childNodes.length > 0) {
            svgNode.appendChild(defsNode)
        }

        val nodeIndices = mutableMapOf(
            "g" to 0,
            "path" to 0
        )

        nodes.forEach { node ->
            val id = node.getAttribute("id")
            nodeIndices[node.tagName]?.let { currentId ->
                nodeIndices[node.tagName] = currentId + 1
                node.setAttribute("id", id.ifEmpty { "${node.tagName}_$currentId" })
                svgNode.appendChild(node)
            }
        }


        val transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(svgNode)
        val writer = StringWriter()
        val result = StreamResult(writer)

        transformer.transform(source, result)
        val svgString = writer.toString()

        // Handle pretty printing if requested
        if (options["pretty"] == true) {
            // You can use a Kotlin com.puntogris.telescope.utils.XML formatting library here
            // For example, using kotlin-xml-formatter or similar
            // For now, we'll return the unformatted string
            // TODO: Implement com.puntogris.telescope.utils.XML formatting
        }

        return svgString
    }

    data class TransformAttribute(val transform: String)
}
