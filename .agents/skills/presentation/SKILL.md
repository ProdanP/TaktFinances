---
name: presentation
description: Apply the TaktFinances screen state pattern. Use when adding or changing a screen, ViewModel, composable state, SwiftUI view, or screen model.
---

# Presentation

Read [docs/system/presentation.md](../../../docs/system/presentation.md) and follow it.

## Which state holder

Android or desktop screen → one MVI `ViewModel` in `:sharedUI`, package `com.prodan.taktfinances.<name>`. Both platforms use that class.

iOS screen → one `@Observable` model in `iosApp/iosApp/<Name>/`. It calls domain types from `SharedLogic`. It is not a `ViewModel`.

## Checklist

- [ ] Compose feature package `com.prodan.taktfinances.<name>` has `components/`, `models/` and `navigation/`, plus `<Screen>Screen.kt`, `<Screen>State.kt`, `<Screen>Intent.kt`, `<Screen>ViewModel.kt` and `<Screen>Effect.kt` at its root. Each file holds one type.
- [ ] The screen's content (the stateless `<Screen>Content` that lays out the screen from `State`) is a `private` composable in `<Screen>Screen.kt`, not a separate file in `components/`.
- [ ] Below-screen composables (cards, fields, rows) are in `components/`. They take values and callbacks, not a ViewModel.
- [ ] Display-ready UI models are in `models/`, mapped from domain with `toUi()`. They hold no Compose types.
- [ ] Routes and `NavGraphBuilder.<screen>Screen(...)` entries are in `navigation/`. `App` owns the `NavController` and connects features.

- [ ] One immutable `State`, one `Intent` (or Swift `Intent`) type, one `onIntent` or `send` entry
- [ ] Presentation depends on `domain` only: no import from the feature's `data`, `data.local` or `data.mapper`, and no use of its Koin module. Repositories and use cases come in through the ViewModel constructor. The ViewModel is declared in `presentationModule` and the screen gets it with `koinViewModel()`
- [ ] State holder calls domain types and does not import Compose, SwiftUI, Room, or Ktor
- [ ] Compose screen has no screen `remember` state; SwiftUI writes state only through `send`
- [ ] Navigation and other one-off events are not stored as sticky flags on Android and desktop. The Compose screen collects them with `CollectEffects` while the lifecycle is at least `STARTED`. iOS navigation is `NavigationStack` state
- [ ] Screen state is not exported from `:sharedLogic`
- [ ] A recoverable failure is `Outcome` on `State`. Swift casts `Ok.value` from `Any`

Do not add Orbit, MVIKotlin, Circuit, or The Composable Architecture. They are not in the stack.
