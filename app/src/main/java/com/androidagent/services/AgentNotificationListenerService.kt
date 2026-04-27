package com.androidagent.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.androidagent.CompanionApplication

class AgentNotificationListenerService : NotificationListenerService() {

    private val agentManager by lazy {
        (application as? CompanionApplication)?.agentManager
    }

    private val monitoredPackages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.google.android.gm",
        "com.microsoft.outlook",
        "com.samsung.android.messaging",
        "com.google.android.apps.messaging"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName ?: return
        if (pkg !in monitoredPackages) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        val manager = agentManager ?: return
        val maxie = manager.maxieRobot

        when {
            pkg.contains("whatsapp") -> {
                maxie.onSmsReceived(title, text)
                manager.updateMaxieStatus("WhatsApp from $title! 💬")
            }
            pkg.contains("gm") || pkg.contains("outlook") -> {
                maxie.onEmailReceived(title)
                manager.updateMaxieStatus("Email from $title! 📧")
            }
            else -> {
                maxie.onSmsReceived(title, text)
                manager.updateMaxieStatus("Message from $title! 📱")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // no-op
    }
}
