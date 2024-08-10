package com.example.noteapp.fragment

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.noteapp.AllNoteActivity
import com.example.noteapp.DAO.Note
import com.example.noteapp.DAO.NoteDatabase
import com.example.noteapp.DAO.NoteRepository
import com.example.noteapp.R
import com.example.noteapp.ViewModel.NoteViewModel
import com.example.noteapp.ViewModel.NoteViewModelFactory
import com.example.noteapp.databinding.FragmentAddNoteBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var imagePath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? AllNoteActivity)?.keepFragmentVisible()
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    insertImageIntoDescription(it)
                    imagePath = saveImageToInternalStorage(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as? AllNoteActivity)?.keepFragmentVisible()
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupListeners()
        setCurrentDate()
    }
    private fun insertImageIntoDescription(imageUri: Uri) {
        try {
            val spannable = SpannableStringBuilder(binding.detailDescriptionAdd.text)
            val start = binding.detailDescriptionAdd.selectionStart
            val imageSpan = ImageSpan(requireContext(), imageUri, ImageSpan.ALIGN_BASELINE)
            spannable.insert(start, "\uFFFC")
            spannable.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.detailDescriptionAdd.setText(spannable, TextView.BufferType.SPANNABLE)
            binding.detailDescriptionAdd.setSelection(start + 1)

            view?.postDelayed({
                (activity as? AllNoteActivity)?.keepFragmentVisible()
            }, 100)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error inserting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveImageToInternalStorage(imageUri: Uri): String? {
        return try {
            // Decode the image from URI
            val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            // Create a file to save the image
            val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)

            // Write the bitmap to the file
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Return the file path as a string
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupViewModel() {
        val noteDao = NoteDatabase.getDatabase(requireActivity().application).noteDao()
        val repository = NoteRepository(noteDao)
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
    }

    private fun setupListeners() {
        binding.addImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.imageBackButtonAdd.setOnClickListener {
            lifecycleScope.launch {
                parentFragmentManager.setFragmentResult("noteAdded", bundleOf())
                parentFragmentManager.popBackStack()
            }
        }
        binding.buttonSaveAdd.setOnClickListener {
            saveNote()
        }
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        binding.detailDateAdd.text = dateFormat.format(Date())
    }

    private fun saveNote() {
        val title = binding.detailTitleAdd.text.toString()
        val description = binding.detailDescriptionAdd.text.toString()
        val emoji = binding.detailEmoji.tag as? Int ?: R.drawable.frame

        if (title.isBlank()) {
            showToast("Title cannot be empty")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val note = Note(
            id = 0,
            title = title,
            description = description,
            emoji = emoji,
            imagePath = imagePath,  // Ensure imagePath is saved here
            userId = userId
        )

        lifecycleScope.launch {
            noteViewModel.insert(note)
            // Chỉ set fragment result và pop back stack sau khi lưu note
            parentFragmentManager.setFragmentResult("noteAdded", bundleOf())
            parentFragmentManager.popBackStack()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }
}
