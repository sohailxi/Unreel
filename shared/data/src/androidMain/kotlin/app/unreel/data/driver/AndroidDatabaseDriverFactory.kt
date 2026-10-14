package app.unreel.data.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.unreel.data.db.UnreelDatabase

/**
 * Android implementation of [DatabaseDriverFactory] using [AndroidSqliteDriver].
 */
class AndroidDatabaseDriverFactory(
    private val context: Context,
    private val dbName: String = "unreel.db",
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = UnreelDatabase.Schema,
            context = context,
            name = dbName,
        )
    }
}
