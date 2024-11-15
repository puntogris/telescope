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

    init {
        maximumHeight = 40
        val searchField = JTextField(30)
        searchField.document.addDocumentListener(this)
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
        if (e == null) {
            onChange("")
        } else {
            onChange(e.documentText)
        }
    }
}