package com.elivesgamez.mainlandrp

/**
 * EDIT THESE for your real Mainland RP server.
 */
object ServerConfig {
    const val HOST = "188.127.241.74"
    const val PORT = 3836

    // Verified via aapt against the actual installed APK - do not change
    // without re-verifying, the naming is easy to guess wrong (there are
    // several similarly-named Alyn-based builds with different package ids).
    const val GAME_CLIENT_PACKAGE = "ro.alyn_sampmobile.game"
}
