package com.example.noteapp.DAO
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)
    @Update
    suspend fun update(note: Note)
    @Delete
    suspend fun delete(note: Note)
    @Query("SELECT * FROM notes WHERE id = :id AND userId = :userId")
    suspend fun getNoteById(id: Int, userId: String): Note?
    /*@Query("SELECT * FROM notes WHERE userId = :userId ORDER BY id DESC")
    fun getAllNotes(userId: String): Flow<List<Note>>*/
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
    @Query("DELETE FROM notes WHERE id = :noteId AND userId = :userId")
    suspend fun deleteById(noteId: Int, userId: String)
    // Cập nhật truy vấn để sắp xếp theo creationTime
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY creationTime DESC")
    fun getAllNotes(userId: String): Flow<List<Note>>
}