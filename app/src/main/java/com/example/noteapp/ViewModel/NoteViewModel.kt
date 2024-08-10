package com.example.noteapp.ViewModel

import androidx.lifecycle.*
import com.example.noteapp.DAO.Note
import com.example.noteapp.DAO.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _userId = MutableLiveData<String>()

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun getAllNotes(userId: String): LiveData<List<Note>> {
        return repository.getAllNotes(userId).asLiveData()
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun deleteNote(noteId: Int, userId: String) = viewModelScope.launch {
        repository.deleteNote(noteId, userId)
    }

    suspend fun getNoteById(id: Int, userId: String): Note? {
        return repository.getNoteById(id, userId)
    }
}


class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
