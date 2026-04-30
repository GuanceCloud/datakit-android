# replay 0.1.4-beta01
1. Removed redundant Kotlin dependencies

---
# replay 0.1.4-alpha03
1. Improved public API documentation for Session Replay configuration, privacy controls, custom mapper extensions, resource upload callbacks, and mapper helper types to make IDE integration and custom extension development clearer.
2. Optimized the internal UTF-8 encoding implementation used when writing replay records, keeping the output format unchanged while avoiding a Kotlin charset dependency in Java code.

---
# replay 0.1.4-alpha02
1. fix: correct dependency version constraint with ft-sdk and replay

---
# replay 0.1.4-alpha01
1. No Changed

---
# replay 0.1.3
1. Added WebView Session Replay support, including DCloud WebView support and full-snapshot keyframe support for WebView containers.
2. Added RUM context association for Session Replay, allowing WebView containers to be linked with the context of loaded HTML content.
3. Added error sampling support and remote configuration support for Session Replay.
4. Added image resource upload support and improved metadata handling, including userAgent, appVersion, and forceFullSnapshot.
5. Optimized frame generation and consumption, frame completion during data sharding, delayed writes for WebView replay data, invisible WebView handling, and disk write limits to improve replay stability and performance.
6. Fixed issues involving WebView replay data loss, blank replay caused by writes occurring too early, synchronization failures on lower OkHttp versions, recording failures caused by bitmap recycling, null value handling for rumLinkKey, and replay timeout issues when many images are present.

---
# replay 0.1.3-beta02
1. Same as 0.1.3-beta01

---
# replay 0.1.3-beta01
1. Same as 0.1.3-alpha15

---
# replay 0.1.3-alpha15
1. Fix an issue where replay recording times out when there are many images due to 
   local icon cache misses.

---
# replay 0.1.3-alpha14
1. ft-sdk:>= 1.7.0-alpha40
2. Added image resource upload logic for Session Replay.

---
# replay 0.1.3-alpha13
1. ft-sdk:>= 1.7.0-alpha39
2. Added Session Replay support to remote configuration.

---
# replay 0.1.3-alpha12
1. ft-sdk:>= 1.7.0-alpha37
2. Fixed the issue where enabling ErrorSampleRate caused key playback data to be lost in WebView
3. Optimized the logic for handling replay data when the WebView is not visible

---
# replay 0.1.3-alpha11
1. ft-sdk:>= 1.7.0-alpha36
2. Optimized the linkView frame completion process during Replay data sharding.

---
# replay 0.1.3-alpha10
1. Added meta data for forceFullSnapshot.
2. Optimized frame generation and consumption logic.
3. Fixed the issue where recording failed due to bitmap recycling.
4. ft-sdk:>= 1.7.0-alpha35

---
# replay 0.1.3-alpha09
1. ft-sdk:>= 1.7.0-alpha34
2. Added null value check for rumLinkKey.

---
# replay 0.1.3-alpha08
1. ft-sdk:>= 1.7.0-alpha34
2. Added full snapshot keyframe Session Replay support for the WebView container

---
# replay 0.1.3-alpha07
1. ft-sdk:>= 1.7.0-alpha33
2. Increased the disk write limit for Session Replay
3. Added RUM Session Replay context association feature,
   and associated the WebView container with the context of the loaded HTML.

---
# replay 0.1.3-alpha06
1. Delayed write for WebView Session Replay data.
2. ft-sdk:>= 1.7.0-alpha29
---
# replay 0.1.3-alpha05
1. Fixed the issue where obtaining write too early caused WebView Session Replay to be blank, and added log output.
2. ft-sdk:>= 1.7.0-alpha28
---
# replay 0.1.3-alpha04
1. resolve the css issues in uni App
2. ft-sdk:>= 1.7.0-alpha27
---
# replay 0.1.3-alpha03
1. DCloud WebView Support
2. ft-sdk:>= 1.7.0-alpha26

---
# replay 0.1.3-alpha02
1. Added `CustomExtensionSupport`for quickly customizing and adding ExtensionSupport settings.
2. WebView session replay supports Tencent X5

---
# replay 0.1.3-alpha01
1. Webview Session Replay support ft-sdk:>= 1.7.0-alpha23

---
# replay 0.1.2-alpha02
1. Adapt to react native replay feature

---
# replay 0.1.2-alpha01
1. Added `FTSessionReplayConfig.setSessionReplayOnErrorSampleRate` support for error sampling, in case it is not sampled by `setSamplingRate`, it can sample data from session replay 1 minute ago when an error occurs

---
# replay 0.1.1-alpha01
1. Added TextAndInputPrivacy, TouchPrivacy settings
2. Supported for covering TextAndInputPrivacy and TouchPrivacy settings for separate pages

---
# replay 0.1.0-alpha09
1. UserAgent appended Session Replay SDK information
2. Supported for depending on Dns and Proxy, unifying data entry dependency ft-sdk internal settings to create thread pool and send data

---
# replay 0.1.0-alpha08
1. Modified X-Pkg-Id placement

---
# replay 0.1.0-alpha07
1. X-Pkg-Id Session Replay data synchronization tracking adaptation

---
# replay 0.1.0-alpha05
1. Fixed React Native View crash problem

---
# replay 0.1.0-alpha04
1. Supported React Native

---
# replay 0.1.0-alpha03
1. Line protocol packet version display
2. dialog background acquisition optimization

---
# replay 0.1.0-alpha02
1. Supported session replay recording function
2. Supported sampling rate, privacy configuration, and Material component additional configuration settings
3. Supported datakit, dataway transmission

# session-replay 0.1.1-alpha01
1. Support for CardView and Chip components

---
# session-replay 0.1.0-alpha04
1. Debug low version support

---
# session-replay 0.1.0-alpha03
1. Line protocol packet version display

---
# session-replay 0.1.0-alpha01
1. Support for session replay Material components
