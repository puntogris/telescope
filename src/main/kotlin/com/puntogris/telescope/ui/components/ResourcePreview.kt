package com.puntogris.telescope.ui.components

import com.android.tools.idea.ui.resourcemanager.plugin.DesignAssetRenderer
import com.android.tools.idea.ui.resourcemanager.plugin.DesignAssetRendererManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.PNG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.swing.JPanel

abstract class ResourcePreview {

    abstract fun render(): JPanel

    companion object {
        const val IMAGE_SIZE = 50

        fun from(res: DrawableRes): ResourcePreview {
            val renderer = DesignAssetRendererManager.getInstance().getViewer(res.file)

            return if (renderer.isFileSupported(res.file)) {
                ValidPreview(renderer, res.file, res.module)
            } else {
                NotSupportedPreview
            }
        }
    }

    class ValidPreview(
        private val renderer: DesignAssetRenderer,
        private val file: VirtualFile,
        private val module: Module
    ) : ResourcePreview() {

        override fun render(): JPanel {
            return try {
                var image: Image? = DiskCache.getIfPresent(file.path)

                if (image == null) {
                    val imageFuture = renderer.getImage(file, module, Dimension(50, 50))

                    CoroutineScope(Dispatchers.Default).launch {
                        image = imageFuture.await()
                        withContext(Dispatchers.IO) {
                            DiskCache.put(requireNotNull(image), PNG, file.path)
                        }
                    }
                }

                return object : JPanel() {
                    init {
                        preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
                    }

                    override fun paintComponent(g: Graphics) {
                        super.paintComponent(g)
                        image?.let {
                            g.drawImage(image, 0, 0, this)
                        }
                    }
                }
            } catch (ignored: Throwable) {
                NotSupportedPreview.render()
            }
        }
    }

    data object NotSupportedPreview : ResourcePreview() {
        override fun render(): JPanel {
            return object : JPanel() {
                init {
                    preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    MyIcons.NotSupportedIcon.paintIcon(this, g, 0, 0)
                }
            }
        }
    }
}