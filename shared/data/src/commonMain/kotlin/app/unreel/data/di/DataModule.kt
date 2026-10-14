package app.unreel.data.di

import app.unreel.data.db.UnreelDatabase
import app.unreel.data.driver.DatabaseDriverFactory
import app.unreel.data.repository.SettingsRepositoryImpl
import app.unreel.data.repository.SqlDelightRuleRepository
import app.unreel.data.repository.SqlDelightUsageRepository
import app.unreel.data.snapshot.SettingsSnapshotSink
import app.unreel.data.snapshot.SnapshotPublisher
import app.unreel.data.snapshot.SnapshotPublisherImpl
import app.unreel.data.snapshot.SnapshotSink
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module {
    single<UnreelDatabase> {
        val driverFactory: DatabaseDriverFactory = get()
        UnreelDatabase(driverFactory.createDriver())
    }

    single<UsageRepository> {
        SqlDelightUsageRepository(db = get())
    }

    single<RuleRepository> {
        SqlDelightRuleRepository(db = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(settings = get())
    }

    single<SnapshotSink> {
        SettingsSnapshotSink(settings = get())
    }

    single<SnapshotPublisher> {
        SnapshotPublisherImpl(sink = get())
    }
}
