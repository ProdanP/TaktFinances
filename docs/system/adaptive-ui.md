# Adaptive UI

Every screen targets phones, tablets, and foldables. Desktop windows reuse the same Compose layouts, so treat a large window like a tablet. Layout adapts in the view. The ViewModel or Swift model does not branch on form factor.

- Do not assume a portrait phone. Prefer the available width and height over fixed full-bleed phone dimensions.
- On a wide window or regular-width size class, keep content readable: center a bounded column or use a multi-pane layout when the design calls for one. Do not stretch a single phone column edge to edge.
- Foldables change aspect ratio and usable area. Avoid layouts that break when height is short, width is large, or the window is not a standard phone rectangle.
- Safe areas, system bars, and display cutouts stay respected on every size. Pin chrome to insets, not to hard-coded offsets that only fit one device.
- Shared Compose screens in `:sharedUI` must look correct on Android phones, tablets, foldables, and desktop. SwiftUI screens must look correct on iPhone and iPad. Do not ship a phone-only layout and leave large screens for later.

```kotlin
BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val contentWidth = maxWidth.coerceAtMost(420.dp)
    Column(
        modifier = Modifier
            .width(contentWidth)
            .align(Alignment.TopCenter)
            .fillMaxHeight(),
    ) {
        // screen content
    }
}
```
