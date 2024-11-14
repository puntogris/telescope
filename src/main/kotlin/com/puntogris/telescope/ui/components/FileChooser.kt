package com.puntogris.telescope.ui.components

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun chooseFile(project: Project): VirtualFile? {
    val fileDescription = FileChooserDescriptor(false, false, false, false, false, false)
    return FileChooser.chooseFile(fileDescription, project, null)
}