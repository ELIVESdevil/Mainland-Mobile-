package com.elivesgamez.mainlandrp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Phase 1/3 scope: collects a display identity locally. Real character
 * data (skin, stats, inventory) lives on your server and is created via
 * whatever dialog/command your gamemode already uses — this screen is a
 * friendlier front door to that, not a replacement for it. Once we know
 * how your gamemode wants this info (a dialog response, a command with
 * params, etc.) this will submit for real instead of just storing locally.
 */
class CharacterCreationActivity : AppCompatActivity() {

    private var isMale = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_creation)

        val bg = findViewById<View>(R.id.bgCharCreate)
        val etFirst = findViewById<EditText>(R.id.etFirstName)
        val etLast = findViewById<EditText>(R.id.etLastName)
        val btnMale = findViewById<Button>(R.id.btnGenderMale)
        val btnFemale = findViewById<Button>(R.id.btnGenderFemale)
        val btnEnter = findViewById<Button>(R.id.btnEnterWorld)
        val tvError = findViewById<TextView>(R.id.tvCcError)

        Animations.kenBurns(bg)
        Animations.pulse(btnEnter)

        fun applyGenderStyle() {
            btnMale.setBackgroundResource(if (isMale) R.drawable.shape_button_gold else R.drawable.shape_input_gold)
            btnFemale.setBackgroundResource(if (isMale) R.drawable.shape_input_gold else R.drawable.shape_button_gold)
        }
        applyGenderStyle()

        btnMale.setOnClickListener { isMale = true; applyGenderStyle() }
        btnFemale.setOnClickListener { isMale = false; applyGenderStyle() }

        btnEnter.setOnClickListener {
            val first = etFirst.text.toString().trim()
            val last = etLast.text.toString().trim()

            if (first.isBlank() || last.isBlank()) {
                tvError.text = "Enter a first and last name"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!first[0].isUpperCase() || !last[0].isUpperCase()) {
                tvError.text = "SA-MP names are usually Capitalized_Like_This"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            getSharedPreferences("mainland_rp", MODE_PRIVATE).edit()
                .putString("char_first_name", first)
                .putString("char_last_name", last)
                .putString("char_gender", if (isMale) "male" else "female")
                .apply()

            startActivity(Intent(this, PlayHubActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
