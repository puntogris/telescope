package com.puntogris.telescope.models

import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile

sealed class DrawableRes {

    abstract val name: String
    abstract val path: String
    abstract val module: Module
    abstract val file: VirtualFile

    data class Simple(
        override val module: Module,
        override val name: String,
        override val path: String,
        override val file: VirtualFile,
    ) : DrawableRes() {

        companion object {
            fun from(file: VirtualFile, module: Module) = Simple(
                file = file,
                name = file.name,
                path = file.path,
                module = module
            )
        }
    }

    data class WithVariants(
        override val module: Module,
        override val name: String,
        override val path: String,
        override val file: VirtualFile,
        val variants: List<DrawableVariant>,
    ) : DrawableRes() {

        companion object {
            fun from(entry: Map.Entry<String, Map<String, VirtualFile>>, module: Module): WithVariants {
                val first = entry.value.entries.first().value

                return WithVariants(
                    file = first,
                    name = first.name,
                    path = first.path,
                    module = module,
                    variants = entry.value.entries.map { v ->
                        DrawableVariant(
                            file = v.value,
                            name = v.value.name,
                            mainPath = v.value.path,
                            module = module,
                            parentDirName = v.key
                        )
                    }.sortedBy { it.parentDirName }
                )
            }
        }
    }
}

data class DrawableVariant(
    val file: VirtualFile,
    val name: String,
    val mainPath: String,
    val module: Module,
    val parentDirName: String
)