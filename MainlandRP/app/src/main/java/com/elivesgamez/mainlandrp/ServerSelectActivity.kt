package com.elivesgamez.mainlandrp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ServerSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_select)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvPlayers = findViewById<TextView>(R.id.tvPlayers)
        val btnPlay = findViewById<Button>(R.id.btnPlay)

        refreshStatus(tvStatus, tvPlayers)
        Animations.pulse(btnPlay)

        btnPlay.setOnClickListener {
            val prefs = getSharedPreferences("mainland_rp", MODE_PRIVATE)
            val hasCharacter = prefs.contains("char_first_name")
            val target = if (hasCharacter) PlayHubActivity::class.java else CharacterCreationActivity::class.java
            startActivity(Intent(this, target))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun refreshStatus(tvStatus: TextView, tvPlayers: TextView) {
        lifecycleScope.launch {
            val info = SampQuery.fetchInfo(ServerConfig.HOST, ServerConfig.PORT)
            if (info.online) {
                tvStatus.text = "Online — ${info.gamemode}"
                tvStatus.setTextColor(getColor(R.color.online_green))
                tvPlayers.text = "${info.players} / ${info.maxPlayers} players"
            } else {
                tvStatus.text = "Offline or unreachable"
                tvStatus.setTextColor(getColor(R.color.offline_gray))
                tvPlayers.text = ""
            }
        }
    }
}
