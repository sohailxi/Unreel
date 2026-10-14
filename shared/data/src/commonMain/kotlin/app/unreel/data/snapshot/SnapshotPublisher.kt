package app.unreel.data.snapshot

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Port for writing and reading raw snapshot JSON from platform storage.
 */
interface SnapshotSink {
    suspend fun writeSnapshotJson(jsonString: String)
    suspend fun readSnapshotJson(): String?
}

/**
 * [SnapshotSink] implementation backed by [multiplatform-settings]
 * (e.g., App Group NSUserDefaults on iOS or SharedPreferences on Android).
 */
class SettingsSnapshotSink(
    private val settings: Settings,
    private val key: String = KEY_USAGE_SNAPSHOT,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SnapshotSink {

    override suspend fun writeSnapshotJson(jsonString: String) = withContext(ioDispatcher) {
        settings.putString(key, jsonString)
    }

    override suspend fun readSnapshotJson(): String? = withContext(ioDispatcher) {
        settings.getStringOrNull(key)
    }

    companion object {
        const val KEY_USAGE_SNAPSHOT = "usage_snapshot_v1"
    }
}

/**
 * Publisher that writes versioned [UsageSnapshot] to shared storage for surface consumers.
 */
interface SnapshotPublisher {
    suspend fun publishSnapshot(snapshot: UsageSnapshot)
    suspend fun readLatestSnapshot(): UsageSnapshot?
}

class SnapshotPublisherImpl(
    private val sink: SnapshotSink,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
    private val logger: Logger = Logger.withTag("SnapshotPublisher"),
) : SnapshotPublisher {

    override suspend fun publishSnapshot(snapshot: UsageSnapshot) {
        try {
            val jsonString = json.encodeToString(snapshot)
            sink.writeSnapshotJson(jsonString)
        } catch (e: Exception) {
            logger.e(e) { "Failed to publish usage snapshot" }
        }
    }

    override suspend fun readLatestSnapshot(): UsageSnapshot? {
        val rawJson = sink.readSnapshotJson() ?: return null
        return try {
            json.decodeFromString<UsageSnapshot>(rawJson)
        } catch (e: Exception) {
            logger.w(e) { "Failed to deserialize snapshot JSON: $rawJson" }
            null
        }
    }
}
