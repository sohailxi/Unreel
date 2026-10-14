package app.unreel.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.unreel.data.db.UnreelDatabase

actual fun createTestDriver(): SqlDriver {
    return NativeSqliteDriver(UnreelDatabase.Schema, "test_unreel.db")
}
