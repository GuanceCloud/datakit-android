# Cold-start launch-type acceptance

This optional, debug-only fixture uses its own package (`com.ft.fixture.launch`),
local SDK storage, disabled automatic upload/remote configuration, and a loopback
intake address. It does not load the sample application's credentials or clear
the sample application's data.

Build with the checked-in wrapper:

```sh
./gradlew -PftLaunchFixture :ft-launch-fixture:assembleDebug --max-workers=1 --no-parallel
```

Run against an existing device, using its actual serial:

```sh
python3 ft-launch-fixture/verify.py --serial emulator-5554 --output docs/launch-type-acceptance
```

The runner installs the fixture, resets only this fixture's data between scenarios,
and stops its process on completion. It reads the actual stored cold-start Action
line protocol and writes JSON evidence for each scenario:

- Foreground-only startup, including classification remaining frozen after completion.
- Headless background startup: `background`, no Activity, no first draw, and no automatic cold Action.
- An explicit **test-only** background completion to verify `background` serialization and freezing.
- Background startup, repeated Application hook, then foreground and an automatic first draw:
  `foreground_and_background`, with the original Application timestamp preserved.
- Foreground startup with drawing held by an `OnPreDrawListener`, Home/background,
  return to the same Activity, then release: automatic `foreground_and_background`.

A headless background process has no Activity first-draw endpoint. The explicit
background endpoint is deliberately identified as `explicit_test_completion` in
the evidence; it is not an automatically recorded first frame and does not establish
normal Activity-stage timing semantics for a headless launch.

The exported control provider and package-private SDK bridge are fixture-only.
Do not include this module in a production build or publish its APK.
