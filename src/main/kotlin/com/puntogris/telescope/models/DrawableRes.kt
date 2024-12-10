package com.puntogris.telescope.models

import com.intellij.openapi.vfs.VirtualFile
import com.puntogris.telescope.ui.components.Preview

sealed class DrawableRes {

    abstract val name: String
    abstract val path: String
    abstract val file: VirtualFile
    abstract val preview: Preview

    data class Simple(
        val module: String,
        val dependencies: List<String>,
        override val name: String,
        override val path: String,
        override val file: VirtualFile,
    ) : DrawableRes() {

        override val preview: Preview = Preview.from(file, mapOf())

        companion object {
            fun from(file: VirtualFile, module: String, dependencies: List<String>) = Simple(
                file = file,
                name = file.name,
                path = file.path,
                module = module,
                dependencies = dependencies
            )
        }
    }

    data class WithVariants(
        val module: String,
        val variants: List<DrawableVariant>,
        override val name: String,
        override val path: String,
        override val file: VirtualFile
    ) : DrawableRes() {

        override val preview: Preview = Preview.from(file, mapOf())

        companion object {
            fun from(entry: Map.Entry<String, Map<String, VirtualFile>>): WithVariants {
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
    val module: String,
    val parentDirName: String
)