package com.radoslav.contactsapp.ui.addeditcontact

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radoslav.contactsapp.R
import com.radoslav.contactsapp.databinding.FragmentAddEditContactBinding
import com.radoslav.contactsapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_contact.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditContactFragment : Fragment(R.layout.fragment_add_edit_contact) {

    private val viewModel: AddEditContactViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditContactBinding.bind(view)

        binding.apply {
            contactFirstName.setText(viewModel.contactFirstName)
            contactLastName.setText(viewModel.contactLastName)
            contactCategory.setText(viewModel.contactCategory)
            contactPhoneNumber.setText(viewModel.contactPhoneNumber)
            contactEmail.setText(viewModel.contactEmailAddress)
            checkBoxFavourite.isChecked = viewModel.contactFavourite
            checkBoxFavourite.jumpDrawablesToCurrentState()
            contactDateCreated.isVisible = viewModel.contact != null
            contactDateCreated.text = "Created: ${viewModel.contact?.createdDateFormatted}"

            contactFirstName.addTextChangedListener {
                viewModel.contactFirstName = it.toString()
            }
            contactLastName.addTextChangedListener {
                viewModel.contactLastName = it.toString()
            }
            contactCategory.addTextChangedListener {
                viewModel.contactCategory = it.toString()
            }
            contactPhoneNumber.addTextChangedListener {
                viewModel.contactPhoneNumber = it.toString()
            }
            contactEmail.addTextChangedListener {
                viewModel.contactEmailAddress = it.toString()
            }

            checkBoxFavourite.setOnCheckedChangeListener { _, isChecked ->
                viewModel.contactFavourite = isChecked
            }

            fabSaveContact.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditContactEvent.collect {event ->
                when (event) {
                    is AddEditContactViewModel.AddEditContactEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditContactViewModel.AddEditContactEvent.NavigateBackWithResult -> {
                        binding.contactFirstName.clearFocus()
                        binding.contactLastName.clearFocus()
                        binding.contactCategory.clearFocus()
                        binding.contactPhoneNumber.clearFocus()
                        binding.contactEmail.clearFocus()

                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive

            }
        }
    }
}