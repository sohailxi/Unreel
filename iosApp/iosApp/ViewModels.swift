import SwiftUI
import Shared

@MainActor
final class HomeObservable: ObservableObject {
    let viewModel: HomeViewModel
    @Published var state: HomeState?
    private var unsubscribe: (() -> Void)?

    init(helper: KoinHelper = KoinHelper()) {
        let vm = helper.getHomeViewModel()
        self.viewModel = vm
        self.state = vm.state.value as? HomeState
        self.unsubscribe = helper.observeHomeState(viewModel: vm) { [weak self] newState in
            Task { @MainActor in
                self?.state = newState
            }
        }
    }

    deinit {
        unsubscribe?()
    }

    func refresh() {
        viewModel.onIntent(intent: HomeIntentRefresh())
    }

    func requestBypass() {
        viewModel.onIntent(intent: HomeIntentRequestBypass())
    }

    func selectApp(appId: String?) {
        viewModel.onIntent(intent: HomeIntentSelectApp(appId: appId))
    }
}

@MainActor
final class StatsObservable: ObservableObject {
    let viewModel: StatsViewModel
    @Published var state: StatsState?
    private var unsubscribe: (() -> Void)?

    init(helper: KoinHelper = KoinHelper()) {
        let vm = helper.getStatsViewModel()
        self.viewModel = vm
        self.state = vm.state.value as? StatsState
        self.unsubscribe = helper.observeStatsState(viewModel: vm) { [weak self] newState in
            Task { @MainActor in
                self?.state = newState
            }
        }
    }

    deinit {
        unsubscribe?()
    }

    func refresh() {
        viewModel.onIntent(intent: StatsIntentRefresh())
    }

    func selectTimeRange(days: Int32) {
        viewModel.onIntent(intent: StatsIntentSelectTimeRange(days: days))
    }
}

@MainActor
final class RulesObservable: ObservableObject {
    let viewModel: RulesViewModel
    @Published var state: RulesState?
    private var unsubscribe: (() -> Void)?

    init(helper: KoinHelper = KoinHelper()) {
        let vm = helper.getRulesViewModel()
        self.viewModel = vm
        self.state = vm.state.value as? RulesState
        self.unsubscribe = helper.observeRulesState(viewModel: vm) { [weak self] newState in
            Task { @MainActor in
                self?.state = newState
            }
        }
    }

    deinit {
        unsubscribe?()
    }

    func toggleRule(id: String, enabled: Bool) {
        viewModel.onIntent(intent: RulesIntentToggleRule(ruleId: id, enabled: enabled))
    }

    func updateFocusGoalReels(reels: Int32) {
        viewModel.onIntent(intent: RulesIntentUpdateFocusGoalReels(reels: max(10, reels)))
    }

    func updateFocusGoalMinutes(minutes: Int32) {
        viewModel.onIntent(intent: RulesIntentUpdateFocusGoalMinutes(minutes: max(5, minutes)))
    }
}
