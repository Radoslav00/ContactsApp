package com.radoslav.contactsapp.ui.contacts

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.radoslav.contactsapp.data.Contact
import com.radoslav.contactsapp.data.ContactDao
import com.radoslav.contactsapp.data.PreferencesManager
import com.radoslav.contactsapp.data.SortOrder
import com.radoslav.contactsapp.ui.ADD_CONTACT_RESULT_OK
import com.radoslav.contactsapp.ui.EDIT_CONTACT_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContactsViewModel @ViewModelInject constructor(
    private val contactDao: ContactDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val searchQuerry = state.getLiveData("searchQuerry", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val contactsEventChannel = Channel<ContactsEvent>()
    val contactsEvent = contactsEventChannel.receiveAsFlow()

    private val contactsFlow = combine(
        searchQuerry.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)

    }.flatMapLatest { (query, filterPreferences) ->
        contactDao.getContacts(query, filterPreferences.sortOrder, filterPreferences.showFavourite)
    }
    val contacts = contactsFlow.asLiveData()
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onShowFavouriteClick(showFavourite: Boolean) = viewModelScope.launch {
        preferencesManager.updateShowFavourite(showFavourite)
    }

    fun onContactSelected(contact: Contact) = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.NavigateToEditContactScreen(contact))
    }

    fun onContactSwiped(contact: Contact) = viewModelScope.launch {
        contactDao.delete(contact)
        contactsEventChannel.send(ContactsEvent.ShowUndoDeleteContactMessage(contact))
    }

    fun onUndoDeleteClick (contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
    }

    fun onAddNewContactClick() = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.NavigateToAddContactScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_CONTACT_RESULT_OK -> showContactSavedConfirmationMessage("Contact added")
            EDIT_CONTACT_RESULT_OK -> showContactSavedConfirmationMessage("Contact updated")
        }
    }

    private fun showContactSavedConfirmationMessage(text: String) = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.ShowContactSavedConfirmationMessage(text))
    }

    sealed class ContactsEvent {
        object NavigateToAddContactScreen : ContactsEvent()
        data class NavigateToEditContactScreen(val contact: Contact) : ContactsEvent()
        data class ShowUndoDeleteContactMessage(val contact: Contact) : ContactsEvent()
        data class ShowContactSavedConfirmationMessage (val msg: String) : ContactsEvent()
    }

}

