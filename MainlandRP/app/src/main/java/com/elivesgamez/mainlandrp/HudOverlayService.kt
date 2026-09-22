package com.elivesgamez.mainlandrp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast

class HudOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val glyphs = linkedMapOf(
        CommandBridge.Action.INVENTORY to "🎒",
        CommandBridge.Action.PHONE to "📱",
        CommandBridge.Action.FACTION_DUTY to "👮",
        CommandBridge.Action.JOB to "💼",
        CommandBridge.Action.VEHICLE to "🚗",
        CommandBridge.Action.LOCK to "🔒",
        CommandBridge.Action.ENGINE to "⚙️",
        CommandBridge.Action.HOUSE to "🏠",
        CommandBridge.Action.MAP to "🗺️",
        CommandBridge.Action.CHARACTER to "👤",
        CommandBridge.Action.CHAT to "💬",
        CommandBridge.Action.VOICE to "🎙️",
        CommandBridge.Action.DO_ACTION to "⚡",
        CommandBridge.Action.SETTINGS to "🔧"
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addOverlay()

        CommandBridge.listener = object : CommandBridge.Listener {
            override fun onCommand(action: CommandBridge.Action) {
                // TODO Phase 4: real dispatch. For now, prove the round trip works
                // and make the DIRECT/DIALOG/NONE distinction visible while testing.
                val msg = when (action.kind) {
                    CommandBridge.Kind.DIRECT -> "${action.label} → sends \"${action.command}\""
                    CommandBridge.Kind.DIALOG -> "${action.label} → opens \"${action.command}\" (you tap the menu)"
                    CommandBridge.Kind.NONE -> "${action.label} → no server command (see notes)"
                }
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "mainland_rp_hud"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mainland RP HUD", NotificationManager.IMPORTANCE_MIN)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val stopIntent = Intent(this, HudOverlayService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, channelId)
            .setContentTitle("Mainland RP HUD active")
            .setContentText("Tap to manage")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .addAction(0, "Stop HUD", stopPending)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun addOverlay() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_hud, null)
        overlayView = view

        val toggle = view.findViewById<TextView>(R.id.btnHudToggle)
        val expanded = view.findViewById<View>(R.id.hudExpanded)
        val close = view.findViewById<TextView>(R.id.btnHudClose)
        val grid = view.findViewById<GridLayout>(R.id.hudGrid)

        glyphs.forEach { (action, glyph) ->
            val btn = TextView(this).apply {
                text = glyph
                textSize = 20f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.shape_hud_button)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(48); height = dp(48)
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                }
                setOnClickListener { CommandBridge.send(action) }
            }
            grid.addView(btn)
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 40
        params.y = 200

        windowManager.addView(view, params)

        // Drag-to-move on the collapsed bubble; tap toggles the panel.
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                expanded.visibility = if (expanded.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                return true
            }
        })

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f

        toggle.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(view, params)
                }
            }
            true
        }

        close.setOnClickListener { expanded.visibility = View.GONE }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        CommandBridge.listener = null
    }

    companion object {
        const val ACTION_STOP = "com.elivesgamez.mainlandrp.ACTION_STOP_HUD"
    }
}
