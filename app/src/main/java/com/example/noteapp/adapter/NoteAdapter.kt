// File: com/example/noteapp/NoteAdapter.kt

package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.DAO.Note
import com.example.noteapp.R

class NoteAdapter(
    private var noteList: List<Note>,
    private val onItemClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.note_title)
        val description: TextView = view.findViewById(R.id.note_description)
        val emoji: ImageView = view.findViewById(R.id.note_emoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount(): Int = noteList.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.title.text = note.title
        holder.description.text = note.description
        holder.emoji.setImageResource(note.emoji)

        holder.itemView.setOnClickListener {
            onItemClick(note)
        }
    }

    fun updateNotes(newNotes: List<Note>) {
        noteList = newNotes
        notifyDataSetChanged()
    }
}
