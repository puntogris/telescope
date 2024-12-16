package com.puntogris.telescope.ui.components

import com.android.tools.idea.gradle.variant.conflict.displayName
import com.puntogris.telescope.models.DrawableRes
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class FileListCellRenderer : ListCellRenderer<DrawableRes> {
    private val label = JLabel().apply { horizontalAlignment = JLabel.CENTER }

    private val iconProvider = IconProvider()

    override fun getListCellRendererComponent(
        list: JList<out DrawableRes>,
        value: DrawableRes,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val view = (list as AssetList).assetView
        label.icon = iconProvider.getIcon(
            drawable = value,
            component = view,
            refreshCallback = { list.getCellBounds(index, index)?.let(list::repaint) },
            shouldBeRendered = { index in list.firstVisibleIndex..list.lastVisibleIndex }
        )
        view.apply {
            selected = isSelected
            focused = cellHasFocus
            thumbnail = label
            title = value.name
            subtitle = value.module.displayName
            metadata = when (value) {
                is DrawableRes.Simple -> "1 version"
                is DrawableRes.WithVariants -> {
                    value.variants.joinToString(" - ") { it.parentDirName.replace("mipmap-", "") }
                }
            }
            withChessboard = true

            // image size plus some padding
            viewWidth = 70
        }
        return view
    }
}