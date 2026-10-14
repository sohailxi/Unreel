package app.unreel.data.driver

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform factory interface for producing a [SqlDriver] for [app.unreel.data.db.UnreelDatabase].
 */
interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
