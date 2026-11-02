# Intent

## Problem

The app still opens on the template greeting: a button that reveals a platform line and a logo. There is no onboarding entry. The first screen should welcome the person, show a money-themed illustration, and offer Continue, before any later onboarding step exists.

## Done when

- Android, desktop, and iOS open on the Landing screen instead of the template greeting.
- The screen shows a short welcome written for this bolt, the money-themed illustration drafted in the project Figma file, and a Continue button at the bottom.
- Tapping Continue changes nothing: no navigation and no saved onboarding state.
- Landing belongs to onboarding. Domain lives in `:feature:onboarding`. Compose lives in `:sharedUI` at `com.prodan.taktfinances.onboarding`. SwiftUI lives in `iosApp/iosApp/Onboarding/`.
