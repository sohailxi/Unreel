package app.unreel.android.platform

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import co.touchlab.kermit.Logger

/**
 * Interface managing the Android floating Dynamic Island overlay.
 * Renders an interactive pill at the top of the screen displaying real-time exact reel counts.
 */
interface DynamicIslandOverlayController {
    fun showOrUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?,
    )
    fun hide()
    fun isVisible(): Boolean
    fun isExpanded(): Boolean
    fun setExpanded(expanded: Boolean)
}

/**
 * Android implementation of [DynamicIslandOverlayController] utilizing [WindowManager]
 * with [WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY] anchored at the top center.
 */
class AndroidDynamicIslandOverlayController(
    private val context: Context?,
    private val logger: Logger = Logger.withTag("DynamicIslandOverlay"),
    private val mainExecutor: ((() -> Unit) -> Unit)? = null,
) : DynamicIslandOverlayController {

    private fun runOnMain(block: () -> Unit) {
        if (mainExecutor != null) {
            mainExecutor.invoke(block)
        } else if (context != null) {
            try {
                Handler(Looper.getMainLooper()).post(block)
            } catch (t: Throwable) {
                block()
            }
        } else {
            block()
        }
    }

    private var windowManager: WindowManager? = null
    private var rootContainer: FrameLayout? = null

    // Subviews
    private var compactLayout: LinearLayout? = null
    private var expandedLayout: LinearLayout? = null
    private var compactReelsText: TextView? = null
    private var compactDurationText: TextView? = null
    private var expandedSessionText: TextView? = null
    private var expandedTodayText: TextView? = null
    private var expandedProgressBar: ProgressBar? = null
    private var expandedStatusText: TextView? = null

    private var isExpandedState: Boolean = false
    private var currentSessionReels: Int = 0
    private var currentTodayReels: Int = 0
    private var currentTodayDurationMs: Long = 0L
    private var currentLimitProgress: Float? = null

    override fun isVisible(): Boolean = rootContainer != null

    override fun isExpanded(): Boolean = isExpandedState

    override fun setExpanded(expanded: Boolean) {
        if (rootContainer == null || isExpandedState == expanded) return
        isExpandedState = expanded
        runOnMain {
            updateViewHierarchy()
        }
    }

    override fun showOrUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?,
    ) {
        currentSessionReels = sessionReels
        currentTodayReels = todayReels
        currentTodayDurationMs = todayDurationMs
        currentLimitProgress = limitProgress

        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
            logger.w { "SYSTEM_ALERT_WINDOW permission not granted; cannot display dynamic island overlay" }
            return
        }

        runOnMain {
            if (rootContainer == null) {
                createAndAttachOverlay(ctx)
            } else {
                updateContent()
            }
        }
    }

    override fun hide() {
        runOnMain {
            val root = rootContainer ?: return@runOnMain
            try {
                windowManager?.removeView(root)
                logger.i { "Dynamic Island overlay removed" }
            } catch (t: Throwable) {
                logger.e(t) { "Error removing Dynamic Island overlay" }
            } finally {
                rootContainer = null
                compactLayout = null
                expandedLayout = null
                compactReelsText = null
                compactDurationText = null
                expandedSessionText = null
                expandedTodayText = null
                expandedProgressBar = null
                expandedStatusText = null
                isExpandedState = false
            }
        }
    }

    private fun createAndAttachOverlay(ctx: Context) {
        try {
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
            windowManager = wm

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val density = ctx.resources.displayMetrics.density
            val topMarginPx = (14 * density).toInt()

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = topMarginPx
            }

            val root = FrameLayout(ctx).apply {
                layoutTransition = LayoutTransition().apply {
                    enableTransitionType(LayoutTransition.CHANGING)
                }
            }

            // --- 1. Compact Pill Layout ---
            val compact = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = GradientDrawable().apply {
                    setColor(0xFF09090B.toInt())
                    cornerRadius = 24 * density
                    setStroke((1.5f * density).toInt(), 0xFF27272A.toInt())
                }
                setPadding((14 * density).toInt(), (6 * density).toInt(), (14 * density).toInt(), (6 * density).toInt())

                val iconView = TextView(ctx).apply {
                    text = "🎬"
                    textSize = 13f
                    setPadding(0, 0, (6 * density).toInt(), 0)
                }
                addView(iconView)

                compactReelsText = TextView(ctx).apply {
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
                addView(compactReelsText)

                val dotSeparator = TextView(ctx).apply {
                    text = " • "
                    textSize = 12f
                    setTextColor(0xFF71717A.toInt())
                }
                addView(dotSeparator)

                compactDurationText = TextView(ctx).apply {
                    textSize = 12f
                    setTextColor(0xFFA1A1AA.toInt())
                }
                addView(compactDurationText)

                setOnClickListener {
                    setExpanded(!isExpandedState)
                }
            }
            compactLayout = compact
            root.addView(compact)

            // --- 2. Expanded Card Layout ---
            val expanded = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                background = GradientDrawable().apply {
                    setColor(0xFF0F0F14.toInt())
                    cornerRadius = 24 * density
                    setStroke((2f * density).toInt(), 0xFF6366F1.toInt())
                }
                setPadding((18 * density).toInt(), (14 * density).toInt(), (18 * density).toInt(), (14 * density).toInt())
                visibility = View.GONE

                // Header Row
                val headerRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(TextView(ctx).apply {
                        text = "🎬 Unreel Live"
                        textSize = 14f
                        setTextColor(Color.WHITE)
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    })

                    val spacer = View(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                    }
                    addView(spacer)

                    addView(TextView(ctx).apply {
                        text = "EXACT"
                        textSize = 10f
                        setTextColor(0xFF6366F1.toInt())
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        background = GradientDrawable().apply {
                            setColor(0x226366F1.toInt())
                            cornerRadius = 10 * density
                        }
                        setPadding((6 * density).toInt(), (2 * density).toInt(), (6 * density).toInt(), (2 * density).toInt())
                    })
                }
                addView(headerRow)

                // Session Reels Big Count
                expandedSessionText = TextView(ctx).apply {
                    textSize = 22f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(0, (10 * density).toInt(), 0, (2 * density).toInt())
                    gravity = Gravity.CENTER
                }
                addView(expandedSessionText)

                // Today's summary
                expandedTodayText = TextView(ctx).apply {
                    textSize = 12f
                    setTextColor(0xFFA1A1AA.toInt())
                    setPadding(0, 0, 0, (10 * density).toInt())
                    gravity = Gravity.CENTER
                }
                addView(expandedTodayText)

                // Progress Bar
                expandedProgressBar = ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    layoutParams = LinearLayout.LayoutParams(
                        (220 * density).toInt(),
                        (6 * density).toInt(),
                    )
                    progressDrawable = GradientDrawable().apply {
                        setColor(0xFF6366F1.toInt())
                        cornerRadius = 3 * density
                    }
                    background = GradientDrawable().apply {
                        setColor(0xFF27272A.toInt())
                        cornerRadius = 3 * density
                    }
                }
                addView(expandedProgressBar)

                // Status text / collapse prompt
                expandedStatusText = TextView(ctx).apply {
                    textSize = 11f
                    setTextColor(0xFF71717A.toInt())
                    setPadding(0, (8 * density).toInt(), 0, 0)
                    text = "Tap to collapse"
                    gravity = Gravity.CENTER
                }
                addView(expandedStatusText)

                setOnClickListener {
                    setExpanded(false)
                }
            }
            expandedLayout = expanded
            root.addView(expanded)

            wm.addView(root, params)
            rootContainer = root
            updateContent()
            logger.i { "Dynamic Island overlay attached successfully" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to show Dynamic Island overlay" }
        }
    }

    private fun updateViewHierarchy() {
        val compact = compactLayout ?: return
        val expanded = expandedLayout ?: return

        if (isExpandedState) {
            compact.visibility = View.GONE
            expanded.visibility = View.VISIBLE
        } else {
            expanded.visibility = View.GONE
            compact.visibility = View.VISIBLE
        }
        updateContent()
    }

    private fun updateContent() {
        val durationMins = currentTodayDurationMs / 60_000L
        val durationStr = if (durationMins > 0) "${durationMins}m" else "<1m"

        compactReelsText?.text = "$currentSessionReels reels"
        compactDurationText?.text = durationStr

        expandedSessionText?.text = "$currentSessionReels Reels This Session"
        expandedTodayText?.text = "Today: $currentTodayReels reels • $durationStr"

        val progress = currentLimitProgress
        if (progress != null) {
            expandedProgressBar?.visibility = View.VISIBLE
            expandedProgressBar?.progress = (progress * 100).toInt().coerceIn(0, 100)
            expandedStatusText?.text = when {
                progress >= 1.0f -> "⚠️ Limit reached (Tap to collapse)"
                progress >= 0.85f -> "⚡ Nearing limit (Tap to collapse)"
                else -> "Tap to collapse"
            }
        } else {
            expandedProgressBar?.visibility = View.GONE
            expandedStatusText?.text = "Tap to collapse"
        }
    }
}
