package com.demo.resumebuilder

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import android.view.LayoutInflater

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        setupViews()
        setupLoadingDialog()
    }

    private fun setupViews() {
        val emailInput = findViewById<TextInputEditText>(R.id.email)
        val passwordInput = findViewById<TextInputEditText>(R.id.password)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val signupButton = findViewById<MaterialButton>(R.id.signupButton)

        emailInput.addTextChangedListener { validateInputs(emailInput, passwordInput, loginButton) }
        passwordInput.addTextChangedListener { validateInputs(emailInput, passwordInput, loginButton) }

        loginButton.setOnClickListener {
            if (validateInputs(emailInput, passwordInput, loginButton)) {
                handleLogin(emailInput.text.toString().trim(), passwordInput.text.toString().trim())
            }
        }

        signupButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.scaleX = 1f
                    it.scaleY = 1f
                    startActivity(Intent(this, SignUp::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
        }
    }

    private fun setupLoadingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
    }

    private fun validateInputs(
        emailInput: TextInputEditText,
        passwordInput: TextInputEditText,
        loginButton: MaterialButton
    ): Boolean {
        var isValid = true

        when {
            emailInput.text.isNullOrEmpty() -> {
                emailInput.error = "Email is required"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailInput.text!!).matches() -> {
                emailInput.error = "Invalid email format"
                isValid = false
            }
            else -> emailInput.error = null
        }

        when {
            passwordInput.text.isNullOrEmpty() -> {
                passwordInput.error = "Password is required"
                isValid = false
            }
            passwordInput.text!!.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                isValid = false
            }
            else -> passwordInput.error = null
        }

        loginButton.isEnabled = isValid
        return isValid
    }

    private fun handleLogin(email: String, password: String) {
        loadingDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    Snackbar.make(findViewById(android.R.id.content), "Login Successful", Snackbar.LENGTH_SHORT)
                        .show() // Removed setAnchorView to avoid ambiguity

                    startActivity(Intent(this, Choice::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    showErrorSnackbar("Login Failed: ${task.exception?.localizedMessage}")
                }
            }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { }
            .setActionTextColor(getColor(com.google.android.material.R.color.design_default_color_error))
            .show()
    }

    override fun onDestroy() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        super.onDestroy()
    }
}