package com.puntogris.telescope.domain.usecase

import com.android.tools.idea.gradle.variant.conflict.displayName
import com.android.tools.idea.projectsystem.gradle.getAllDependencies
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findFile
import com.puntogris.telescope.domain.Globals
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.Resources
import com.puntogris.telescope.utils.iterable
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

private const val COLOR_TAG = "color"
private const val NAME_ATTR = "name"
private const val MAIN_MODULE = "main"
private const val ROOT_MAIN_MODULE_SUFFIX = ".main"
private const val COLORS_FILE_PATH = "res/values/colors.xml"
private const val DRAWABLES_DIR_PATH = "res/drawable"

class GetResources {

    private val dpiVariants = listOf(
        "mipmap-hdpi",
        "mipmap-mdpi",
        "mipmap-xhdpi",
        "mipmap-xxhhdpi",
        "mipmap-xxxhdpi"
    )

    operator fun invoke(project: Project): Resources {
        val manager = ModuleManager.getInstance(project)
        val modules = manager.modules.filter { it.name.endsWith(ROOT_MAIN_MODULE_SUFFIX) }

        val dependencies = getModuleDependencies(modules)
        val colors = getColorsResources(modules)

        Globals.setModuleColors(colors)
        Globals.setModuleDependencies(dependencies)

        return Resources(
            drawables = getDrawableResources(modules),
            colors = getColorsResources(modules),
            dependencies = getModuleDependencies(modules)
        )
    }

    private fun getModuleDependencies(modules: List<Module>): Map<String, List<String>> {
        return buildMap {
            for (module in modules) {
                val dependencies = module.getAllDependencies(false)
                    .map { it.displayName }
                    .filter { it == module.displayName }
                put(module.displayName, dependencies)
            }
        }
    }

    private fun getDrawableResources(modules: List<Module>): List<DrawableRes> {
        val singlesRes = modules.flatMap(::extractSinglesFromModule)
        val variantsRes = modules.flatMap(::extractVariantsFromModule)

        return (singlesRes + variantsRes).sortedBy { it.name }
    }

    private fun extractVariantsFromModule(module: Module): List<DrawableRes.WithVariants> {
        val tempDpiRes = mutableMapOf<String, MutableMap<String, VirtualFile>>()
        val manager = ModuleRootManager.getInstance(module)
        val root = manager.contentRoots.find { it.name == MAIN_MODULE }

        if (root == null) {
            return emptyList()
        }
        for (variant in dpiVariants) {
            root.findDirectory("res/${variant}")
                ?.children
                ?.forEach { tempDpiRes.computeIfAbsent(it.name) { mutableMapOf() }[variant] = it }
        }
        return tempDpiRes.map { DrawableRes.WithVariants.from(it, module) }
    }

    private fun extractSinglesFromModule(module: Module): List<DrawableRes.Simple> {
        val drawables = ModuleRootManager.getInstance(module).contentRoots
            .find { it.name == MAIN_MODULE }
            ?.findDirectory(DRAWABLES_DIR_PATH)
            ?.children.orEmpty()

        return drawables.map { DrawableRes.Simple.from(it, module) }
    }

    private fun getColorsResources(modules: List<Module>): Map<String, Map<String, String>> {
        val colorRes = mutableMapOf<String, Map<String, String>>()

        for (module in modules) {
            val manager = ModuleRootManager.getInstance(module)
            val root = manager.contentRoots.find { it.name == MAIN_MODULE } ?: continue
            val colorsFile = root.findFile(COLORS_FILE_PATH) ?: continue

            colorRes[module.displayName] = extractColorFromFile(colorsFile)
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