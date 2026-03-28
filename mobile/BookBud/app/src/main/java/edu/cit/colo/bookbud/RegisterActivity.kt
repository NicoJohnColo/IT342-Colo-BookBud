package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val editName: EditText = findViewById(R.id.editName)
        val editEmail: EditText = findViewById(R.id.editEmail)
        val editPassword: EditText = findViewById(R.id.editPassword)
        val editConfirmPassword: EditText = findViewById(R.id.editConfirmPassword)
        val progress: ProgressBar = findViewById(R.id.progressRegister)
        val buttonRegister: Button = findViewById(R.id.buttonRegister)
        val textHaveAccount: TextView = findViewById(R.id.textHaveAccount)

        textHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        buttonRegister.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (!validateInputs(name, email, password, confirmPassword, editName, editEmail, editPassword, editConfirmPassword)) {
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            buttonRegister.isEnabled = false

            Thread {
                val result = AuthApiClient.register(name, email, password, confirmPassword)
                runOnUiThread {
                    progress.visibility = View.GONE
                    buttonRegister.isEnabled = true

                    when (result) {
                        is AuthResult.Success -> {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        is AuthResult.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.start()
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        editName: EditText,
        editEmail: EditText,
        editPassword: EditText,
        editConfirmPassword: EditText
    ): Boolean {
        if (name.isEmpty()) {
            editName.error = "Name is required"
            editName.requestFocus()
            return false
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Valid email is required"
            editEmail.requestFocus()
            return false
        }

        if (password.length < 6) {
            editPassword.error = "Password must be at least 6 characters"
            editPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            editConfirmPassword.error = "Passwords do not match"
            editConfirmPassword.requestFocus()
            return false
        }

        return true
    }
}
