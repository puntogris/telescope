package com.puntogris.telescope.domain

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.puntogris.telescope.models.DrawableDir
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Files {

    private val dpiDirs = listOf(
        "mipmap-hdpi",
        "mipmap-mdpi",
        "mipmap-xhdpi",
        "mipmap-xxhhdpi",
        "mipmap-xxxhdpi"
    )

    // TODO figure a way to launch AN from or how to check what ide are we using
    // Only to debug in IntelliJ
    fun getResDirectories(project: Project): List<DrawableDir> {
        val baseDirs = project.baseDir.children.filter { it.isDirectory }

        val drawablesDirs = baseDirs
            .mapNotNull { it.findDirectory("src/main/res/drawable") }
            .flatMap { it.children.toList() }
            .map { DrawableDir.Simple.from(it) }

        //  hashMapOf<file name, HashMap<dpi dir, VirtualFile>>()
        val tempDpiDirs = hashMapOf<String, HashMap<String, VirtualFile>>()

        for (dir in dpiDirs) {
            baseDirs.mapNotNull { it.findDirectory("src/main/res/${dir}") }
                .flatMap { it.children.toList() }
                .forEach {
                    if (tempDpiDirs.containsKey(it.name)) {
                        tempDpiDirs[it.name]!![dir] = it
                    } else {
                        tempDpiDirs[it.name] = hashMapOf(dir to it)
                    }
                }
        }
        val dpiDirs = tempDpiDirs.map { DrawableDir.WithVariants.from(it) }

        return (drawablesDirs + dpiDirs).sortedBy { it.name }
    }

    // For release, we should use this
//    fun getResDirectoriesV2(project: Project): List<FileEmb> {
//        val moduleManager = ModuleManager.getInstance(project)
//        val modules = moduleManager.modules.filter { it.name.endsWith(".main") }
//
//        //TODO we should look here too, and create a custom item with x variants,
//        val dpi = listOf(
//            "mipmap-hdpi",
//            "mipmap-mdpi",
//            "mipmap-xhdpi",
//            "mipmap-xxhhdpi",
//            "mipmap-xxxhdpi"
//        )
//
//        val drawables =  modules
//            .flatMap { ModuleRootManager.getInstance(it).contentRoots.toList() }
//            .mapNotNull { it.findDirectory("res/drawable") }
//            .flatMap { it.children.toList() }
//
//        return drawables
//    }

    private suspend fun indexFiles(project: Project) {
        getResDirectories(project).forEach {
            Clip.encodeFileImage(it.file).onSuccess { emb ->
                ImagesDB.add(it.path, emb)
            }
        }
    }

    fun refresh(project: Project) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                DiskCache.invalidateAll()
                ImagesDB.removeAll()
                indexFiles(project)
                sendNotification(project, "Telescope sync completed", NotificationType.INFORMATION)
            } catch (e: Exception) {
                sendNotification(project, "Telescope sync completed failed", NotificationType.ERROR)
            }
        }
    }
}