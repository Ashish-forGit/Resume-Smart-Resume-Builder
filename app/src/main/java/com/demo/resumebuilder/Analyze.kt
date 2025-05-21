package com.demo.resumebuilder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Analyze : AppCompatActivity() {
    private lateinit var cardUpload: MaterialCardView
    private lateinit var tvUpload: MaterialTextView
    private lateinit var btnAnalyze: MaterialCardView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvAnalyzeText: MaterialTextView
    private var selectedPdfUri: Uri? = null

    private val GEMINI_API_KEY = "AIzaSyA_-k8as3w-Ck6dJ4qiY2A88ll2vCOXdH0"
    private val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent"

    private val pickPdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPdfUri = uri
                val fileName = getFileName(uri)
                animateFileSelection(fileName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()
        setContentView(R.layout.activity_analyze)

        initializeViews()
        setupClickListeners()
        animateInitialViews()
    }

    private fun setupWindowAnimations() {
        window.requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.enterTransition = MaterialFadeThrough()
        window.exitTransition = MaterialFadeThrough()
        window.decorView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
    }

    private fun initializeViews() {
        cardUpload = findViewById(R.id.cardUpload)
        tvUpload = findViewById(R.id.tvUpload)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        progressIndicator = findViewById(R.id.progressIndicator)
        tvAnalyzeText = findViewById(R.id.tvAnalyzeText)

        btnAnalyze.alpha = 0.5f
        btnAnalyze.isEnabled = false
    }

    private fun setupClickListeners() {
        cardUpload.setOnClickListener {
            animateCardClick(it) { selectPdf() }
        }
        btnAnalyze.setOnClickListener {
            if (btnAnalyze.isEnabled) {
                animateCardClick(it) {
                    selectedPdfUri?.let { uri -> analyzeResume(uri) }
                }
            }
        }
    }

    private fun animateInitialViews() {
        val views = arrayOf(cardUpload, tvUpload, btnAnalyze, tvAnalyzeText)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 100f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(index * 100L)
                .setInterpolator(AnticipateOvershootInterpolator())
                .start()
        }
    }

    private fun animateCardClick(view: View, onAnimationEnd: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction { onAnimationEnd() }
                    .start()
            }
            .start()
    }

    private fun animateFileSelection(fileName: String) {
        tvUpload.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                tvUpload.text = "Selected: $fileName"
                tvUpload.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

                btnAnalyze.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction { btnAnalyze.isEnabled = true }
                    .start()
            }
            .start()
    }

    private fun selectPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickPdfLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun analyzeResume(uri: Uri) {
        showLoading(true)
        val pdfText = extractTextFromPdf(uri)
        if (pdfText.isEmpty()) {
            showError("Failed to extract text from the PDF.")
            return
        }
        sendToGemini(pdfText)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressIndicator.visibility = View.VISIBLE
            btnAnalyze.isEnabled = false
            cardUpload.isEnabled = false
        } else {
            progressIndicator.visibility = View.GONE
            btnAnalyze.isEnabled = true
            cardUpload.isEnabled = true
        }
    }

    private fun extractTextFromPdf(uri: Uri): String {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                PdfReader(inputStream).use { pdfReader ->
                    PdfDocument(pdfReader).use { pdfDocument ->
                        val stringBuilder = StringBuilder()
                        for (i in 1..pdfDocument.numberOfPages) {
                            stringBuilder.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)))
                            stringBuilder.append("\n")
                        }
                        stringBuilder.toString()
                    }
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun sendToGemini(resumeText: String) {
        val client = OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(
                        JSONObject().apply {
                            put("text", "Please analyze this resume and provide feedback: $resumeText")
                        }
                    ))
                }
            ))
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(GEMINI_API_URL)
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", GEMINI_API_KEY)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showError("Failed to analyze resume: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    progressIndicator.visibility = View.GONE
                    btnAnalyze.isEnabled = true

                    if (!response.isSuccessful) {
                        showError("Error: ${response.message} - ${responseBody}")
                    } else {
                        try {
                            val suggestions = parseSuggestions(responseBody ?: "")
                            val intent = Intent(this@Analyze, AnalysisResult::class.java)
                            intent.putExtra("analysis_result", suggestions)
                            startActivity(intent)
                        } catch (e: Exception) {
                            showError("Error parsing response: ${e.message}")
                        }
                    }
                }
            }
        })
    }

    private fun parseSuggestions(responseBody: String): String {
        val json = JSONObject(responseBody)
        return try {
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Error parsing response: ${e.message}"
        }
    }

    private fun showError(message: String) {
        showLoading(false)
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                selectedPdfUri?.let { analyzeResume(it) }
            }
            .show()
    }
}