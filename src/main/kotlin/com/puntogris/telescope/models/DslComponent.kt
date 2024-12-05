package com.puntogris.telescope.models

import javax.swing.JComponent

interface DslComponent {
    fun createContent(): JComponent
}