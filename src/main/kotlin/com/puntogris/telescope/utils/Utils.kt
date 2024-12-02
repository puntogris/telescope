package com.puntogris.telescope.utils

import ai.grazie.utils.capitalize
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import java.nio.file.Path
import java.nio.file.Paths

fun sendNotification(project: Project, message: String, type: NotificationType) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup(PLUGIN_NAME.capitalize())
        .createNotification(message, type)
        .notify(project)
}

val configPath: Path = Paths.get(PathManager.getConfigPath(), PLUGIN_NAME)