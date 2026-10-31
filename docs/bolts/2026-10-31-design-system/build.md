# Build

## Notes

Applied the Material Theme Builder export to the current greeting shell. Android and desktop share six Material 3 color schemes in `:sharedUI`, selected by appearance and contrast. Desktop stays on the standard scheme. Android reads `UiModeManager.getContrast()` from API 34 and updates when the contrast setting changes. iOS uses the same export as semantic colors, with the high scheme under increased contrast, the system font, and a text button. `AccentColor` is the light and dark primary, including the high-contrast appearances.

The Figma card, text field, and button mocks stay illustrations. The shell is the only screen this bolt changes.

https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=6-2171
