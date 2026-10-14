package app.unreel.data.di

import app.cash.sqldelight.db.SqlDriver
import app.unreel.data.createTestDriver
import app.unreel.data.db.UnreelDatabase
import app.unreel.data.driver.DatabaseDriverFactory
import app.unreel.data.snapshot.SnapshotPublisher
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DataModuleTest {

    private var koinApp: KoinApplication? = null

    @BeforeTest
    fun setUp() {
        stopKoin()
        val testModule = module {
            single<DatabaseDriverFactory> {
                object : DatabaseDriverFactory {
                    override fun createDriver(): SqlDriver = createTestDriver()
                }
            }
            single<Settings> { MapSettings() }
        }

        koinApp = startKoin {
            modules(testModule, dataModule)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        koinApp = null
    }

    @Test
    fun resolvesAllDataLayerDependencies() {
        val koin = koinApp!!.koin

        val database: UnreelDatabase = koin.get()
        assertNotNull(database)

        val usageRepo: UsageRepository = koin.get()
        assertNotNull(usageRepo)

        val ruleRepo: RuleRepository = koin.get()
        assertNotNull(ruleRepo)

        val settingsRepo: SettingsRepository = koin.get()
        assertNotNull(settingsRepo)

        val publisher: SnapshotPublisher = koin.get()
        assertNotNull(publisher)
    }
}
