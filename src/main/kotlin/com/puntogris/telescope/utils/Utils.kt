package com.puntogris.telescope.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun sendNotification(project: Project, message: String, type: NotificationType) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Telescope")
        .createNotification(message, type)
        .notify(project)
}