package com.radoslav.contactsapp.ui.contacts

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radoslav.contactsapp.R
import com.radoslav.contactsapp.data.Contact
import com.radoslav.contactsapp.data.SortOrder
import com.radoslav.contactsapp.databinding.FragmentContactsBinding
import com.radoslav.contactsapp.util.exhaustive
import com.radoslav.contactsapp.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment (R.layout.fragment_contacts), ContactsAdapter.OnItemClickListener {

    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentContactsBinding.bind(view)

        val contactAdapter = ContactsAdapter(this)

        binding.apply {
            recyclerViewContacts.apply {
                adapter = contactAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val contact = contactAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onContactSwiped(contact)
                }
            }).attachToRecyclerView(recyclerViewContacts)

            fabAddContact.setOnClickListener{
                viewModel.onAddNewContactClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.contacts.observe(viewLifecycleOwner) {
            contactAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.contactsEvent.collect { event ->
                when (event) {
                    is ContactsViewModel.ContactsEvent.ShowUndoDeleteContactMessage -> {
                        Snackbar.make(requireView(), "Contact deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.contact)
                            }.show()
                    }
                    is ContactsViewModel.ContactsEvent.NavigateToAddContactScreen -> {
                        val action = ContactsFragmentDirections.actionContactsFragmentToAddEditContactFragment(null, "New Contact")
                        findNavController().navigate(action)
                    }
                    is ContactsViewModel.ContactsEvent.NavigateToEditContactScreen -> {
                        val action = ContactsFragmentDirections.actionContactsFragmentToAddEditContactFragment(event.contact, "Edit Contact")
                        findNavController().navigate(action)

                    }
                    is ContactsViewModel.ContactsEvent.ShowContactSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(contact: Contact) {
        viewModel.onContactSelected(contact)
    }

    override fun onCheckBoxClick(contact: Contact, isChecked: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_contacts, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuerry = viewModel.searchQuerry.value
        if (pendingQuerry != null && pendingQuerry.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuerry, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuerry.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_show_favourite_contacts).isChecked =
                viewModel.preferencesFlow.first().showFavourite
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
               true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_show_favourite_contacts ->{
                item.isChecked = !item.isChecked
                viewModel.onShowFavouriteClick(item.isChecked)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}