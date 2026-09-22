package com.elivesgamez.mainlandrp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY_MS = 3200L

    // Measured directly from the art: where the drawn loading bar sits as a
    // fraction of the ORIGINAL image (see bg_splash.jpg), not the screen.
    private val BAR_LEFT_FRAC = 0.353f
    private val BAR_RIGHT_FRAC = 0.648f
    private val BAR_TOP_FRAC = 0.857f
    private val BAR_BOTTOM_FRAC = 0.883f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val bg = findViewById<ImageView>(R.id.bgSplash)
        val shimmer = findViewById<android.view.View>(R.id.shimmerSplash)
        val progressContainer = findViewById<FrameLayout>(R.id.progressContainer)
        val progressTrack = findViewById<android.view.View>(R.id.progressTrack)
        val progressFill = findViewById<android.view.View>(R.id.progressFill)

        Animations.kenBurns(bg)

        bg.post {
            positionProgressBar(bg, progressContainer)

            Animations.shimmerSweep(shimmer, resources.displayMetrics.widthPixels)
            progressTrack.post {
                Animations.growWidth(progressFill, progressTrack.width, SPLASH_DELAY_MS - 300)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, SPLASH_DELAY_MS)
    }

    /**
     * With centerCrop filling a wide phone screen, the art is scaled up until
     * it covers the full width, then cropped top/bottom. This works out
     * exactly where the drawn loading bar ends up on THIS screen so our real
     * progress bar sits right on top of it, on any aspect ratio.
     */
    private fun positionProgressBar(bg: ImageView, container: FrameLayout) {
        val drawable = bg.drawable ?: return
        val viewW = bg.width.toFloat()
        val viewH = bg.height.toFloat()
        val imgW = drawable.intrinsicWidth.toFloat()
        val imgH = drawable.intrinsicHeight.toFloat()
        if (viewW <= 0 || viewH <= 0 || imgW <= 0 || imgH <= 0) return

        // centerCrop scales by whichever dimension needs to grow MORE.
        val scale = maxOf(viewW / imgW, viewH / imgH)
        val scaledH = imgH * scale
        val cropTopPx = (scaledH - viewH) / 2f
        val cropTopFrac = cropTopPx / scaledH
        val cropBottomFrac = 1f - cropTopFrac

        fun toScreenFrac(imageFrac: Float): Float =
            ((imageFrac - cropTopFrac) / (cropBottomFrac - cropTopFrac)).coerceIn(0f, 1f)

        val screenTop = toScreenFrac(BAR_TOP_FRAC)
        val screenBottom = toScreenFrac(BAR_BOTTOM_FRAC)

        // Horizontal is untouched by centerCrop here since width is the
        // fitting dimension (no side cropping happens).
        val left = (viewW * BAR_LEFT_FRAC).toInt()
        val right = (viewW * BAR_RIGHT_FRAC).toInt()
        val top = (viewH * screenTop).toInt()
        val bottom = (viewH * screenBottom).toInt()

        val params = container.layoutParams as ViewGroup.MarginLayoutParams
        params.width = right - left
        params.height = (bottom - top).coerceAtLeast(4)
        params.leftMargin = left
        params.topMargin = top
        container.layoutParams = params
    }
}
