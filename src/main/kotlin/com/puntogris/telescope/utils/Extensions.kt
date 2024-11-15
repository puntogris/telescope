package com.puntogris.telescope.utils

import javax.swing.event.DocumentEvent

val DocumentEvent.documentText: String
    get() = document.getText(0, document.length)
