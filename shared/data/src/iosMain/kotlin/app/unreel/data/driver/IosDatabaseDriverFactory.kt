package app.unreel.data.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import app.unreel.data.db.UnreelDatabase
import co.touchlab.sqliter.DatabaseConfiguration
import platform.Foundation.NSFileManager

/**
 * iOS implementation of [DatabaseDriverFactory] using [NativeSqliteDriver].
 * Targets the App Group container ("group.app.unreel.shared") in WAL mode
 * per project architectural specifications.
 */
class IosDatabaseDriverFactory(
    private val appGroupId: String = APP_GROUP_ID,
    private val dbName: String = "unreel.db",
) : DatabaseDriverFactory {

    override fun createDriver(): SqlDriver {
        val containerUrl = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)
        val basePath = containerUrl?.path

        val configuration = DatabaseConfiguration(
            name = dbName,
            version = UnreelDatabase.Schema.version.toInt(),
            create = { connection ->
                wrapConnection(connection) { UnreelDatabase.Schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) {
                    UnreelDatabase.Schema.migrate(it, oldVersion.toLong(), newVersion.toLong())
                }
            },
            extendedConfig = DatabaseConfiguration.Extended(
                basePath = basePath,
            ),
        )

        val driver = NativeSqliteDriver(configuration)
        driver.execute(null, "PRAGMA journal_mode = WAL;", 0)
        driver.execute(null, "PRAGMA synchronous = NORMAL;", 0)
        return driver
    }

    companion object {
        const val APP_GROUP_ID = "group.app.unreel.shared"
    }
}
