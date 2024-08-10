package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebaseAuth()
        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        with(binding) {
            loginButton.setOnClickListener {
                val email = usernameEditText.text.toString().trim()
                val pass = passwordEditText.text.toString().trim()
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Please enter complete information", Toast.LENGTH_SHORT).show()
                } else {
                    showProgress(true)
                    loginWithEmailPassword(email, pass)
                }
            }
            registerTextView.setOnClickListener { navigateToSignInActivity() }
            imageView.setOnClickListener {
                showProgress(true)
                signIn()
            }
        }
    }

    private fun navigateToAllNoteActivity() {
        startActivity(Intent(this, AllNoteActivity::class.java))
        finish()
    }

    private fun navigateToSignInActivity() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun signIn() {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showProgress(false)
                if (task.isSuccessful) {
                    saveLoginState()
                    navigateToAllNoteActivity()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveLoginState() {
        val sharedPreferences = getSharedPreferences("MyNoteApp", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("userId", mAuth.currentUser?.uid)
        editor.apply()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showProgress(false)
                if (task.isSuccessful) {
                    saveLoginState()
                    navigateToAllNoteActivity()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            handleGoogleSignInResult(data)
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
            account?.idToken?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            showProgress(false)
            Toast.makeText(this, "Google sign-in failed, please try again later!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressBarLogin.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
    }
}
