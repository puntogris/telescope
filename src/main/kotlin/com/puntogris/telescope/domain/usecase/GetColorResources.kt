package com.puntogris.telescope.domain.usecase

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.puntogris.telescope.utils.iterable
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

private const val COLOR_TAG = "color"
private const val NAME_ATTR = "name"

class GetColorResources {

    // mutableMapOf<module name, Map<color name, hex value>>()
    operator fun invoke(baseDirs: List<VirtualFile>): Map<String, Map<String, String>> {
        val colorRes = mutableMapOf<String, Map<String, String>>()

        for (dir in baseDirs) {
            val colorsFile = dir.findFile("src/main/res/values/colors.xml")
            if (colorsFile != null) {
                colorRes[dir.name] = extractColorFromFile(colorsFile)
            }
        }
        return colorRes
    }

    private fun extractColorFromFile(file: VirtualFile): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.inputStream)
        document.documentElement.normalize()

        val colorNodes = document.getElementsByTagName(COLOR_TAG).iterable.filterIsInstance<Element>()

        return buildMap {
            colorNodes.forEach {
                val name = it.getAttribute(NAME_ATTR)
                val value = it.textContent.trim()

                // TODO For now we ignore colors that point to a style res, like ?primaryColor
                if (name.isNotEmpty() && value.isNotEmpty() && !name.startsWith("?")) {
                    put(name, value)
                }
            }
        }
    }
}