package com.puntogris.telescope.models

import com.intellij.openapi.vfs.VirtualFile

sealed class DrawableDir {

    abstract val name: String
    abstract val path: String
    abstract val file: VirtualFile

    data class Simple(
        val module: String,
        override val name: String,
        override val path: String,
        override val file: VirtualFile
    ) : DrawableDir() {

        companion object {
            fun from(file: VirtualFile) = Simple(
                file = file,
                name = file.name,
                path = file.path,
                module = ""
            )
        }
    }

    data class WithVariants(
        val module: String,
        val variants: List<DrawableVariant>,
        override val name: String,
        override val path: String,
        override val file: VirtualFile
    ) : DrawableDir() {

        companion object {
            fun from(entry: Map.Entry<String, HashMap<String, VirtualFile>>): WithVariants {
                val first = entry.value.entries.first().value

                return WithVariants(
                    file = first,
                    name = first.name,
                    path = first.path,
                    module = "",
                    variants = entry.value.entries.map { v ->
                        DrawableVariant(
                            file = v.value,
                            name = v.value.name,
                            mainPath = v.value.path,
                            module = "",
                            parentDirName = v.key
                        )
                    }
                )
            }
        }
    }
}

data class DrawableVariant(
    val file: VirtualFile,
    val name: String,
    val mainPath: String,
    val module: String,
    val parentDirName: String
)