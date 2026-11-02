# Build

## Notes

Added `:feature:onboarding` with `LandingWelcome` and `landingWelcome()`. `:sharedLogic` depends on it with `api` and exports it from the `SharedLogic` framework. `:sharedUI` depends on it and opens `LandingScreen` from `App` instead of the greeting. Android and desktop share `LandingViewModel` and `LandingScreen`: theme `background`, Welcome at the top, 240 illustration, message under it, Continue pinned to the bottom with 24 point insets, and a 420-wide column on wide windows. Continue does nothing. iOS uses `LandingModel` and `LandingView` in `iosApp/iosApp/Onboarding/`, with `ContentView` showing that screen. The illustration is in Compose resource `landing` and iOS asset `Landing`. Figma export was rate-limited, so the raster matches the coins-and-notebook art on `#F9F9FF`.

https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=18-41
