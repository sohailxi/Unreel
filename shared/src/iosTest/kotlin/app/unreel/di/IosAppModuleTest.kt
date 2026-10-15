package app.unreel.di

import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.usecase.IngestReelEventUseCase
import app.unreel.domain.usecase.IngestUsageTickUseCase
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.context.stopKoin

class IosAppModuleTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testInitKoinIosAndResolveViewModels() {
        startKoinIos()

        val helper = KoinHelper()
        val homeVm = helper.getHomeViewModel()
        assertNotNull(homeVm)
        assertNotNull(homeVm.state.value)

        val statsVm = helper.getStatsViewModel()
        assertNotNull(statsVm)
        assertNotNull(statsVm.state.value)

        val rulesVm = helper.getRulesViewModel()
        assertNotNull(rulesVm)
        assertNotNull(rulesVm.state.value)
    }

    @Test
    fun testResolveIosPlatformPortsAndUseCases() {
        startKoinIos()

        val koin = KoinPlatformTools.defaultContext().get()
        val publisher: LiveSurfacePublisher = koin.get()
        assertNotNull(publisher)

        val enforcer: Enforcer = koin.get()
        assertNotNull(enforcer)

        val notificationPort: NotificationPort = koin.get()
        assertNotNull(notificationPort)

        val ingestReelUseCase: IngestReelEventUseCase = koin.get()
        assertNotNull(ingestReelUseCase)

        val ingestTickUseCase: IngestUsageTickUseCase = koin.get()
        assertNotNull(ingestTickUseCase)
    }
}

