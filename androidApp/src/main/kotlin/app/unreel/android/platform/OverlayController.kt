package app.unreel.android.platform

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import co.touchlab.kermit.Logger

/**
 * Interface representing system overlay interactions on Android.
 */
interface OverlayController {
    fun showBlockingOverlay(
        reason: String,
        passesRemaining: Int = 0,
        onBypass: (() -> Unit)? = null,
    )
    fun hideOverlay()
    fun isOverlayVisible(): Boolean
}

/**
 * Android implementation of [OverlayController] utilizing [WindowManager]
 * with TYPE_APPLICATION_OVERLAY for reels-only blocking (ADR-004 / AGENTS.md).
 */
class AndroidOverlayController(
    private val context: Context?,
    private val logger: Logger = Logger.withTag("AndroidOverlayController"),
) : OverlayController {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun isOverlayVisible(): Boolean = overlayView != null

    override fun showBlockingOverlay(
        reason: String,
        passesRemaining: Int,
        onBypass: (() -> Unit)?,
    ) {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
            logger.w { "SYSTEM_ALERT_WINDOW permission not granted; cannot display blocking overlay" }
            return
        }

        if (overlayView != null) {
            hideOverlay()
        }

        try {
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
            windowManager = wm

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.CENTER
            }

            val layout = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(0xEE121212.toInt())
                setPadding(64, 64, 64, 64)

                val card = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(0xFF1E1E2E.toInt())
                        cornerRadius = 48f
                        setStroke(2, 0xFF6366F1.toInt())
                    }
                    setPadding(56, 56, 56, 56)

                    addView(TextView(ctx).apply {
                        text = "🛡️"
                        textSize = 44f
                        gravity = Gravity.CENTER
                    })

                    addView(TextView(ctx).apply {
                        text = "Scroll Limit Reached"
                        textSize = 22f
                        setTextColor(0xFFFFFFFF.toInt())
                        setPadding(0, 24, 0, 12)
                        gravity = Gravity.CENTER
                    })

                    addView(TextView(ctx).apply {
                        text = reason
                        textSize = 15f
                        setTextColor(0xFFA1A1AA.toInt())
                        setPadding(0, 0, 0, 36)
                        gravity = Gravity.CENTER
                    })

                    val homeButton = Button(ctx).apply {
                        text = "Step Away & Return Home"
                        setTextColor(Color.WHITE)
                        background = GradientDrawable().apply {
                            setColor(0xFF6366F1.toInt())
                            cornerRadius = 24f
                        }
                        setOnClickListener {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            ctx.startActivity(homeIntent)
                            hideOverlay()
                        }
                    }
                    addView(homeButton)

                    if (onBypass != null && passesRemaining > 0) {
                        val bypassButton = Button(ctx).apply {
                            text = "Use Emergency Pass ($passesRemaining left)"
                            setTextColor(0xFFA1A1AA.toInt())
                            background = null
                            setPadding(0, 20, 0, 0)
                            setOnClickListener {
                                onBypass.invoke()
                                hideOverlay()
                            }
                        }
                        addView(bypassButton)
                    }
                }

                addView(card)
            }

            wm.addView(layout, params)
            overlayView = layout
            logger.i { "Blocking overlay attached successfully" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to show blocking overlay" }
        }
    }

    override fun hideOverlay() {
        val view = overlayView ?: return
        val wm = windowManager ?: (context?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
        try {
            wm?.removeView(view)
            logger.i { "Blocking overlay removed" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to remove blocking overlay" }
        } finally {
            overlayView = null
        }
    }
}
