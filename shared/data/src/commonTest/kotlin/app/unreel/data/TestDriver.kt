package app.unreel.data

import app.cash.sqldelight.db.SqlDriver
import app.unreel.data.db.UnreelDatabase

expect fun createTestDriver(): SqlDriver

fun createTestDatabase(): UnreelDatabase {
    val driver = createTestDriver()
    return UnreelDatabase(driver)
}
