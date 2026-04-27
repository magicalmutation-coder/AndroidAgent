package com.androidagent.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import com.androidagent.CompanionApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgentSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }

        val app = context.applicationContext as? CompanionApplication ?: return
        val agentManager = app.agentManager
        val db = app.database

        agentManager.maxieRobot.onSmsReceived(sender, body)
        agentManager.updateMaxieStatus("SMS from $sender! 📱")

        CoroutineScope(Dispatchers.IO).launch {
            val contact = db.agentContactDao().getByPhoneNumber(sender)
            val prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
            val masterEnabled = prefs.getBoolean("auto_reply_master", false)
            val smsEnabled = prefs.getBoolean("auto_reply_sms", false)

            if (masterEnabled && smsEnabled && contact?.isAutoReplyEnabled == true) {
                agentManager.maxieRobot.onReplying(contact.name)
                agentManager.updateMaxieStatus("Replying to ${contact.name}... ✍️")

                val llmManager = com.androidagent.llm.LLMManager.getInstance(context)
                val prompt = if (contact.customPrompt.isNotBlank()) {
                    "${contact.customPrompt}\n\nIncoming SMS from ${contact.name}: $body\n\nReply:"
                } else {
                    "Auto-reply to SMS from ${contact.name}: $body\n\nWrite a brief, friendly reply."
                }
                val reply = llmManager.sendRawMessage(prompt)
                if (reply.isNotBlank()) {
                    sendSmsReply(sender, reply)
                }
                agentManager.maxieRobot.resetStatus()
                agentManager.updateMaxieStatus("Monitoring messages 📡")
            }
        }
    }

    private fun sendSmsReply(to: String, message: String) {
        runCatching {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(to, null, parts, null, null)
        }
    }
}
