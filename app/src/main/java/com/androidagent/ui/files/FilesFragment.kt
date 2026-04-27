package com.androidagent.ui.files

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidagent.R
import com.androidagent.databinding.FragmentFilesBinding
import com.google.android.material.snackbar.Snackbar

class FilesFragment : Fragment() {

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FilesViewModel by viewModels()
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filesAdapter = FilesAdapter(
            onItemClick = { file ->
                if (file.isFile && file.canRead()) {
                    viewModel.readFile(file)
                }
            },
            onItemLongClick = { file ->
                showDeleteDialog(file)
                true
            }
        )

        binding.filesRecycler.apply {
            adapter = filesAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.files.observe(viewLifecycleOwner) { files ->
            filesAdapter.submitList(files)
            binding.emptyText.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.fileContent.observe(viewLifecycleOwner) { content ->
            content?.let {
                showFileContentDialog(it)
                viewModel.clearFileContent()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }

        binding.fabNewFile.setOnClickListener { showCreateFileDialog() }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshFiles()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showCreateFileDialog() {
        val input = EditText(requireContext()).apply {
            hint = "filename.txt"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Create New File")
            .setMessage("Enter file name:")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) viewModel.createFile(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(file: java.io.File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete File")
            .setMessage("Delete \"${file.name}\"?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteFile(file) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFileContentDialog(content: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("File Content")
            .setMessage(content.ifBlank { "(empty file)" })
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
