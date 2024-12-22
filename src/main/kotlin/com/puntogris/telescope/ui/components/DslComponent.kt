package com.puntogris.telescope.ui.components

import javax.swing.JComponent

interface DslComponent {
    fun createContent(): JComponent
}