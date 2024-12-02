package com.puntogris.telescope.domain.usecase

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findFile
import com.puntogris.telescope.domain.iterable
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.Resources
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class GetResourcesUseCase {

    private val dpiVariants = listOf(
        "mipmap-hdpi",
        "mipmap-mdpi",
        "mipmap-xhdpi",
        "mipmap-xxhhdpi",
        "mipmap-xxxhdpi"
    )

    // TODO figure a way to launch AN from or how to check what ide are we using
    // Only to debug in IntelliJ
    operator fun invoke(project: Project): Resources {
        val baseDirs = project.baseDir.children.filter { it.isDirectory }

        return Resources(
            drawablesRes = getDrawableResources(baseDirs),
            colorsRes = getColorResources(baseDirs)
        )
    }

    private fun getDrawableResources(baseDirs: List<VirtualFile>): List<DrawableRes> {
        val drawablesRes = baseDirs
            .mapNotNull { it.findDirectory("src/main/res/drawable") }
            .flatMap { it.children.toList() }
            .map { DrawableRes.Simple.from(it) }

        //  mutableMapOf<file name, MutableMap<dpi dir, VirtualFile>>()
        val tempDpiRes = mutableMapOf<String, MutableMap<String, VirtualFile>>()

        for (variant in dpiVariants) {
            baseDirs.mapNotNull { it.findDirectory("src/main/res/${variant}") }
                .flatMap { it.children.toList() }
                .forEach { tempDpiRes.computeIfAbsent(it.name) { mutableMapOf() }[variant] = it }
        }
        val dpiRes = tempDpiRes.map { DrawableRes.WithVariants.from(it) }

        return (drawablesRes + dpiRes).sortedBy { it.name }
    }

    private fun getColorResources(baseDirs: List<VirtualFile>): Map<String, Map<String, String>> {
        // mutableMapOf<module name, Map<color name, hex value>>()
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

        val colorNodes = document.getElementsByTagName("color").iterable.filterIsInstance<Element>()

        return buildMap {
            colorNodes.forEach {
                val name = it.getAttribute("name")
                val value = it.textContent.trim()

                // TODO For now we ignore colors that point to a style res, like ?primaryColor
                if (name.isNotEmpty() && value.isNotEmpty() && !name.startsWith("?")) {
                    put(name, value)
                }
            }
        }
    }

    // For release, we should use this
//    fun getResDirectoriesV2(project: Project): List<FileEmb> {
//        val moduleManager = ModuleManager.getInstance(project)
//        val modules = moduleManager.modules.filter { it.name.endsWith(".main") }
//
//        //TODO we should look here too, and create a custom item with x variants,
//
//        val drawables =  modules
//            .flatMap { ModuleRootManager.getInstance(it).contentRoots.toList() }
//            .mapNotNull { it.findDirectory("res/drawable") }
//            .flatMap { it.children.toList() }
//
//        return drawables
//    }
}