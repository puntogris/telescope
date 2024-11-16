package com.puntogris.telescope.models

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.puntogris.telescope.domain.Cache
import com.puntogris.telescope.domain.VectorDrawableConverter
import com.puntogris.telescope.ui.components.SVGPanel
import com.puntogris.telescope.utils.JPEG
import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.WEBP
import com.puntogris.telescope.utils.XML
import javax.swing.JPanel

sealed class Preview {

    abstract fun render(): JPanel

    protected val defaultPreview by lazy { JPanel() }

    class XmlVector(private val fileContent: String, val path: String) : Preview() {
        override fun render(): JPanel {
            return try {
                val cached = Cache.svg.getIfPresent(path)

                if (cached == null) {
                    val svg = VectorDrawableConverter().transform(fileContent).replaceColors()
                    Cache.svg.put(path, svg)
                    SVGPanel(svg)
                } else {
                    SVGPanel(cached)
                }
            } catch (e: Throwable) {
                defaultPreview
            }
        }
    }

    class Png(private val fileContent: String) : Preview() {
        override fun render(): JPanel {
            return try {
                JPanel()
            } catch (e: Throwable) {
                defaultPreview
            }
        }
    }

    class Webp(private val fileContent: String) : Preview() {
        override fun render(): JPanel {
            return try {
                JPanel()
            } catch (e: Throwable) {
                defaultPreview
            }
        }
    }

    class Jpeg(private val fileContent: String) : Preview() {
        override fun render(): JPanel {
            return try {
                JPanel()
            } catch (e: Throwable) {
                defaultPreview
            }
        }
    }

    data object NotSupported : Preview() {
        override fun render(): JPanel {
            return defaultPreview
        }
    }

    protected fun String.replaceColors(): String {
        val fillRegex = """fill="[^"]*"""".toRegex()
        val strokeRegex = """stroke="[^"]*"""".toRegex()

        return replace(fillRegex, """fill="#000000"""").replace(strokeRegex, """stroke="#000000"""")
    }

    companion object {

        fun from(file: VirtualFile): Preview {
            val content = file.readText()

            return when (file.extension) {
                XML -> {
                    if (content.startsWith("<vector")) {
                        XmlVector(content, file.path)
                    } else {
                        NotSupported
                    }
                }

                PNG -> Png(content)
                JPEG -> Jpeg(content)
                WEBP -> Webp(content)
                else -> NotSupported
            }
        }
    }
}
