package com.puntogris.telescope.ui.components

import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.util.ui.JBUI
import com.puntogris.telescope.utils.documentText
import org.jdesktop.swingx.AbstractPatternPanel.SEARCH_FIELD_LABEL
import java.awt.Dimension
import java.util.Timer
import javax.swing.event.DocumentEvent
import kotlin.concurrent.schedule

private const val DEBOUNCE_MS = 200L
private const val COMPONENT_HEIGHT = 40
private val GAP_SIZE = JBUI.scale(10)

class SearchPanel(
    private val onChange: (String) -> Unit
) : SearchTextField(true) , DocumentListener{

    private var debounceTimer: Timer? = null

    init {
        isFocusable = true
        border = JBUI.Borders.empty(0, 6)
        toolTipText = SEARCH_FIELD_LABEL
        textEditor.columns = GAP_SIZE
        textEditor.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                onEvent(e)
            }
        })
    }

    override fun getMaximumSize(): Dimension = Dimension(Int.MAX_VALUE, COMPONENT_HEIGHT)

    private fun onEvent(e: DocumentEvent?) {
        debounceTimer?.cancel()
        debounceTimer = Timer().apply {
            schedule(DEBOUNCE_MS) {
                onChange(e?.documentText.orEmpty())
            }
        }
    }
}