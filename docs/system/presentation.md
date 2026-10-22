# Presentation

Screen state is unidirectional. The screen sends an intent. The state holder calls domain types and publishes one immutable state. `domain` and `data` do not know about this.

Android and desktop share `:sharedUI`, so they share one state holder. iOS is SwiftUI and has its own.

## Android and desktop — MVI ViewModel

One `ViewModel` per screen, in `com.prodan.taktfinances.<name>` inside `:sharedUI`. It extends AndroidX Lifecycle `ViewModel` (the Compose Multiplatform artifact already in the stack). Desktop uses that same class. Do not add a desktop-only state holder, and do not add an MVI library.

The ViewModel does not import Compose. The composable does not hold screen state.

Three types:

- `State` — one immutable `data class`. Loading, content, and error live here.
- `Intent` — one `sealed interface` of user and lifecycle events. The only public entry is `onIntent`.
- `Effect` — one-off work the state must not remember: navigation and short messages. A `Channel` exposed as a `Flow`. The composable collects it once. The ViewModel does not hold a `NavController`.

```kotlin
class AccountViewModel(
    private val balances: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = Channel<AccountEffect>()
    val effects: Flow<AccountEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.Opened -> refresh()
            is AccountIntent.AmountChanged -> _state.update { it.copy(amount = intent.value) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            _state.update { it.copy(loading = false, balance = balances.balance(it.id)) }
        }
    }
}
```

The screen collects `state` with `collectAsStateWithLifecycle` and sends intents from callbacks. `remember` is only for gesture and focus state that no other screen needs.

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

    func send(_ intent: AccountIntent) {
        switch intent {
        case .opened:
            state.loading = true
        case let .amountChanged(value):
            state.amount = value
        }
    }
}
```

The view owns the model with `@State` and reads `model.state`. It does not write state except by calling `send`.

## What stays out

UI state is not shared through the `SharedLogic` framework. Both sides map the same domain values into their own screen state. A change to a label or a progress flag does not change a use case.
