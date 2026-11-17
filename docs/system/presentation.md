# Presentation

Screen state is unidirectional. The screen sends an intent. The state holder calls domain types and publishes one immutable state. `domain` and `data` do not know about this.

Android and desktop share `:sharedUI`, so they share one state holder. iOS is SwiftUI and has its own.

## Dependencies

Presentation depends on `domain` only.

- A screen, ViewModel, component, UI model, or navigation entry imports `<name>.domain`, `<name>.domain.entity`, `<name>.domain.usecase`, `<name>.domain.error`, and `:core` (`Outcome`, `Reason`). It imports no other feature types.
- It never imports `<name>.data`, `data.local`, or `data.mapper`, and never sees a Room entity, DAO, HTTP body, or repository implementation.
- It does not use a feature's Koin module (`accountsModule`) and does not build a repository or use case from `data`. The ViewModel receives the repository interface or use case through its constructor. It may create a use case from the repository interface it was given.
- Koin builds the ViewModels. `presentationModule` in `com.prodan.taktfinances.di` (`:sharedUI`) declares one `viewModel { }` per screen, and the screen takes it with `koinViewModel()`. Route arguments go in with `parametersOf(...)`. A new screen adds its ViewModel to `presentationModule`.
- iOS follows the same rule. Swift screen models use the domain types exported by `SharedLogic` and nothing else.

## Android and desktop — MVI ViewModel

One `ViewModel` per screen, in `com.prodan.taktfinances.<name>` inside `:sharedUI`. It extends AndroidX Lifecycle `ViewModel` (the Compose Multiplatform artifact already in the stack). Desktop uses that same class. Do not add a desktop-only state holder, and do not add an MVI library.

The ViewModel does not import Compose. The composable does not hold screen state.

### Package layout

Each feature has one package in `:sharedUI`, `com.prodan.taktfinances.<name>`:

```
sharedUI/src/commonMain/kotlin/com/prodan/taktfinances/<name>/
  components/
    AccountCard.kt           # composables used by this feature's screens
  models/
    AccountCardModel.kt      # UI models: display-ready values the State holds
  navigation/
    AddAccountNavigation.kt  # NavKey route and EntryProviderScope entry for each screen
  AddAccountScreen.kt        # screen composable, collects state and effects, plus private AddAccountContent
  AddAccountState.kt         # data class AddAccountState
  AddAccountIntent.kt        # sealed interface AddAccountIntent
  AddAccountViewModel.kt     # class AddAccountViewModel
  AddAccountEffect.kt        # sealed interface AddAccountEffect
```

- The five screen files, `<Screen>Screen.kt`, `<Screen>State.kt`, `<Screen>Intent.kt`, `<Screen>ViewModel.kt` and `<Screen>Effect.kt`, sit directly in the feature package. Each file holds one type with the same name. A feature with two screens has two sets of five.
- `<Screen>Effect.kt` exists even when the screen has one effect. A screen with no one-off events leaves it out, along with the `effects` flow.
- The screen's content lives in `<Screen>Screen.kt`. The stateless `<Screen>Content`, which lays out the whole screen from `State` and callbacks, is a `private` composable in that file, below `<Screen>Screen`. It is not a separate file and does not go in `components/`.
- `components/` holds this feature's composables below screen level: cards, fields, rows, icons. They take values and callbacks and do not take a ViewModel. A composable two features use goes in the root `com.prodan.taktfinances` package or `theme`, not in a feature's `components`.
- `models/` holds UI models: display-ready types that `State` holds when a domain type doesn't fit the screen as is, such as a formatted amount or a label with its color. Mapping from domain to a UI model is a `toUi()` extension function in `models/`. Domain types can go on `State` directly when they need no change. No Compose types (`Color`, `Painter`) go in a model. Keep a plain value such as an ARGB `Long`, and the composable converts it.
- `navigation/` holds the feature's routes and one `EntryProviderScope<NavKey>.<screen>Entry(...)` extension per screen that calls `entry<Route> { <Screen>Screen(...) }`. A route is a `@Serializable` `NavKey`. Its parameters are the screen's navigation callbacks and layout flags. The shell (`App`) owns the `NavBackStack`, calls these entries inside `NavDisplay`'s `entryProvider`, registers every route in its saved-state serializers, and connects one feature's callback to another feature's route. A feature does not navigate to another feature's route by itself.
- The ViewModel imports from `models` but not from `components` or `navigation`.

