# Intent

## Problem

Landing's Continue button still does nothing. After welcome, the person needs a Home shell with three destinations—Dashboard, My Finances, and Profile—so the app can grow real content later. Those screens are empty mocks for now. Navigation chrome must adapt: a bottom bar on phones, a NavigationRail or drawer on tablets, foldables, and large windows—not a phone-only bottom bar stretched to every size.

## Done when

- Tapping Continue on Landing opens Home on Android, desktop, and iOS.
- Home shows three destinations: Dashboard, My Finances, and Profile; each is an empty mock screen.
- On compact width, destination switching uses a bottom navigation bar.
- On medium and expanded width (tablets, foldables, large windows), destination switching uses a NavigationRail or a drawer instead of a bottom bar.
- Home belongs to `:feature:home`. Compose lives in `:sharedUI` at `com.prodan.taktfinances.home`. SwiftUI lives in `iosApp/iosApp/Home/`.
