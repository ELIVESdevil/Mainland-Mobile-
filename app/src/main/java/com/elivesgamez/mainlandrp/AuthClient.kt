package com.elivesgamez.mainlandrp

/**
 * CONFIRMED against the real gamemode: registration/login is dialog-based.
 * OnPlayerConnect shows dialog ID 1 (DIALOG_STYLE_INPUT) for the password
 * prompt, driven by IsPlayerLogged[playerid]. There is no separate web/API
 * account system — this screen's job for Phase 2 is to store the typed
 * credentials and auto-answer that exact dialog the moment the game client
 * connects and shows it (requires knowing how that client exposes/renders
 * SA-MP dialogs — same open question as the HUD's dialog-class actions).
 *
 * For now this always succeeds locally so the Phase 1 UI flow is testable
 * end-to-end without a live connection.
 */
object AuthClient {

    data class Result(val success: Boolean, val message: String? = null)

    fun login(username: String, password: String): Result {
        if (username.isBlank() || password.isBlank()) {
            return Result(false, "Enter username and password")
        }
        // TODO Phase 2: real call or store-for-dialog-autofill
        return Result(true)
    }

    fun register(username: String, password: String): Result {
        if (username.length < 3) {
            return Result(false, "Username too short")
        }
        if (password.length < 6) {
            return Result(false, "Password must be at least 6 characters")
        }
        // TODO Phase 2: real call or store-for-dialog-autofill
        return Result(true)
    }
}
