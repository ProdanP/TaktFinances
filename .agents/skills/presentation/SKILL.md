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

- [ ] One immutable `State`, one `Intent` (or Swift `Intent`) type, one `onIntent` or `send` entry
- [ ] State holder calls domain types and does not import Compose, SwiftUI, SQLDelight, or Ktor
- [ ] Compose screen has no screen `remember` state; SwiftUI writes state only through `send`
- [ ] Navigation and other one-off events are not stored as sticky flags on Android and desktop. The Compose screen collects them with `CollectEffects` while the lifecycle is at least `STARTED`. iOS navigation is `NavigationStack` state
- [ ] Screen state is not exported from `:sharedLogic`
- [ ] A recoverable failure is `Outcome` on `State`. Swift casts `Ok.value` from `Any`

Do not add Orbit, MVIKotlin, Circuit, or The Composable Architecture. They are not in the stack.
