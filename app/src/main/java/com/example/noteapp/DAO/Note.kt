package com.example.noteapp.DAO
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "notes")
data class Note (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "userId") val userId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "emoji") val emoji: Int,
    @ColumnInfo(name = "image") val imagePath: String?,
    @ColumnInfo(name = "creationTime") val creationTime: Long = System.currentTimeMillis(),

)