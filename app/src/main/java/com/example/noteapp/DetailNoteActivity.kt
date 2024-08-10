package com.example.noteapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.noteapp.DAO.Note
import com.example.noteapp.DAO.NoteDatabase
import com.example.noteapp.DAO.NoteRepository
import com.example.noteapp.ViewModel.NoteViewModel
import com.example.noteapp.ViewModel.NoteViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DetailNoteActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var emojiImageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var noteImageView: ImageView

    private lateinit var noteViewModel: NoteViewModel
    private var currentNoteId: Int = -1
    private lateinit var userId: String
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_detail_note_acitivity)

        initializeViews()
        setupViewModel()
        setupListeners()

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        currentNoteId = intent.getIntExtra("noteId", -1)
        if (currentNoteId != -1) {
            loadNoteDetails()
        } else {
            dateTextView.text = getCurrentDatetime()
            imagePath = intent.getStringExtra("imagePath")
            imagePath?.let {
                noteImageView.setImageURI(Uri.parse(it))
            }
        }
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.detail_title)
        descriptionEditText = findViewById(R.id.detail_description)
        dateTextView = findViewById(R.id.detail_date)
        emojiImageView = findViewById(R.id.detail_emoji)
        backButton = findViewById(R.id.imageBackButton)
        deleteButton = findViewById(R.id.imageDeleteButton)
        shareButton = findViewById(R.id.imageShareButton)
        saveButton = findViewById(R.id.buttonSave)
        noteImageView = findViewById(R.id.detail_image)
    }

    private fun setupViewModel() {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        val repository = NoteRepository(noteDao)
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }
        deleteButton.setOnClickListener { showAlertDialog() }
        shareButton.setOnClickListener { shareNote() }
        saveButton.setOnClickListener { saveNoteDetails() }
    }

    private fun loadNoteDetails() {
        lifecycleScope.launch {
            val note = noteViewModel.getNoteById(currentNoteId, userId)
            note?.let {
                titleEditText.setText(it.title)
                descriptionEditText.setText(it.description)
                emojiImageView.setImageResource(it.emoji)

                // Hiển thị thời gian tạo note
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                dateTextView.text = dateFormat.format(Date(it.creationTime))

                emojiImageView.tag = it.emoji

                if (!it.imagePath.isNullOrEmpty()) {
                    val file = File(it.imagePath)
                    if (file.exists()) {
                        noteImageView.setImageURI(Uri.fromFile(file))
                        imagePath = it.imagePath
                    } else {
                        Toast.makeText(this@DetailNoteActivity, "Image not found at the saved path.", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                Toast.makeText(this@DetailNoteActivity, "Note not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveNoteDetails() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val emoji = emojiImageView.tag as? Int ?: R.drawable.frame

        if (title.isBlank()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

    /*    // Ensure imagePath is correctly set
        if (imagePath == null && currentNoteId != -1) {
            // Load the existing image path if available
            val existingNote = noteViewModel.getNoteById(currentNoteId, userId)
            imagePath = existingNote?.imagePath
        }
*/
        val note = Note(
            id = if (currentNoteId != -1) currentNoteId else 0,
            title = title,
            description = description,
            emoji = emoji,
            imagePath = imagePath,  // Set the image path here
            userId = userId
        )

        lifecycleScope.launch {
            if (currentNoteId != -1) {
                noteViewModel.update(note)
            } else {
                noteViewModel.insert(note)
            }
            finish()  // Return to AllNoteActivity
        }
    }

    private fun showAlertDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            deleteNote()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteNote() {
        if (currentNoteId != -1) {
            lifecycleScope.launch {
                noteViewModel.getNoteById(currentNoteId, userId)?.let {
                    noteViewModel.deleteNote(it.id, userId)
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    private fun shareNote() {
        val noteTitle = titleEditText.text.toString()
        val noteDescription = descriptionEditText.text.toString()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "$noteTitle\n\n$noteDescription")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share note via"))
    }

    private fun getCurrentDatetime(): String {
        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDate.format(formatter)
    }
}
