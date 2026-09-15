package com.aurikqq.planify.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.NotesScreenUiState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val isExpanded: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
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

    val db = Firebase.firestore

    init {
        loadInitialData()
        observeRepositoryChanges()
    }

    private fun observeRepositoryChanges() {
        viewModelScope.launch {
            repo.plansUpdatedFlow.collect {
                val newPrefix = repo.getPlansPrefix()
                val notesButtonAtEnd = repo.getNotesButtonPlacement()
                _uiState.update {
                    it.copy(
                        plansPrefix = newPrefix,
                        isNotesButtonAtEnd = notesButtonAtEnd
                    )
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val notesList = withContext(Dispatchers.IO) { repo.getNotesList() }
            val sortedNotes = withContext(Dispatchers.Default) {
                notesList.sortedWith(compareByDescending<Note> { it.timestamp }.thenByDescending { it.id })
                    .toMutableList()
            }

            _uiState.update {
                it.copy (
                    notes = sortedNotes,
                    isSignedIn = repo.getIsSignedIn(),
                    email = repo.getEmail(),
                    plansPrefix = repo.getPlansPrefix(),
                    isNotesButtonAtEnd = repo.getNotesButtonPlacement()
                )
            }
        }
    }

    fun onNoteTextInput(input: String) {
        repo.setTempNoteText(input)
        _uiState.update {
            it.copy(
                tempNote = input
            )
        }
    }

    fun onNoteTitleEditingInput(note: Note, input: String) {
        val updatedNote = note.copy(title = input)
        _uiState.update { state ->
            val newNotes = state.notes.map { if (it.id == note.id) updatedNote else it }.toMutableList()
            state.copy(notes = newNotes)
        }
    }

    fun onNoteTextEditingInput(note: Note, input: String) {
        val updatedNote = note.copy(text = input)
        _uiState.update { state ->
            val newNotes = state.notes.map { if (it.id == note.id) updatedNote else it }.toMutableList()
            state.copy(notes = newNotes)
        }
    }

    fun onNoteTitleInput(input: String) {
        repo.setTempNoteTitle(input)
        _uiState.update {
            it.copy(
                tempNoteTitle = input
            )
        }
    }
    // TODO manage these methods, there's plenty of them and they're small - do they actually have point?

    fun isAddingNote(value: Boolean) {
        _uiState.update {
            it.copy(
                isAddingNote = value
            )
        }
        if (!value) {
            _uiState.update {
                it.copy(
                    tempNoteTitle = "",
                    tempNote = ""
                )
            }
        }
    }

    fun isEditing(value: Boolean) {
        _uiState.update {
            it.copy(
                isEditing = value
            )
        }
    }

    fun setNote(note: Note = Note()) {
        val isNew = note.id.isBlank()
        val finalNote = if (isNew) Note(
            UUID.randomUUID().toString(),
            _uiState.value.tempNoteTitle,
            _uiState.value.tempNote,
            timestamp = System.currentTimeMillis()
        ) else note

        if (!isNew) {
            _uiState.update { state ->
                val newNotes = state.notes.map { if (it.id == finalNote.id) finalNote else it }.toMutableList()
                state.copy(notes = newNotes)
            }
        }

        viewModelScope.launch {
            if (_uiState.value.isSignedIn && isOnline())
                sendNoteToDatabase(finalNote)
            repo.saveNote(finalNote)
            val notesList = withContext(Dispatchers.IO) { repo.getNotesList() }
            val sortedList = withContext(Dispatchers.Default) {
                notesList.sortedWith(compareByDescending<Note> { it.timestamp }.thenByDescending { it.id })
                    .toMutableList()
            }

            _uiState.update {
                it.copy(
                    notes = sortedList,
                    tempNoteTitle = "",
                    tempNote = ""
                )
            }
        }
    }

    fun removeNote(note: Note) {
        _uiState.update { state ->
            val newNotes = state.notes.filter { it.id != note.id }.toMutableList()
            state.copy(notes = newNotes)
        }
        viewModelScope.launch {
            if (_uiState.value.isSignedIn && isOnline())
                removeNoteFromDatabase(note)
            repo.removeNote(note)
        }
    }

    fun sendNoteToDatabase(note: Note) {
        if (isOnline()) {
            val noteHash = hashMapOf(
                "title" to note.title,
                "text" to note.text,
                "is_expanded" to note.isExpanded,
                "timestamp" to note.timestamp
            )

            db.collection(_uiState.value.email)
                .document("notes")
                .collection("notes_collection")
                .document(note.id)
                .set(noteHash)
                .addOnSuccessListener { noteRef ->
                    Log.d("Notes Sync", "Note added: $noteRef")
                }
                .addOnFailureListener { e ->
                    Log.w("Notes Sync", "Error adding note", e)
                }
        }
    }

    fun sendNotesListToDatabase() {
        db.collection(_uiState.value.email)
            .document("notes")
            .collection("notes_collection")
    }

    fun getNotesFromDatabase() {
        val email = _uiState.value.email
        if (email.isNotBlank() && email != "null" && isOnline()) {
            db.collection(email)
                .document("notes")
                .collection("notes_collection")
                .get()
                .addOnSuccessListener { notes ->
                    viewModelScope.launch {
                        val result = withContext(Dispatchers.Default) {
                            notes.map { note ->
                                Note(
                                    note.id,
                                    note.get("title").toString(),
                                    note.get("text").toString(),
                                    note.get("is_expanded") as Boolean,
                                    note.get("timestamp") as? Long ?: 0L
                                )
                            }.sortedWith(compareByDescending<Note> { it.timestamp }.thenByDescending { it.id })
                                .toMutableList()
                        }
                        Log.d("Plans Sync", "Imported notes from DB")

                        _uiState.update {
                            it.copy(
                                notes = result
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Plans Sync", "Error importing notes from DB", e)
                }
        }
    }

    fun removeNoteFromDatabase(note: Note) {
        if (isOnline()) {
            db.collection(_uiState.value.email)
                .document("notes")
                .collection("notes_collection")
                .document(note.id)
                .delete()
                .addOnSuccessListener { noteRef ->
                    Log.d("Notes Sync", "Note deleted: $noteRef")
                }
                .addOnFailureListener { e ->
                    Log.w("Notes Sync", "Error deleting note", e)
                }
        }
    }

//    fun raiseNote(note: Note) {
//        val notesList = repo.getNotesList()
//        val index = notesList.indexOfFirst { it.id == note.id }
//        notesList.removeAt(index)
//        notesList.add(index - 1, note)
//        _uiState.update {
//            it.copy(
//                notes = notesList
//            )
//        }
//    }

    fun toggleNotePlanCompletion(note: Note, index: Int) {
        val prefix = _uiState.value.plansPrefix
        val lines = note.text.lines().toMutableList()
        if (index in lines.indices) {
            val line = lines[index]
            val trimmed = line.trimStart()
            val leadingSpaces = line.takeWhile { it.isWhitespace() }

            val updatedLine = if (trimmed.startsWith("$prefix*")) {
                leadingSpaces + prefix + trimmed.substring(prefix.length + 1)
            } else if (trimmed.startsWith(prefix)) {
                leadingSpaces + prefix + "*" + trimmed.substring(prefix.length)
            } else {
                null
            }

            if (updatedLine != null) {
                lines[index] = updatedLine
                val newText = lines.joinToString("\n")
                val updatedNote = note.copy(text = newText)

                _uiState.update { state ->
                    val newNotes = state.notes.map { if (it.id == note.id) updatedNote else it }.toMutableList()
                    state.copy(notes = newNotes)
                }

                viewModelScope.launch {
                    if (_uiState.value.isSignedIn && isOnline())
                        sendNoteToDatabase(updatedNote)
                    repo.saveNote(updatedNote)
                }
            }
        }
    }

    fun isOnline() : Boolean {
        return repo.isOnline()
    }

/*
    fun updateWidget(note: Note) {
        viewModelScope.launch {
            //repo.updateTextWidgetData(TextWidgetDataTypes.NOTE, note.id)
        }
    }
*/
}