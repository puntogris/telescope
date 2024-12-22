package com.puntogris.telescope.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.puntogris.telescope.models.DrawableRes

private const val MAIN_MODULE = "main"
private const val ROOT_MAIN_MODULE_SUFFIX = ".main"
private const val DRAWABLES_DIR_PATH = "res/drawable"

@Service(Service.Level.PROJECT)
class ResourcesService(private val project: Project) {

    private val dpiVariants = listOf(
        "mipmap-hdpi",
        "mipmap-mdpi",
        "mipmap-xhdpi",
        "mipmap-xxhhdpi",
        "mipmap-xxxhdpi"
    )

    fun getDrawableResources(): List<DrawableRes> {
        val manager = ModuleManager.getInstance(project)
        val modules = manager.modules.filter { it.name.endsWith(ROOT_MAIN_MODULE_SUFFIX) }

        return getDrawableResources(modules)
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

    companion object {
        fun getInstance(project: Project): ResourcesService = project.service()
    }
}