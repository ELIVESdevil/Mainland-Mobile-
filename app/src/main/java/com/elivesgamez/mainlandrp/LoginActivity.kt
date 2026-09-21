package com.elivesgamez.mainlandrp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bg = findViewById<View>(R.id.bgLogin)
        val shimmer = findViewById<View>(R.id.shimmerLogin)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnPrimary = findViewById<Button>(R.id.btnPrimary)
        val tvToggleMode = findViewById<TextView>(R.id.tvToggleMode)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvError = findViewById<TextView>(R.id.tvError)

        Animations.kenBurns(bg)
        bg.post { Animations.shimmerSweep(shimmer, resources.displayMetrics.widthPixels) }
        Animations.pulse(btnPrimary)

        fun applyMode() {
            etEmail.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
            etConfirmPassword.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
            btnPrimary.text = if (isRegisterMode) getString(R.string.btn_register) else getString(R.string.btn_login)
            tvToggleMode.text = if (isRegisterMode) getString(R.string.toggle_to_login) else getString(R.string.toggle_to_register)
            tvTitle.text = if (isRegisterMode) getString(R.string.btn_register) else getString(R.string.login_title)
            tvError.visibility = View.GONE
        }

        tvToggleMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            applyMode()
        }

        btnPrimary.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString()

            if (isRegisterMode) {
                val confirm = etConfirmPassword.text.toString()
                if (pass != confirm) {
                    tvError.text = "Passwords do not match"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
            }

            val result = if (isRegisterMode) AuthClient.register(user, pass) else AuthClient.login(user, pass)

            if (result.success) {
                getSharedPreferences("mainland_rp", MODE_PRIVATE).edit()
                    .putString("username", user)
                    .putString("password", pass)
                    .apply()
                startActivity(Intent(this, ServerSelectActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            } else {
                tvError.text = result.message
                tvError.visibility = View.VISIBLE
            }
        }

        applyMode()
    }
}
