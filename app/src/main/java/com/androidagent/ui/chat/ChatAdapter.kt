package com.androidagent.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.androidagent.data.database.entities.Message
import com.androidagent.databinding.ItemMessageAgentBinding
import com.androidagent.databinding.ItemMessageUserBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AGENT = 1
        private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(old: Message, new: Message) = old.id == new.id
            override fun areContentsTheSame(old: Message, new: Message) = old == new
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_AGENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(
                ItemMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> AgentViewHolder(
                ItemMessageAgentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AgentViewHolder -> holder.bind(message)
        }
    }

    inner class UserViewHolder(private val binding: ItemMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.content
            binding.messageTime.text = TIME_FORMAT.format(Date(message.timestamp))
        }
    }

    inner class AgentViewHolder(private val binding: ItemMessageAgentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val emoji = when (message.agentName) {
                "MAXIE" -> "💬"
                "FELIX" -> "📁"
                "NEXUS" -> "🌐"
                "MEMO" -> "🧠"
                else -> "🤖"
            }
            binding.agentEmoji.text = emoji
            binding.agentName.text = message.agentName
            binding.messageText.text = message.content
            binding.messageTime.text = TIME_FORMAT.format(Date(message.timestamp))
        }
    }
}
