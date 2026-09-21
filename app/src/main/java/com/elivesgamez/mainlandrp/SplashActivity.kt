package com.elivesgamez.mainlandrp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY_MS = 3200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val bg = findViewById<View>(R.id.bgSplash)
        val shimmer = findViewById<View>(R.id.shimmerSplash)
        val progressTrack = findViewById<View>(R.id.progressTrack)
        val progressFill = findViewById<View>(R.id.progressFill)

        Animations.kenBurns(bg)

        progressTrack.post {
            Animations.shimmerSweep(shimmer, resources.displayMetrics.widthPixels)
            Animations.growWidth(progressFill, progressTrack.width, SPLASH_DELAY_MS - 300)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, SPLASH_DELAY_MS)
    }
}
