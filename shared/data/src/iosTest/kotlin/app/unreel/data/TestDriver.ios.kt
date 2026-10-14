package app.unreel.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import app.unreel.data.db.UnreelDatabase

actual fun createTestDriver(): SqlDriver {
    return inMemoryDriver(UnreelDatabase.Schema)
}
