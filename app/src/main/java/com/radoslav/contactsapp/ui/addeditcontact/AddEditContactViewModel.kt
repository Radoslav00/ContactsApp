package com.radoslav.contactsapp.ui.addeditcontact

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radoslav.contactsapp.data.Contact
import com.radoslav.contactsapp.data.ContactDao
import com.radoslav.contactsapp.ui.ADD_CONTACT_RESULT_OK
import com.radoslav.contactsapp.ui.EDIT_CONTACT_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditContactViewModel @ViewModelInject constructor(
    private val contactDao: ContactDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val contact = state.get<Contact>("contact")

    var contactFirstName = state.get<String>("contactFirstName") ?: contact?.first_name ?: ""
        set(value) {
            field = value
            state.set("contactFirstName", value)
        }
    var contactLastName = state.get<String>("contactLastName") ?: contact?.last_name ?: ""
        set(value) {
            field = value
            state.set("contactLastName", value)
        }
    var contactCategory = state.get<String>("contactCategory") ?: contact?.category ?: ""
        set(value) {
            field = value
            state.set("contactCategory", value)
        }
    var contactPhoneNumber = state.get<String>("contactPhoneNumber") ?: contact?.phone_number ?: ""
        set(value) {
            field = value
            state.set("contactPhoneNumber", value)
        }
    var contactEmailAddress = state.get<String>("contactEmailAddress") ?: contact?.email_address ?: ""
        set(value) {
            field = value
            state.set("contactEmailAddress", value)
        }
    var contactFavourite = state.get<Boolean>("contactFavourite") ?: contact?.favourite ?: false
        set(value) {
            field = value
            state.set("contactFavourite", value)
        }

    private val addEditContactEventChannel = Channel<AddEditContactEvent>()
    val addEditContactEvent = addEditContactEventChannel.receiveAsFlow()
    private val regex: Regex = Regex("^[a-zA-Z]+\$")

    fun onSaveClick(){
        if (contactPhoneNumber.isBlank() || contactPhoneNumber.length > 13) {
            showInvalidInputMessage("Phone number cannot be blank and it must not exceed 13 digits!")
            return
        }
        if (!contactFirstName.matches(regex)) {
            showInvalidInputMessage( "First name must contain only letters")
            return
        }
        if (contactLastName.isNotBlank() && !contactLastName.matches(regex)) {
            showInvalidInputMessage("Last name must contain only letters")
            return
        }

        if (contact != null) {
            val updatedContact = contact.copy(
                first_name = contactFirstName,
                last_name = contactLastName,
                category = contactCategory,
                phone_number = contactPhoneNumber,
                email_address = contactEmailAddress,
                favourite = contactFavourite
            )
            updateContact(updatedContact)
        } else {
            val newContact = Contact(
                first_name = contactFirstName,
                last_name = contactLastName,
                category = contactCategory,
                phone_number = contactPhoneNumber,
                email_address = contactEmailAddress,
                favourite = contactFavourite
            )
             createContact(newContact)
        }

    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditContactEventChannel.send(AddEditContactEvent.ShowInvalidInputMessage(text))
    }

    private fun createContact(contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
        addEditContactEventChannel.send(AddEditContactEvent.NavigateBackWithResult(ADD_CONTACT_RESULT_OK))
    }

    private fun updateContact(contact: Contact) = viewModelScope.launch {
        contactDao.update(contact)
        addEditContactEventChannel.send(AddEditContactEvent.NavigateBackWithResult(EDIT_CONTACT_RESULT_OK))
    }

    sealed class AddEditContactEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditContactEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditContactEvent()
    }
}