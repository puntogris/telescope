package com.puntogris.telescope.ui.components

import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.JPEG
import com.puntogris.telescope.utils.JPG
import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.WEBP
import com.puntogris.telescope.utils.XML
import com.puntogris.telescope.utils.createRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.JPanel

abstract class ResourcePreview {

    abstract fun render(): JPanel

    companion object {
        const val IMAGE_SIZE = 50

        fun from(res: DrawableRes): ResourcePreview {
            return when (res.file.extension) {
                XML -> {
                    VectorPreview(res.path, res.module, res.file)
                }

                PNG, JPEG, JPG, WEBP -> RasterPreview(res.file)
                else -> NotSupportedPreview
            }
        }
    }
}

class VectorPreview(
    private val path: String,
    private val module: Module,
    private val file: VirtualFile
) : ResourcePreview() {

    override fun render(): JPanel {
        return try {
            var image: Image? = DiskCache.getIfPresent(path)

            if (image == null) {
                val imageFuture = createRenderer(module, file).thenCompose {
                    it.renderDrawable(String(file.contentsToByteArray()), Dimension(50, 50))
                }
                MainScope().launch(Dispatchers.Default) {
                    image = imageFuture.await()
                    withContext(Dispatchers.IO) {
                        DiskCache.put(requireNotNull(image), PNG, path)
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

class RasterPreview(private val file: VirtualFile) : ResourcePreview() {
    override fun render(): JPanel {
        return try {
            val cached = DiskCache.getIfPresent(file.path)

            val image = if (cached != null) {
                cached
            } else {
                val new = ImageIO.read(file.inputStream).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH)
                DiskCache.put(new, file.extension.orEmpty(), file.path)
                new
            }
            return object : JPanel() {
                init {
                    preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.drawImage(image, 0, 0, this)
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
