// File: com/example/noteapp/AllNoteActivity.kt

package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.DAO.NoteDatabase
import com.example.noteapp.DAO.NoteRepository
import com.example.noteapp.ViewModel.NoteViewModel
import com.example.noteapp.ViewModel.NoteViewModelFactory
import com.example.noteapp.adapter.NoteAdapter
import com.example.noteapp.fragment.AddNoteFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class AllNoteActivity : AppCompatActivity() {
    private lateinit var emptyStateImage: ImageView
    private lateinit var emptyStateExtraText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var fragmentContainer: FragmentContainerView
    private lateinit var textView: TextView
    private lateinit var imageButton: ImageButton
    private lateinit var logoutButton: ImageButton
    private lateinit var welcomeTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_all_note)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: run {
            // If no user is logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        initializeViews()
        setupViewModel()
        setupFab()
        welcomeTitle()
        setupLogout()

        val user = auth.currentUser
        user?.let {
            noteViewModel.getAllNotes(it.uid).observe(this) { notes ->
                noteAdapter.updateNotes(notes)
            }
        }
        noteViewModel.getAllNotes(userId).observe(this) { notes ->
            noteAdapter.updateNotes(notes)
            updateEmptyState(notes.isEmpty())
        }
        supportFragmentManager.setFragmentResultListener("noteAdded", this) { _, _ ->
            showParentViews()
            refreshNotes()
        }
    }
    override fun onResume() {
        super.onResume()
        refreshNotes()  // Refresh notes to ensure latest data is displayed
    }
    private fun refreshNotes() {
        noteViewModel.getAllNotes(userId).observe(this) { notes ->
            noteAdapter.updateNotes(notes)
            updateEmptyState(notes.isEmpty())
        }
    }
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        fragmentContainer = findViewById(R.id.fragment_container)
        textView = findViewById(R.id.textView)
        imageButton = findViewById(R.id.imageButton)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        logoutButton = findViewById(R.id.logoutButton)
        emptyStateImage = findViewById(R.id.emptyStateImage)
        emptyStateText = findViewById(R.id.emptyStateText)
        emptyStateExtraText = findViewById(R.id.emptyStateExtraText)
        setupRecyclerView()
    }

    private fun welcomeTitle() {
        val user = auth.currentUser
        user?.let {
            val displayName = it.displayName ?: "User"
            welcomeTextView.text = "Welcome, $displayName"
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(emptyList()) { note ->
            val intent = Intent(this, DetailNoteActivity::class.java)
            intent.putExtra("noteId", note.id)
            startActivity(intent)
        }
        recyclerView.adapter = noteAdapter
    }

    private fun setupViewModel() {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        val repository = NoteRepository(noteDao)
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
    }

    private fun setupFab() {
        fab.setOnClickListener {
            showAddNoteFragment()
        }
    }

    private fun setupLogout() {
        logoutButton.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateImage.alpha = 0f
            emptyStateText.alpha = 0f
            emptyStateText.alpha = 0f
            emptyStateImage.visibility = View.VISIBLE
            emptyStateText.visibility = View.VISIBLE
            emptyStateExtraText.visibility =View.VISIBLE
            emptyStateImage.animate().alpha(1f).setDuration(300).start()
            emptyStateText.animate().alpha(1f).setDuration(330).start()
            emptyStateExtraText.animate().alpha(1f).setDuration(350).start()
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateImage.animate().alpha(0f).setDuration(300).withEndAction {
                emptyStateImage.visibility = View.GONE
            }.start()
            emptyStateText.animate().alpha(0f).setDuration(300).withEndAction {
                emptyStateText.visibility = View.GONE
            }.start()
            emptyStateExtraText.animate().alpha(0f).setDuration(300).withEndAction {
                emptyStateExtraText.visibility = View.GONE
            }.start()
        }
    }
    private fun showAddNoteFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, AddNoteFragment())
            addToBackStack(null)
        }
        // Hide parent activity views
        recyclerView.visibility = View.GONE
        fab.visibility = View.GONE
        textView.visibility = View.GONE
        imageButton.visibility = View.GONE
        welcomeTextView.visibility = View.GONE
        emptyStateImage.visibility = View.GONE
        emptyStateExtraText.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        logoutButton.visibility =View.GONE
        // Show fragment container
        fragmentContainer.visibility = View.VISIBLE
    }


    private fun showParentViews() {
        fragmentContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        logoutButton.visibility =View.VISIBLE
        fab.visibility = View.VISIBLE
        textView.visibility = View.VISIBLE
        imageButton.visibility = View.VISIBLE
        welcomeTextView.visibility = View.VISIBLE
        emptyStateImage.visibility = View.VISIBLE
        emptyStateExtraText.visibility = View.VISIBLE
        emptyStateText.visibility = View.VISIBLE
    }

    private fun logOut() {
        val sharedPreferences = getSharedPreferences("MyNoteApp", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        FirebaseAuth.getInstance().signOut()
        navigateToLoginActivity()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showAlertDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            logOut()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Kiểm tra xem fragment hiện tại có phải là AddNoteFragment không
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is AddNoteFragment) {
                // Hiển thị dialog xác nhận trước khi thoát
                showExitConfirmationDialog()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard changes?")
            .setMessage("Are you sure you want to discard your changes?")
            .setPositiveButton("Discard") { _, _ ->
                supportFragmentManager.popBackStack()
                showParentViews()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    fun keepFragmentVisible() {
        recyclerView.visibility = View.GONE
        fab.visibility = View.GONE
        textView.visibility = View.GONE
        imageButton.visibility = View.GONE
        welcomeTextView.visibility = View.GONE
        emptyStateImage.visibility = View.GONE
        emptyStateExtraText.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        logoutButton.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
    }
}

