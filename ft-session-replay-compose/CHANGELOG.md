# replay-compose 0.1.3
1. Added Jetpack Compose Session Replay support for `ComposeView` and `AndroidComposeView`.
2. Added replay support for common Compose components, including text, text fields, buttons, images, sliders, switches, checkboxes, radio buttons, tabs, and container nodes.
3. Added Session Replay privacy overrides for Compose through `Modifier`, including hide, image privacy, text and input privacy, and touch privacy.
4. Compose replay is based on semantics mapping and is not pixel-perfect. Brush-based backgrounds such as linear or radial gradients are not replayed with their original visual effect.
