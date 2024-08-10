package com.example.noteapp.DAO

import kotlinx.coroutines.flow.Flow
class NoteRepository(private val noteDAO: NoteDAO) {
    suspend fun insert(note: Note) {
        noteDAO.insert(note)
    }

    suspend fun update(note: Note) {
        noteDAO.update(note)
    }

    suspend fun delete(note: Note) {
        noteDAO.delete(note)
    }

    suspend fun getNoteById(id: Int, userId: String): Note? {
        return noteDAO.getNoteById(id, userId)
    }

    fun getAllNotes(userId: String): Flow<List<Note>> {
        return noteDAO.getAllNotes(userId)
    }

    suspend fun deleteNote(noteId: Int, userId: String) {
        noteDAO.deleteById(noteId, userId)
    }
}


