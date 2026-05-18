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
import com.example.bookbud.features.dashboard.presentation.activity.DashboardActivity
import com.example.bookbud.features.admin.presentation.activity.AdminDashboardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    viewModel.googleLogin(idToken)
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
        setContentView(R.layout.activity_login)

        val editLoginEmail = findViewById<EditText>(R.id.editLoginEmail)
        val editLoginPassword = findViewById<EditText>(R.id.editLoginPassword)
        val toggleLoginPassword = findViewById<ImageView>(R.id.toggleLoginPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonGoogleLogin = findViewById<Button>(R.id.buttonGoogleLogin)
        val textForgotPassword = findViewById<TextView>(R.id.textForgotPassword)
        val textNoAccount = findViewById<TextView>(R.id.textNoAccount)
        val progressLogin = findViewById<ProgressBar>(R.id.progressLogin)

        // Password visibility toggle
        var isPasswordVisible = false
        toggleLoginPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                editLoginPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleLoginPassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editLoginPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleLoginPassword.setImageResource(R.drawable.ic_eye_off)
            }
            editLoginPassword.setSelection(editLoginPassword.text.length)
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Login button action
        buttonLogin.setOnClickListener {
            val email = editLoginEmail.text.toString().trim()
            val password = editLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
            }
        }

        // Google Login button action
        buttonGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        // Forgot Password link
        textForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Register link
        textNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Observe UI State & Events
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        progressLogin.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        buttonLogin.isEnabled = !state.isLoading
                        buttonGoogleLogin.isEnabled = !state.isLoading
                    }
                }
                
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is LoginUiEvent.ShowError -> {
                                Toast.makeText(this@LoginActivity, event.message, Toast.LENGTH_LONG).show()
                            }
                            is LoginUiEvent.NavigateToDashboard -> {
                                Toast.makeText(this@LoginActivity, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                                val role = com.example.bookbud.shared.auth.TokenManager.getRole() ?: "USER"
                                val nextActivity = if (role.equals("ADMIN", ignoreCase = true)) {
                                    AdminDashboardActivity::class.java
                                } else {
                                    DashboardActivity::class.java
                                }
                                startActivity(Intent(this@LoginActivity, nextActivity))
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
