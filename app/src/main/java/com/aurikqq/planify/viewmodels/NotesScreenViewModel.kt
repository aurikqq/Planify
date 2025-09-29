package com.aurikqq.planify.viewmodels

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.NotesScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = "",
    var title: String = "",
    var text: String = ""
)

@Suppress("UNCHECKED_CAST")
class NotesScreenViewModelFactory(
    private val repo: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesScreenViewModel::class.java)) {
            return NotesScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NotesScreenViewModel(private val repo: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesScreenUiState())
    val uiState: StateFlow<NotesScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val notes = repo.getNotesList()

            _uiState.update {
                it.copy (
                    notes = notes,
                )
            }
        }
    }

    fun onNoteTextInput(input: String) {
        _uiState.update {
            it.copy(
                tempNote = input
            )
        }
    }

    fun onNoteTitleEditingInput(input: String) {
        _uiState.update {
            it.copy(
                tempNoteTitle = input
            )
        }
    }

    fun onNoteTextEditingInput(input: String) {
        _uiState.update {
            it.copy(
                tempNote = input
            )
        }
    }

    fun onNoteTitleInput(input: String) {
        _uiState.update {
            it.copy(
                tempNoteTitle = input
            )
        }
    }
    // TODO manage these methods, there's plenty of them and they're small - do they actually have point?

    fun setNote(note: Note = Note()) {
        val note = if(note.id.isBlank()) Note(UUID.randomUUID().toString(), _uiState.value.tempNoteTitle, _uiState.value.tempNote) else note

        repo.saveNote(note)
        val newNotesList = repo.getNotesList()

        _uiState.update {
            it.copy(
                notes = newNotesList,
            )
        }

        repo.sendToast(R.string.toast_set_plans, Toast.LENGTH_SHORT)
    }
}