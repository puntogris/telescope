package com.puntogris.telescope.domain.usecase

import com.android.tools.idea.gradle.variant.conflict.displayName
import com.android.tools.idea.projectsystem.gradle.getAllDependencies
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.Resources

class GetResources {

    private val dpiVariants = listOf(
        "mipmap-hdpi",
        "mipmap-mdpi",
        "mipmap-xhdpi",
        "mipmap-xxhhdpi",
        "mipmap-xxxhdpi"
    )

    operator fun invoke(project: Project): Resources {
        val baseDirs = project.baseDir.children.filter { it.isDirectory }

        return Resources(
            drawablesRes = getDrawableResources(project, baseDirs),
            colorsRes = GetColorResources().invoke(baseDirs)
        )
    }

    private fun getDrawableResources(project: Project, baseDirs: List<VirtualFile>): List<DrawableRes> {
        val drawablesRes = ModuleManager.getInstance(project).modules
            .filter { it.name.endsWith(".main") }
            .flatMap { extractDrawablesFromModule(it) }

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

    private fun extractDrawablesFromModule(module: Module): List<DrawableRes.Simple> {
        val dependencies = module.getAllDependencies(false)
            .map { it.displayName }
            .filter { it == module.displayName }

        val drawables = ModuleRootManager.getInstance(module).contentRoots
            .find { it.name == "main" }
            ?.findDirectory("res/drawable")
            ?.children.orEmpty()

        return drawables.map { DrawableRes.Simple.from(it, module.displayName, dependencies) }
    }
}