Three types:

- `State` — one immutable `data class`. Loading, content, and error live here.
- `Intent` — one `sealed interface` of user and lifecycle events. The only public entry is `onIntent`.
- `Effect` — one-off work the state must not remember: navigation and short messages. A `Channel.BUFFERED` exposed as a `Flow`. The screen collects it with `CollectEffects`. The ViewModel does not hold the back stack.

```kotlin
class AccountViewModel(
    private val balances: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = Channel<AccountEffect>(Channel.BUFFERED)
    val effects: Flow<AccountEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.Opened -> refresh()
            is AccountIntent.AmountChanged -> _state.update { it.copy(amount = intent.value) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val outcome = balances.balance(_state.value.id)) {
                is Outcome.Ok -> _state.update { it.copy(loading = false, balance = outcome.value) }
                is Outcome.Failed -> _state.update { it.copy(loading = false, error = outcome.reason) }
            }
        }
    }
}
```

The screen collects `state` with `collectAsStateWithLifecycle` and sends intents from callbacks. `remember` is only for gesture and focus state that no other screen needs. A recoverable failure stays on `State`. See [errors.md](errors.md).

### Collecting effects

The shell owns the back stack and connects features. Each feature declares its routes and entries in its `navigation/` package. `App` calls those entries and passes a navigation callback into each one, such as `onNavigateToHome`. The screen does not take the back stack.

The ViewModel sends an effect from `onIntent`. The screen collects `effects` with `CollectEffects` in `com.prodan.taktfinances`. Collection runs while the lifecycle is at least `STARTED` and stops on `ON_STOP`. `Channel.BUFFERED` keeps an effect that is sent while collection is stopped. The next start delivers it. The button sends the intent. The collector calls the navigation callback.

```kotlin
private val _effects = Channel<LandingEffect>(Channel.BUFFERED)
val effects: Flow<LandingEffect> = _effects.receiveAsFlow()

fun onIntent(intent: LandingIntent) {
    when (intent) {
        LandingIntent.ContinueTapped -> viewModelScope.launch {
            _effects.send(LandingEffect.OpenHome)
        }
    }
}
```

```kotlin
@Composable
fun LandingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LandingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            LandingEffect.OpenHome -> onNavigateToHome()
        }
    }
    LandingContent(
        state = state,
        onContinue = { viewModel.onIntent(LandingIntent.ContinueTapped) },
    )
}
```

Do not collect `effects` with a `LaunchedEffect` that ignores the lifecycle. Do not call the navigation callback from the button when the ViewModel is the one that decides to navigate.

## iOS — Observable screen model

iOS 18 uses SwiftUI Observation. One `@Observable` model per screen, in `iosApp/iosApp/<Name>/`. The view calls `send`. The model calls `SharedLogic` domain types. It is not a Lifecycle `ViewModel`, an `ObservableObject`, or a Kotlin class.

Navigation stays on the `NavigationStack`. Put a destination in state when the stack should show it. Do not add The Composable Architecture or another navigation library.

```swift
@Observable
final class AccountModel {
    private(set) var state = AccountState()
    private let balances: AccountRepository

    init(balances: AccountRepository) {
        self.balances = balances
    }

    func send(_ intent: AccountIntent) async {
        switch intent {
        case .opened:
            state.loading = true
            let outcome = try await balances.balance(id: state.id)
            guard let found = outcome as? OutcomeOk, let amount = found.value as? Money else {
                state.loading = false
                state.unavailable = true
                return
            }
            state.loading = false
            state.balance = amount
        case let .amountChanged(value):
            state.amount = value
        }
    }
}
```

The view owns the model with `@State` and reads `model.state`. It does not write state except by calling `send`. `SharedLogic` exports `Outcome.Ok` as `OutcomeOk`, and `value` as `Any`, so the model casts it.

## What stays out

UI state is not shared through the `SharedLogic` framework. Both sides map the same domain values into their own screen state. A change to a label or a progress flag does not change a use case.
