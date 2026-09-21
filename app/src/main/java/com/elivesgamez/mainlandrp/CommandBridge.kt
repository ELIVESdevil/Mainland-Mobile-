package com.elivesgamez.mainlandrp

/**
 * Verified against MAINLAND_RP_ENGLISH_TRANSLATION_V8_FIXED3_FOUNDER_MAKELEADER.pwn
 * (106,856 lines, ZCMD-style CMD: handlers, 417 commands total). Findings below
 * are real, not guessed — each is annotated with what was actually found.
 *
 * IMPORTANT STRUCTURAL FINDING: this gamemode leans heavily on ShowPlayerDialog
 * menus (numeric dialog IDs), not flat one-shot commands. A typed command often
 * just OPENS a dialog; picking the actual option happens by tapping a list item
 * inside that dialog. SA-MP dialogs are drawn by the game client's own renderer,
 * not as native Android views — so once a dialog is open, our overlay cannot see
 * or tap its contents (there is no Accessibility node tree for game-drawn UI).
 * That means: for DIALOG-class actions below, the HUD button can reliably send
 * the opening command, but the player still taps the resulting in-game menu
 * themselves. That's still a real win (no typing), just not a single full
 * automation.
 *
 * DELIVERY MECHANISM — still unbuilt, blocked on knowing which SA-MP mobile
 * client APK your players use:
 *  1) If it exposes an Intent/API to inject chat text -> call it directly.
 *  2) Otherwise, HudOverlayService types the command into its native chat
 *     input via AccessibilityService (this only works if that client's chat
 *     box is a real Android EditText, not drawn in-engine — unconfirmed).
 *
 * sendCommand() still only logs + toasts for now so the HUD is testable
 * end-to-end before a delivery mechanism is wired in.
 */
object CommandBridge {

    enum class Kind {
        DIRECT,     // single command, fully completes the action, no follow-up tap
        DIALOG,     // command opens a menu; player must tap a listitem in-game
        NONE        // no chat command exists for this at all (see note)
    }

    enum class Action(val label: String, val command: String, val kind: Kind, val note: String) {
        INVENTORY(
            "Inventory", "/invent", Kind.DIRECT,
            "cmd:invent -> calls ShowInventory() directly. Confirmed real, single tap."
        ),
        PHONE(
            "Phone", "", Kind.NONE,
            "CMD:phone exists but only replies 'open your phone through the inventory' — " +
                "there's no standalone phone command. Route this button to /invent instead " +
                "for now, or tell me which inventory item to look for."
        ),
        FACTION_DUTY(
            "Faction Duty", "/f", Kind.DIALOG,
            "CMD:f opens dialog 581 (\"Change clothes\" / \"Take ammo\"), which for clothes " +
                "leads to dialog 7551 where listitem 0 actually flips TeamDuty. Two nested " +
                "menus — button can only open the first one."
        ),
        JOB(
            "Job", "", Kind.NONE,
            "No CMD: to start a job. InJob[] is driven by entering job vehicles / world " +
                "checkpoints, not chat. This button can't trigger it via commands at all."
        ),
        VEHICLE(
            "Vehicle", "", Kind.NONE,
            "CMD:veh is an admin-only spawn command (requires pAdmin >= 8), not a player " +
                "vehicle menu — my first guess was wrong. /alarm exists but needs a numeric " +
                "car slot id as a parameter, so it isn't a clean single-tap button either."
        ),
        LOCK(
            "Lock", "/lock", Kind.DIRECT,
            "CMD:lock finds your nearest owned vehicle and toggles it. Confirmed real, " +
                "single tap, no parameters needed."
        ),
        ENGINE(
            "Engine", "/engine", Kind.DIRECT,
            "CMD:engine toggles the engine of the vehicle you're currently in. Confirmed " +
                "real, single tap."
        ),
        HOUSE(
            "House", "/house", Kind.DIALOG,
            "CMD:house opens dialog 181 or 174 (open/close, garage, sell, etc.) depending " +
                "on ownership and location. Opens the menu; player taps the option in-game."
        ),
        MAP(
            "Map", "", Kind.NONE,
            "No matching server command found. This is very likely a built-in feature of " +
                "whichever GTA:SA mobile client you use (world map / GPS key), not something " +
                "the server or our HUD controls."
        ),
        CHARACTER(
            "Character", "/stats", Kind.DIRECT,
            "CMD:stats calls ShowStats() — an info dialog, no further input needed. " +
                "Confirmed real."
        ),
        CHAT("Chat", "", Kind.NONE, "Opens the game's own chat input; no command to send."),
        VOICE("Voice", "", Kind.NONE, "Push-to-talk; not a chat command."),
        DO_ACTION(
            "Action", "/do", Kind.DIRECT,
            "CMD:do confirmed — standard roleplay action command. Needs the player to type " +
                "the action text after it, so this button should open chat pre-filled with " +
                "\"/do \" rather than send it outright."
        ),
        SETTINGS("Settings", "", Kind.NONE, "App-local screen, not a game command.")
    }

    interface Listener {
        fun onCommand(action: Action)
    }

    var listener: Listener? = null

    fun send(action: Action) {
        // TODO Phase 4: real dispatch once the game client + its chat/dialog
        // rendering approach are confirmed.
        listener?.onCommand(action)
    }
}
