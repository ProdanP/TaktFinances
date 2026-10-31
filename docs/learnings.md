# Learnings

Lessons worth keeping from closed bolts. Add a line only when something surprised you.

`Settings.Secure.CONTRAST_LEVEL` is hidden from the public Android SDK. Observe the setting by the name `contrast_level`, and read the level from `UiModeManager.getContrast()`.
