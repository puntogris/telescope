package com.puntogris.telescope.domain

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory

object Files {

    // TODO figure a way to launch AN from or how to check what ide are we using
    // Only to debug in IntelliJ
    fun getResDirectories(project: Project): List<VirtualFile> {
        return project.baseDir.children
            .filter { it.isDirectory }
            .mapNotNull { it.findDirectory("src/main/res/drawable") }
            .flatMap { it.children.toList() }
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

    //TODO we should
    //fetch all new files
    //clear svg cache
    //check path model state valid/checked -> enable/disable embeddings
    //create embeddings and store in db
    fun refresh(project: Project) {
        MemoryCache.svg.invalidateAll()
        DiskCache.invalidateAll()
        if (GlobalStorage.getEmbeddingsState()) {
            // todo create new embeddings
        }
    }
}