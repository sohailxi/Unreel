package app.unreel.data.driver

import kotlin.test.Test
import kotlin.test.assertNotNull

class IosDatabaseDriverFactoryTest {
    @Test
    fun testCreateDriverSuccessfully() {
        val factory = IosDatabaseDriverFactory(dbName = "test_unreel.db")
        val driver = factory.createDriver()
        assertNotNull(driver)
        driver.close()
    }
}
