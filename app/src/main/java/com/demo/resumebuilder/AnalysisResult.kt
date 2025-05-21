package com.demo.resumebuilder

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.platform.MaterialFadeThrough
import io.noties.markwon.Markwon

class AnalysisResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()
        setContentView(R.layout.activity_analysis_result)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val resultCard = findViewById<MaterialCardView>(R.id.resultCard)
        val tvAnalysisResult = findViewById<TextView>(R.id.tvAnalysisResult)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val resultText = intent.getStringExtra("analysis_result") ?: "No analysis available"
        val markwon = Markwon.create(this)
        markwon.setMarkdown(tvAnalysisResult, resultText)

        animateViews(resultCard)
    }

    private fun setupWindowAnimations() {
        window.requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.enterTransition = MaterialFadeThrough()
        window.exitTransition = MaterialFadeThrough()
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
    }

    private fun animateViews(vararg views: View) {
        views.forEach { view ->
            view.alpha = 0f
            view.translationY = 100f
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 500
                start()
            }
            ObjectAnimator.ofFloat(view, "translationY", 100f, 0f).apply {
                duration = 500
                interpolator = AnticipateOvershootInterpolator()
                start()
            }
        }
    }

    override fun onBackPressed() {
        val fadeOut = ObjectAnimator.ofFloat(window.decorView, "alpha", 1f, 0f)
        fadeOut.duration = 300
        fadeOut.addListener(onEnd = {
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        })
        fadeOut.start()
    }
}