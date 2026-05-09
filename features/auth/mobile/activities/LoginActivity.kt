package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : ComponentActivity() {

    private var isPasswordVisible = false
    private lateinit var googleSignInButton: Button
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data ?: return@registerForActivityResult
        try {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = accountTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            
            if (idToken.isNullOrBlank()) {
                Toast.makeText(this, "Unable to get Google token", Toast.LENGTH_LONG).show()
                println("Google Auth Error: idToken is null or blank")
                return@registerForActivityResult
            }

            println("Google Auth: Got idToken successfully")

            googleSignInButton.isEnabled = false
            findViewById<ProgressBar>(R.id.progressLogin).visibility = View.VISIBLE

            Thread {
                try {
                    val resultAuth = AuthApiClient.googleAuth(idToken)
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressLogin).visibility = View.GONE
                        googleSignInButton.isEnabled = true
                        handleAuthResult(resultAuth)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressLogin).visibility = View.GONE
                        googleSignInButton.isEnabled = true
                        Toast.makeText(this, "Auth request failed: ${e.message}", Toast.LENGTH_LONG).show()
                        println("Google Auth API Error: ${e.message}")
                    }
                }
            }.start()
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                10 -> "Google sign-in failed. Please try email/password login."
                12501 -> "Google Play Services not available or outdated."
                12502 -> "Google Play Services missing on device."
                else -> "Google sign-in failed: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            println("Google Sign-In API Exception: Status=${e.statusCode}, Message=${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, "Unexpected error during Google sign-in: ${e.message}", Toast.LENGTH_LONG).show()
            println("Google Sign-In Unexpected Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail: EditText = findViewById(R.id.editLoginEmail)
        val editPassword: EditText = findViewById(R.id.editLoginPassword)
        val togglePassword: ImageView = findViewById(R.id.toggleLoginPassword)
        val progress: ProgressBar = findViewById(R.id.progressLogin)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        googleSignInButton = findViewById(R.id.buttonGoogleLogin)
        val textNoAccount: TextView = findViewById(R.id.textNoAccount)
        val textForgotPassword: TextView = findViewById(R.id.textForgotPassword)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Password toggle
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val cursorPos = editPassword.selectionEnd
            if (isPasswordVisible) {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            editPassword.setSelection(cursorPos)
        }

        textNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        textForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        buttonLogin.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }

        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Valid email is required"; editEmail.requestFocus(); return@setOnClickListener
            }
            if (password.isEmpty()) {
                editPassword.error = "Password is required"; editPassword.requestFocus(); return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            buttonLogin.isEnabled = false

            Thread {
                val result = AuthApiClient.login(email, password)
                runOnUiThread {
                    progress.visibility = View.GONE
                    buttonLogin.isEnabled = true
                    when (result) {
                        is AuthResult.Success -> {
                            if (!result.accessToken.isNullOrBlank() && !result.refreshToken.isNullOrBlank()) {
                                TokenManager.saveTokens(this, result.accessToken, result.refreshToken)
                            }

                            if (!result.userId.isNullOrBlank()) {
                                TokenManager.saveUser(
                                    context = this,
                                    userId = result.userId,
                                    username = result.username ?: "",
                                    email = "",
                                    role = result.role ?: ""
                                )
                            } else {
                                val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
                                prefs.edit().putString("role", result.role ?: "").apply()
                            }

                            // Always navigate to DashboardActivity, admin access shown in ProfileFragment
                            startActivity(Intent(this, DashboardActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                        is AuthResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        googleSignInButton.setOnClickListener {
            googleSignInButton.isEnabled = false
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
        
        // Test backend connection on app start
        Thread {
            val backendConnected = TestConnection.testBackendConnection()
            val googleEndpointExists = TestConnection.testGoogleAuthEndpoint()
            runOnUiThread {
                println("Backend Connection Test: $backendConnected")
                println("Google Auth Endpoint Test: $googleEndpointExists")
                
                if (!backendConnected) {
                    Toast.makeText(this, "Backend server not accessible. Check your connection.", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    
    private fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                if (!result.accessToken.isNullOrBlank() && !result.refreshToken.isNullOrBlank()) {
                    TokenManager.saveTokens(this, result.accessToken, result.refreshToken)
                }

                if (!result.userId.isNullOrBlank()) {
                    TokenManager.saveUser(
                        context = this,
                        userId = result.userId,
                        username = result.username ?: "",
                        email = "",
                        role = result.role ?: ""
                    )
                } else {
                    val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
                    prefs.edit().putString("role", result.role ?: "").apply()
                }

                // Always navigate to DashboardActivity, admin access shown in ProfileFragment
                startActivity(Intent(this, DashboardActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            is AuthResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }
}
