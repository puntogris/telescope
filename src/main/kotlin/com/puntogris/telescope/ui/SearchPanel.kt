package com.puntogris.telescope.ui

import com.intellij.ui.util.maximumHeight
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchPanel(
    private val onChange: (String) -> Unit
): JPanel() {

    private val documentListener = object :DocumentListener{
        override fun insertUpdate(e: DocumentEvent?) {
            if (e == null) {
                onChange("")
            } else {
                onChange(e.document.getText(0, e.document.length))
            }
        }

        override fun removeUpdate(e: DocumentEvent?) {
            if (e == null) {
                onChange("")
            } else {
                onChange(e.document.getText(0, e.document.length))
            }
        }

        override fun changedUpdate(e: DocumentEvent?) {
            if (e == null) {
                onChange("")
            } else {
                onChange(e.document.getText(0, e.document.length))
            }
        }
    }

    init {
        maximumHeight = 40
        val searchField = JTextField(30)
       // searchField.pl = "Search..." // Optional placeholder

        searchField.document.addDocumentListener(documentListener)
        add(searchField)
    }
}