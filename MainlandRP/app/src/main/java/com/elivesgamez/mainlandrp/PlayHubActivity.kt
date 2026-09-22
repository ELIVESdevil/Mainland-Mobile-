package com.elivesgamez.mainlandrp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlayHubActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnGrantOverlay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_hub)

        val bg = findViewById<View>(R.id.bgPlayHub)
        val tvCharName = findViewById<TextView>(R.id.tvCharName)
        val btnLaunch = findViewById<Button>(R.id.btnLaunch)
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay)

        Animations.kenBurns(bg)
        Animations.pulse(btnLaunch)

        val prefs = getSharedPreferences("mainland_rp", MODE_PRIVATE)
        val first = prefs.getString("char_first_name", null)
        val last = prefs.getString("char_last_name", null)
        tvCharName.text = if (first != null && last != null) "${first}_$last" else "No character yet"

        btnGrantOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        btnLaunch.setOnClickListener {
            launchGameAndHud()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        val granted = Settings.canDrawOverlays(this)
        tvPermissionStatus.text = if (granted) {
            "HUD overlay permission granted ✓"
        } else {
            getString(R.string.hud_permission_needed)
        }
        btnGrantOverlay.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun launchGameAndHud() {
        // 1) Start our floating HUD, if permission is granted.
        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, HudOverlayService::class.java))
        } else {
            Toast.makeText(this, "Grant overlay permission first for the HUD buttons", Toast.LENGTH_LONG).show()
        }

        // 2) Hand off to the installed GTA:SA + SA-MP mobile client.
        // TODO Phase 2: fill ServerConfig.GAME_CLIENT_PACKAGE once known.
        if (ServerConfig.GAME_CLIENT_PACKAGE.isBlank()) {
            Toast.makeText(
                this,
                "Game client package not set yet — HUD is running standalone for now",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(ServerConfig.GAME_CLIENT_PACKAGE)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "SA-MP client not installed", Toast.LENGTH_LONG).show()
        }
    }
}
