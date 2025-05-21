package com.demo.resumebuilder

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setupViews()
        setupLoadingDialog()
    }

    private fun setupViews() {
        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val emailInput = findViewById<TextInputEditText>(R.id.email)
        val passwordInput = findViewById<TextInputEditText>(R.id.password)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val signupButton = findViewById<MaterialButton>(R.id.signupButton)

        nameInput.addTextChangedListener { validateInputs(nameInput, emailInput, passwordInput, confirmPasswordInput, signupButton) }
        emailInput.addTextChangedListener { validateInputs(nameInput, emailInput, passwordInput, confirmPasswordInput, signupButton) }
        passwordInput.addTextChangedListener { validateInputs(nameInput, emailInput, passwordInput, confirmPasswordInput, signupButton) }
        confirmPasswordInput.addTextChangedListener { validateInputs(nameInput, emailInput, passwordInput, confirmPasswordInput, signupButton) }

        signupButton.setOnClickListener {
            if (validateInputs(nameInput, emailInput, passwordInput, confirmPasswordInput, signupButton)) {
                hideKeyboard()
                handleSignUp(emailInput.text.toString(), passwordInput.text.toString())
            }
        }
    }

    private fun setupLoadingDialog() {
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            indeterminateTintList = getColorStateList(android.R.color.holo_blue_light)
        }

        loadingDialog = MaterialAlertDialogBuilder(this)
            .setMessage("Creating your account...")
            .setView(progressBar)
            .setCancelable(false)
            .create()
    }

    private fun validateInputs(
        nameInput: TextInputEditText,
        emailInput: TextInputEditText,
        passwordInput: TextInputEditText,
        confirmPasswordInput: TextInputEditText,
        signupButton: MaterialButton
    ): Boolean {
        var isValid = true

        if (nameInput.text.isNullOrEmpty() || nameInput.text!!.length < 3) {
            nameInput.error = "Name must be at least 3 characters"
            isValid = false
        } else nameInput.error = null

        if (emailInput.text.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailInput.text!!).matches()) {
            emailInput.error = "Enter a valid email"
            isValid = false
        } else emailInput.error = null

        if (passwordInput.text.isNullOrEmpty() || passwordInput.text!!.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            isValid = false
        } else passwordInput.error = null

        if (confirmPasswordInput.text.isNullOrEmpty() || confirmPasswordInput.text.toString() != passwordInput.text.toString()) {
            confirmPasswordInput.error = "Passwords do not match"
            isValid = false
        } else confirmPasswordInput.error = null

        signupButton.isEnabled = isValid
        return isValid
    }

    private fun handleSignUp(email: String, password: String) {
        loadingDialog.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    Snackbar.make(findViewById(android.R.id.content), "Account created successfully", Snackbar.LENGTH_SHORT)
                        .show() // Removed setAnchorView to avoid ambiguity
                    startActivity(Intent(this, Choice::class.java))
                    finishAffinity()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Sign up failed: ${task.exception?.message}", Snackbar.LENGTH_LONG)
                        .setAction("Retry") { handleSignUp(email, password) }
                        .show()
                }
            }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}