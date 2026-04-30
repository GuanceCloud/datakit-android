# replay-compose 0.1.4-beta01
1. No Changed

---
# replay-compose 0.1.4-alpha03
1. Improved KDoc for Compose Session Replay privacy `Modifier` APIs, Compose extension support, text truncation modes, and image wireframe helpers to make Kotlin and Compose integration clearer in IDEs.

---
# replay-compose 0.1.4-alpha02
1. No Changed

---
# replay-compose 0.1.4-alpha01
1. Fixed a crash when recording `AndroidView` interop nodes with Session Replay core implementations that do not expose `MappingContext.getInteropViewCallback()`.
2. Added consumer ProGuard rules to keep the `MappingContext` AndroidView interop callback accessors used by Compose replay.

---
# replay-compose 0.1.3
1. Added Jetpack Compose Session Replay support for `ComposeView` and `AndroidComposeView`.
2. Added replay support for common Compose components, including text, text fields, buttons, images, sliders, switches, checkboxes, radio buttons, tabs, and container nodes.
3. Added Session Replay privacy overrides for Compose through `Modifier`, including hide, image privacy, text and input privacy, and touch privacy.
4. Compose replay is based on semantics mapping and is not pixel-perfect. Brush-based backgrounds such as linear or radial gradients are not replayed with their original visual effect.
