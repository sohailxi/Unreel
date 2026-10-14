package app.unreel.model

import kotlinx.serialization.Serializable

/**
 * Platform identifier for multi-platform data separation.
 */
@Serializable
enum class Platform {
    ANDROID,
    IOS,
}
