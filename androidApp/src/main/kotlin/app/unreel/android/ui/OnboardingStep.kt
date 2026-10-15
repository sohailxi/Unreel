package app.unreel.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Onboarding steps guiding the user through privacy and required Android permissions.
 */
enum class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
) {
    WELCOME(
        title = "Know Your Scroll",
        subtitle = "Reclaim your time from mindless feeds",
        description = "Unreel counts short-video scrolls on Instagram, YouTube, and TikTok to help you build digital balance. Your privacy is 100% protected: no personal data, messages, usernames, or video content are ever read or collected.",
        icon = Icons.Rounded.PlayCircle,
    ),
    ACCESSIBILITY(
        title = "Exact Reel Counting",
        subtitle = "Precise scroll detection on Android",
        description = "Unreel uses Android's Accessibility service to detect reel swipe transitions in real-time. It only monitors package transitions and scroll events—never screen text, accounts, or personal information.",
        icon = Icons.Rounded.Speed,
    ),
    SHIELDING(
        title = "Shields & Survival",
        subtitle = "Overlay alerts and background protection",
        description = "To enforce your scroll limits, Unreel displays a mindful Overlay shield over short-video feeds and sends timely notifications. Configure battery optimization and autostart to prevent background termination.",
        icon = Icons.Rounded.Shield,
    ),
}

/**
 * Progresses to the next step, bounded by [totalSteps].
 */
fun calculateNextStep(currentIndex: Int, totalSteps: Int): Int =
    (currentIndex + 1).coerceAtMost(totalSteps - 1)

/**
 * Moves to the previous step, bounded at 0.
 */
fun calculatePreviousStep(currentIndex: Int): Int =
    (currentIndex - 1).coerceAtLeast(0)

/**
 * Indicates whether the current index is the final step.
 */
fun isLastStep(currentIndex: Int, totalSteps: Int): Boolean =
    currentIndex >= totalSteps - 1
