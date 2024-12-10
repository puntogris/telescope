package com.puntogris.telescope.domain.usecase

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

    private var resColors = mapOf<String, Map<String, String>>()

    operator fun invoke(content: String, colors: Map<String, Map<String, String>>): String {
        resColors = colors
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
                        "android:fillColor", "android:strokeColor" -> convertHexColor(attr.nodeValue)
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
                                convertHexColor(attr.nodeValue)
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

    private fun convertHexColor(argb: String): String {
        if (argb.startsWith("@")) {
            return transformColorFromRes(argb)
        }
        val digits = argb.removePrefix("#")

        return when {
            digits.length != 4 && digits.length != 8 -> argb
            digits.length == 4 -> {
                val (alpha, red, green, blue) = digits.toCharArray()
                "#$red$green$blue$alpha"
            }

            else -> {
                val alpha = digits.substring(0, 2)
                val red = digits.substring(2, 4)
                val green = digits.substring(4, 6)
                val blue = digits.substring(6, 8)
                "#$red$green$blue$alpha"
            }
        }
    }

    private fun transformColorFromRes(color: String): String {
        if (color.startsWith("@android:color/")) {
            val new = androidColors[color.substringAfter("/")]
            if (new != null) {
                return new
            } else {
                // default
                return "#000000"
            }
        } else {
            //find the color
            return "#000000"
        }
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

val androidColors = hashMapOf(
    "white" to "#FFFFFF",
    "ivory" to "#FFFFF0",
    "light_yellow" to "#FFFFE0",
    "yellow" to "#FFFF00",
    "snow" to "#FFFAFA",
    "floral_white" to "#FFFAF0",
    "lemon_chiffon" to "#FFFACD",
    "cornsilk" to "#FFF8DC",
    "seashell" to "#FFF5EE",
    "lavender_blush" to "#FFF0F5",
    "papaya_whip" to "#FFEFD5",
    "blanched_almond" to "#FFEBCD",
    "misty_rose" to "#FFE4E1",
    "bisque" to "#FFE4C4",
    "moccasin" to "#FFE4B5",
    "navajo_white" to "#FFDEAD",
    "peach_puff" to "#FFDAB9",
    "gold" to "#FFD700",
    "pink" to "#FFC0CB",
    "light_pink" to "#FFB6C1",
    "orange" to "#FFA500",
    "light_salmon" to "#FFA07A",
    "dark_orange" to "#FF8C00",
    "coral" to "#FF7F50",
    "hot_pink" to "#FF69B4",
    "tomato" to "#FF6347",
    "orange_red" to "#FF4500",
    "deep_pink" to "#FF1493",
    "fuchsia" to "#FF00FF",
    "magenta" to "#FF00FF",
    "red" to "#FF0000",
    "old_lace" to "#FDF5E6",
    "light_goldenrod_yellow" to "#FAFAD2",
    "linen" to "#FAF0E6",
    "antique_white" to "#FAEBD7",
    "salmon" to "#FA8072",
    "ghost_white" to "#F8F8FF",
    "mint_cream" to "#F5FFFA",
    "white_smoke" to "#F5F5F5",
    "beige" to "#F5F5DC",
    "wheat" to "#F5DEB3",
    "sandy_brown" to "#F4A460",
    "azure" to "#F0FFFF",
    "honeydew" to "#F0FFF0",
    "alice_blue" to "#F0F8FF",
    "khaki" to "#F0E68C",
    "light_coral" to "#F08080",
    "pale_goldenrod" to "#EEE8AA",
    "violet" to "#EE82EE",
    "dark_salmon" to "#E9967A",
    "lavender" to "#E6E6FA",
    "light_cyan" to "#E0FFFF",
    "burlywood" to "#DEB887",
    "plum" to "#DDA0DD",
    "gainsboro" to "#DCDCDC",
    "crimson" to "#DC143C",
    "pale_violet_red" to "#DB7093",
    "goldenrod" to "#DAA520",
    "orchid" to "#DA70D6",
    "thistle" to "#D8BFD8",
    "light_grey" to "#D3D3D3",
    "tan" to "#D2B48C",
    "chocolate" to "#D2691E",
    "peru" to "#CD853F",
    "indian_red" to "#CD5C5C",
    "medium_violet_red" to "#C71585",
    "silver" to "#C0C0C0",
    "dark_khaki" to "#BDB76B",
    "rosy_brown" to "#BC8F8F",
    "medium_orchid" to "#BA55D3",
    "dark_goldenrod" to "#B8860B",
    "fire_brick" to "#B22222",
    "powder_blue" to "#B0E0E6",
    "light_steel_blue" to "#B0C4DE",
    "pale_turquoise" to "#AFEEEE",
    "green_yellow" to "#ADFF2F",
    "light_blue" to "#ADD8E6",
    "dark_gray" to "#A9A9A9",
    "brown" to "#A52A2A",
    "sienna" to "#A0522D",
    "yellow_green" to "#9ACD32",
    "dark_orchid" to "#9932CC",
    "pale_green" to "#98FB98",
    "dark_violet" to "#9400D3",
    "medium_purple" to "#9370DB",
    "light_green" to "#90EE90",
    "dark_sea_green" to "#8FBC8F",
    "saddle_brown" to "#8B4513",
    "dark_magenta" to "#8B008B",
    "dark_red" to "#8B0000",
    "blue_violet" to "#8A2BE2",
    "light_sky_blue" to "#87CEFA",
    "sky_blue" to "#87CEEB",
    "gray" to "#808080",
    "olive" to "#808000",
    "purple" to "#800080",
    "maroon" to "#800000",
    "aquamarine" to "#7FFFD4",
    "chartreuse" to "#7FFF00",
    "lawn_green" to "#7CFC00",
    "medium_slate_blue" to "#7B68EE",
    "light_slate_gray" to "#778899",
    "slate_gray" to "#708090",
    "olive_drab" to "#6B8E23",
    "slate_blue" to "#6A5ACD",
    "dim_gray" to "#696969",
    "medium_aquamarine" to "#66CDAA",
    "cornflower_blue" to "#6495ED",
    "cadet_blue" to "#5F9EA0",
    "dark_olive_green" to "#556B2F",
    "indigo" to "#4B0082",
    "medium_turquoise" to "#48D1CC",
    "dark_slate_blue" to "#483D8B",
    "steel_blue" to "#4682B4",
    "royal_blue" to "#4169E1",
    "turquoise" to "#40E0D0",
    "medium_sea_green" to "#3CB371",
    "lime_green" to "#32CD32",
    "dark_slate_gray" to "#2F4F4F",
    "sea_green" to "#2E8B57",
    "forest_green" to "#228B22",
    "light_sea_green" to "#20B2AA",
    "dodger_blue" to "#1E90FF",
    "midnight_blue" to "#191970",
    "aqua" to "#00FFFF",
    "cyan" to "#00FFFF",
    "spring_green" to "#00FF7F",
    "lime" to "#00FF00",
    "medium_spring_green" to "#00FA9A",
    "dark_turquoise" to "#00CED1",
    "deep_sky_blue" to "#00BFFF",
    "dark_cyan" to "#008B8B",
    "teal" to "#008080",
    "green" to "#008000",
    "dark_green" to "#006400",
    "blue" to "#0000FF",
    "medium_blue" to "#0000CD",
    "dark_blue" to "#00008B",
    "navy" to "#000080",
    "black" to "#000000"
)


//https://gist.github.com/sghael/2930380

//<color name="white">#FFFFFF</color>
//<color name="ivory">#FFFFF0</color>
//<color name="light_yellow">#FFFFE0</color>
//<color name="yellow">#FFFF00</color>
//<color name="snow">#FFFAFA</color>
//<color name="floral_white">#FFFAF0</color>
//<color name="lemon_chiffon">#FFFACD</color>
//<color name="cornsilk">#FFF8DC</color>
//<color name="seashell">#FFF5EE</color>
//<color name="lavender_blush">#FFF0F5</color>
//<color name="papaya_whip">#FFEFD5</color>
//<color name="blanched_almond">#FFEBCD</color>
//<color name="misty_rose">#FFE4E1</color>
//<color name="bisque">#FFE4C4</color>
//<color name="moccasin">#FFE4B5</color>
//<color name="navajo_white">#FFDEAD</color>
//<color name="peach_puff">#FFDAB9</color>
//<color name="cold">#FFD700</color>
//<color name="pink">#FFC0CB</color>
//<color name="light_pink">#FFB6C1</color>
//<color name="orange">#FFA500</color>
//<color name="light_salmon">#FFA07A</color>
//<color name="dark_orange">#FF8C00</color>
//<color name="coral">#FF7F50</color>
//<color name="hot_pink">#FF69B4</color>
//<color name="tomato">#FF6347</color>
//<color name="orange_red">#FF4500</color>
//<color name="deep_pink">#FF1493</color>
//<color name="fuchsia">#FF00FF</color>
//<color name="magenta">#FF00FF</color>
//<color name="red">#FF0000</color>
//<color name="old_lace">#FDF5E6</color>
//<color name="light_goldenrod_yellow">#FAFAD2</color>
//<color name="linen">#FAF0E6</color>
//<color name="antique_white">#FAEBD7</color>
//<color name="salmon">#FA8072</color>
//<color name="ghost_white">#F8F8FF</color>
//<color name="mint_cream">#F5FFFA</color>
//<color name="white_smoke">#F5F5F5</color>
//<color name="beige">#F5F5DC</color>
//<color name="wheat">#F5DEB3</color>
//<color name="sandy_brown">#F4A460</color>
//<color name="azure">#F0FFFF</color>
//<color name="honeydew">#F0FFF0</color>
//<color name="alice_blue">#F0F8FF</color>
//<color name="khaki">#F0E68C</color>
//<color name="light_coral">#F08080</color>
//<color name="pale_goldenrod">#EEE8AA</color>
//<color name="violet">#EE82EE</color>
//<color name="dark_salmon">#E9967A</color>
//<color name="lavender">#E6E6FA</color>
//<color name="light_cyan">#E0FFFF</color>
//<color name="burlyWood">#DEB887</color>
//<color name="plum">#DDA0DD</color>
//<color name="gainsboro">#DCDCDC</color>
//<color name="crimson">#DC143C</color>
//<color name="pale_violet_red">#DB7093</color>
//<color name="goldenrod">#DAA520</color>
//<color name="orchid">#DA70D6</color>
//<color name="thistle">#D8BFD8</color>
//<color name="light_grey">#D3D3D3</color>
//<color name="tan">#D2B48C</color>
//<color name="chocolate">#D2691E</color>
//<color name="peru">#CD853F</color>
//<color name="indian_red">#CD5C5C</color>
//<color name="medium_violet_red">#C71585</color>
//<color name="silver">#C0C0C0</color>
//<color name="dark_khaki">#BDB76B</color>
//<color name="rosy_brown">#BC8F8F</color>
//<color name="medium_orchid">#BA55D3</color>
//<color name="dark_goldenrod">#B8860B</color>
//<color name="fire_brick">#B22222</color>
//<color name="powder_blue">#B0E0E6</color>
//<color name="light_steel_blue">#B0C4DE</color>
//<color name="pale_turquoise">#AFEEEE</color>
//<color name="greenYellow">#ADFF2F</color>
//<color name="light_blue">#ADD8E6</color>
//<color name="dark_gray">#A9A9A9</color>
//<color name="brown">#A52A2A</color>
//<color name="sienna">#A0522D</color>
//<color name="yellow_green">#9ACD32</color>
//<color name="dark_orchid">#9932CC</color>
//<color name="pale_green">#98FB98</color>
//<color name="dark_violet">#9400D3</color>
//<color name="mediumPurple">#9370DB</color>
//<color name="_light_green">#90EE90</color>
//<color name="dark_sea_green">#8FBC8F</color>
//<color name="saddle_brown">#8B4513</color>
//<color name="dark_magenta">#8B008B</color>
//<color name="dark_red">#8B0000</color>
//<color name="blue_violet">#8A2BE2</color>
//<color name="light_sky_blue">#87CEFA</color>
//<color name="sky_blue">#87CEEB</color>
//<color name="gray">#808080</color>
//<color name="olive">#808000</color>
//<color name="purple">#800080</color>
//<color name="maroon">#800000</color>
//<color name="aquamarine">#7FFFD4</color>
//<color name="chartreuse">#7FFF00</color>
//<color name="lawn_green">#7CFC00</color>
//<color name="medium_slate_blue">#7B68EE</color>
//<color name="light_slate_gray">#778899</color>
//<color name="slate_gray">#708090</color>
//<color name="olive_drab">#6B8E23</color>
//<color name="slate_blue">#6A5ACD</color>
//<color name="dim_gray">#696969</color>
//<color name="medium_aquamarine">#66CDAA</color>
//<color name="cornflower_blue">#6495ED</color>
//<color name="cadet_blue">#5F9EA0</color>
//<color name="dark_olive_green">#556B2F</color>
//<color name="indigo">#4B0082</color>
//<color name="medium_turquoise">#48D1CC</color>
//<color name="dark_slate_blue">#483D8B</color>
//<color name="steel_blue">#4682B4</color>
//<color name="royal_blue">#4169E1</color>
//<color name="turquoise">#40E0D0</color>
//<color name="medium_sea_green">#3CB371</color>
//<color name="lime_green">#32CD32</color>
//<color name="dark_slate_gray">#2F4F4F</color>
//<color name="sea_green">#2E8B57</color>
//<color name="forest_green">#228B22</color>
//<color name="light_sea_green">#20B2AA</color>
//<color name="dodger_blue">#1E90FF</color>
//<color name="midnight_blue">#191970</color>
//<color name="aqua">#00FFFF</color>
//<color name="cyan">#00FFFF</color>
//<color name="spring_green">#00FF7F</color>
//<color name="lime">#00FF00</color>
//<color name="medium_spring_green">#00FA9A</color>
//<color name="dark_turquoise">#00CED1</color>
//<color name="deep_sky_blue">#00BFFF</color>
//<color name="dark_cyan">#008B8B</color>
//<color name="teal">#008080</color>
//<color name="green">#008000</color>
//<color name="dark_green">#006400</color>
//<color name="blue">#0000FF</color>
//<color name="medium_blue">#0000CD</color>
//<color name="dark_blue">#00008B</color>
//<color name="navy">#000080</color>
//<color name="black">#000000</color>