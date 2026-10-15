package app.unreel.di

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
}
