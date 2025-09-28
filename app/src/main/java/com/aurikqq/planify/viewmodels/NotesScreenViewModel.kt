package com.aurikqq.planify.viewmodels

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.NotesScreenUiState
import com.aurikqq.planify.PlansRepository
import com.aurikqq.planify.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.String

@Suppress("UNCHECKED_CAST")
class NotesScreenViewModelFactory(
    private val repo: PlansRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesScreenViewModel::class.java)) {
            return NotesScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NotesScreenViewModel(private val repo: PlansRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesScreenUiState())
    val uiState: StateFlow<NotesScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val notes = repo.getNotes()
            val haveNotes = repo.getHaveNotes()

            _uiState.update {
                it.copy (
                    notes = notes,
                    haveNotes = haveNotes,
                )
            }
        }
    }

    fun onTempNotesInput(input: String) {
        _uiState.update {
            it.copy(
                tempNotes = input
            )
        }
    }

    fun setNotes() {
        val tempNotes = _uiState.value.tempNotes

        repo.setNotes(tempNotes)
        repo.setHaveNotes(true)

        _uiState.update {
            it.copy(
                notes = tempNotes,
                haveNotes = true,
                tempNotes = ""
            )
        }

        repo.sendToast(R.string.toast_set_plans, Toast.LENGTH_SHORT)
    }

    fun startEditing() {
        val notes = _uiState.value.notes

        _uiState.update {
            it.copy(
                isEditing = true,
                tempNotes = notes
            )
        }
    }

    fun endEditing() {
        val tempNotes = _uiState.value.tempNotes

        repo.setNotes(tempNotes, true)

        _uiState.update {
            it.copy(
                notes = tempNotes,
                isEditing = false,
                tempNotes = ""
            )
        }

        repo.sendToast(R.string.toast_edited_plans, Toast.LENGTH_SHORT)
    }
}