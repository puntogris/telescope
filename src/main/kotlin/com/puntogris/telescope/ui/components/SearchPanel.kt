package com.puntogris.telescope.ui.components

import com.intellij.ui.util.maximumHeight
import com.puntogris.telescope.utils.documentText
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchPanel(
    private val onChange: (String) -> Unit
) : JPanel(), DocumentListener {

    private val searchField = JTextField(30).apply {
        document.addDocumentListener(this@SearchPanel)
    }

    init {
        maximumHeight = 40
        add(searchField)
    }

    override fun insertUpdate(e: DocumentEvent?) {
        onEvent(e)
    }

    override fun removeUpdate(e: DocumentEvent?) {
        onEvent(e)
    }

    override fun changedUpdate(e: DocumentEvent?) {
        onEvent(e)
    }

    private fun onEvent(e: DocumentEvent?) {
        onChange(e?.documentText.orEmpty())
    }
}