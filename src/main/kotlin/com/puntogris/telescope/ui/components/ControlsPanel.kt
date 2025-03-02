package com.puntogris.telescope.ui.components

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.application.Clip
import com.puntogris.telescope.application.GetModelsPath
import com.puntogris.telescope.application.SettingsEvents
import com.puntogris.telescope.storage.GlobalStorage
import com.puntogris.telescope.utils.sendNotification
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

private const val COMPONENT_HEIGHT = 30

class ControlsPanel(
    private val project: Project,
    private val onRefreshClicked: () -> Unit
) : JPanel() {

    private val modelsPath = GetModelsPath()

    private val partialMatchCheckbox = JBCheckBox("Partial Match").apply {
        isSelected = GlobalStorage.getPartialMatchState()
        addActionListener {
            GlobalStorage.sePartialMatchState(isSelected)
        }
    }

    private val embeddingCheckbox = JBCheckBox("Embeddings").apply {
        toolTipText = "To enable this enable on the settings page."
        isEnabled = Clip.canEnableClip()
        isSelected = GlobalStorage.getEmbeddingsState()
        addActionListener {
            if (modelsPath().areValid) {
                GlobalStorage.setEmbeddingsState(isSelected)
            } else {
                isSelected = false
                sendNotification(
                    project,
                    "It looks like no AI models are configured. Please set them up in the settings page.",
                    NotificationType.ERROR
                )
            }
        }
    }

    private val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
        add(JBLabel("Filters:"))
        add(partialMatchCheckbox)
        add(embeddingCheckbox)
    }

    private val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
        val button = JButton(AllIcons.Actions.Refresh)
        button.addActionListener { onRefreshClicked() }
        add(button)
    }

    private val messageBusConnection = project.messageBus.connect()

    init {
        border = JBUI.Borders.empty(0, 6)
        layout = BorderLayout()

        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)

        messageBusConnection.subscribe(SettingsEvents.TOPIC, object : SettingsEvents {
            override fun embeddingsModelDownloaded() {
                embeddingCheckbox.isEnabled = true
            }
        })
    }

    override fun getMaximumSize(): Dimension = Dimension(Int.MAX_VALUE, COMPONENT_HEIGHT)

    override fun removeNotify() {
        super.removeNotify()
        messageBusConnection.disconnect()
    }
}
