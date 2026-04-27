package com.androidagent.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.androidagent.databinding.ItemFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilesAdapter(
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (File) -> Boolean
) : ListAdapter<File, FilesAdapter.FileViewHolder>(FILE_DIFF) {

    companion object {
        private val FILE_DIFF = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File) =
                oldItem.absolutePath == newItem.absolutePath
            override fun areContentsTheSame(oldItem: File, newItem: File) =
                oldItem.lastModified() == newItem.lastModified() && oldItem.length() == newItem.length()
        }
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.fileName.text = file.name
            binding.fileSize.text = formatSize(file.length())
            binding.fileDate.text = DATE_FORMAT.format(Date(file.lastModified()))
            binding.fileIcon.text = if (file.isDirectory) "📁" else getFileIcon(file.extension)
            binding.root.setOnClickListener { onItemClick(file) }
            binding.root.setOnLongClickListener { onItemLongClick(file) }
        }

        private fun formatSize(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }

        private fun getFileIcon(ext: String): String = when (ext.lowercase()) {
            "txt", "md" -> "📝"
            "json" -> "📋"
            "jpg", "jpeg", "png", "gif" -> "🖼️"
            "mp3", "wav" -> "🎵"
            "mp4", "avi" -> "🎬"
            "pdf" -> "📄"
            else -> "📄"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FileViewHolder(
        ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
