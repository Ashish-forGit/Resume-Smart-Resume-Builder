package com.demo.resumebuilder

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.platform.MaterialFadeThrough

@Suppress("DEPRECATION")
class Choice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choice)

        window.decorView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))

        window.enterTransition = MaterialFadeThrough()
        window.exitTransition = MaterialFadeThrough()

        val templateCard = findViewById<MaterialCardView>(R.id.templateCard)
        val analyzeCard = findViewById<MaterialCardView>(R.id.analyzeCard)
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)

        animateViewsSequentially(templateCard, analyzeCard, logoutButton)

        templateCard.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            startActivity(
                                Intent(this, Build::class.java).apply {
                                    putExtra("mode", "template")
                                }
                            )
                        }
                }
        }

        analyzeCard.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            startActivity(Intent(this, Analyze::class.java))
                        }
                }
        }

        logoutButton.setOnClickListener {
            val fadeOut = ObjectAnimator.ofFloat(View(this), "alpha", 1f, 0f)
            fadeOut.duration = 300
            fadeOut.doOnEnd {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            fadeOut.start()
        }
    }

    private fun animateViewsSequentially(vararg views: View) {
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val fadeOut = ObjectAnimator.ofFloat(window.decorView, "alpha", 1f, 0f)
        fadeOut.duration = 300
        fadeOut.doOnEnd {
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        fadeOut.start()
    }
}