package com.example.bookbud.features.auth.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bookbud.R
import com.example.bookbud.features.auth.presentation.viewmodel.LoginUiEvent
import com.example.bookbud.features.auth.presentation.viewmodel.LoginViewModel
import com.example.bookbud.features.auth.presentation.viewmodel.RegisterUiEvent
import com.example.bookbud.features.auth.presentation.viewmodel.RegisterViewModel
import com.example.bookbud.features.dashboard.presentation.activity.DashboardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val registerViewModel: RegisterViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    loginViewModel.googleLogin(idToken)
                } else {
                    Toast.makeText(this, "Failed to retrieve Google ID Token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val editName = findViewById<EditText>(R.id.editName)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editConfirmPassword = findViewById<EditText>(R.id.editConfirmPassword)
        val togglePassword = findViewById<ImageView>(R.id.togglePassword)
        val toggleConfirmPassword = findViewById<ImageView>(R.id.toggleConfirmPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val buttonGoogleRegister = findViewById<Button>(R.id.buttonGoogleRegister)
        val textLoginLink = findViewById<TextView>(R.id.textLoginLink)
        val progressRegister = findViewById<ProgressBar>(R.id.progressRegister)

        // Password visibility toggles
        var isPasswordVisible = false
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            editPassword.setSelection(editPassword.text.length)
        }

        var isConfirmPasswordVisible = false
        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                editConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off)
            }
            editConfirmPassword.setSelection(editConfirmPassword.text.length)
        }

        // Configure Google Sign-In Options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Register button listener
        buttonRegister.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val confirmPassword = editConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val parts = name.split(" ")
                    val firstName = parts.firstOrNull() ?: ""
                    val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else "User"
                    registerViewModel.register(firstName, lastName, email, password)
                }
            }
        }

        // Google register button listener
        buttonGoogleRegister.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        // Back to login
        textLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Observe UI States & Events
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe credential-based registration
                launch {
                    registerViewModel.uiState.collect { state ->
                        progressRegister.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        buttonRegister.isEnabled = !state.isLoading
                        buttonGoogleRegister.isEnabled = !state.isLoading
                    }
                }
                
                launch {
                    registerViewModel.uiEvent.collect { event ->
                        when (event) {
                            is RegisterUiEvent.ShowError -> {
                                Toast.makeText(this@RegisterActivity, event.message, Toast.LENGTH_LONG).show()
                            }
                            is RegisterUiEvent.NavigateToDashboard -> {
                                Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                                finish()
                            }
                        }
                    }
                }

                // Observe Google registration (shares LoginViewModel success/fail events)
                launch {
                    loginViewModel.uiState.collect { state ->
                        progressRegister.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        buttonRegister.isEnabled = !state.isLoading
                        buttonGoogleRegister.isEnabled = !state.isLoading
                    }
                }

                launch {
                    loginViewModel.uiEvent.collect { event ->
                        when (event) {
                            is LoginUiEvent.ShowError -> {
                                Toast.makeText(this@RegisterActivity, event.message, Toast.LENGTH_LONG).show()
                            }
                            is LoginUiEvent.NavigateToDashboard -> {
                                Toast.makeText(this@RegisterActivity, "Registered successfully with Google!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
