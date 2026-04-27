package com.androidagent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidagent.CompanionApplication
import com.androidagent.data.database.entities.AgentContact
import com.androidagent.databinding.FragmentContactsAutoreplyBinding
import com.androidagent.databinding.ItemContactAutoreplyBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsAutoReplyFragment : Fragment() {

    private var _binding: FragmentContactsAutoreplyBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { CompanionApplication.instance.database }
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsAutoreplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactAdapter { contact, enabled ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.agentContactDao().update(contact.copy(isAutoReplyEnabled = enabled))
                }
            }
        }

        binding.contactsRecycler.apply {
            this.adapter = this@ContactsAutoReplyFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.btnAddContact.setOnClickListener { showAddContactDialog() }

        db.agentContactDao().getAll().observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            binding.emptyText.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddContactDialog() {
        val nameInput = android.widget.EditText(requireContext()).apply { hint = "Contact Name" }
        val phoneInput = android.widget.EditText(requireContext()).apply {
            hint = "Phone Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
            addView(nameInput)
            addView(phoneInput)
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Contact")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                if (name.isNotBlank()) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            db.agentContactDao().insert(AgentContact(name = name, phoneNumber = phone))
                        }
                    }
                } else {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ContactAdapter(
    private val onToggle: (AgentContact, Boolean) -> Unit
) : ListAdapter<AgentContact, ContactAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AgentContact>() {
            override fun areItemsTheSame(old: AgentContact, new: AgentContact) = old.id == new.id
            override fun areContentsTheSame(old: AgentContact, new: AgentContact) = old == new
        }
    }

    inner class ViewHolder(private val binding: ItemContactAutoreplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: AgentContact) {
            binding.contactName.text = contact.name
            binding.contactPhone.text = contact.phoneNumber.ifBlank { contact.email.ifBlank { "No number" } }
            binding.autoReplySwitch.isChecked = contact.isAutoReplyEnabled
            binding.autoReplySwitch.setOnCheckedChangeListener { _, checked ->
                onToggle(contact, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemContactAutoreplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
