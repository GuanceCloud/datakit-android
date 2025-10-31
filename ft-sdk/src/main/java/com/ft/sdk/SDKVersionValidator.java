package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.VersionUtils;

import java.lang.reflect.Field;

/**
 * SDK version matching validator
 * Used to validate version compatibility between ft-sdk and ft-session-replay during SDK initialization
 */
public class SDKVersionValidator {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "SDKVersionValidator";

    /**
     * Validates version compatibility between two SDKs
     *
     * @return true if version mismatch detected, false if versions are compatible or session-replay is not integrated
     */
    public static boolean validateSDKVersions() {
        try {
            // Get static fields from ft-sdk BuildConfig
            Class<?> sdkBuildConfigClass = com.ft.sdk.BuildConfig.class;
            Field ftSdkVersionField = sdkBuildConfigClass.getField("FT_SDK_VERSION");
            Field replayMiniSupportField = sdkBuildConfigClass.getField("REPLAY_MINI_SUPPORT");

            String ftSdkVersion = (String) ftSdkVersionField.get(null);
            String replayMiniSupport = (String) replayMiniSupportField.get(null);

            // Try to get ft-session-replay BuildConfig (if integrated)
            String replayVersion = null;
            String miniAgentSupport = null;

            try {
                Class<?> replayBuildConfigClass = Class.forName("com.ft.sdk.sessionreplay.BuildConfig");
                Field versionNameField = replayBuildConfigClass.getField("VERSION_NAME");
                Field miniAgentSupportField = replayBuildConfigClass.getField("MINI_AGENT_SUPPORT");

                replayVersion = (String) versionNameField.get(null);
                miniAgentSupport = (String) miniAgentSupportField.get(null);
            } catch (ClassNotFoundException e) {
                // session-replay not integrated, skip validation
                LogUtils.d(TAG, "ft-session-replay not integrated, skip version validation");
                return false;
            } catch (Exception e) {
                LogUtils.e(TAG, "Failed to read ft-session-replay BuildConfig: " + LogUtils.getStackTraceString(e));
                return true;
            }

            // Validation 1: Check if session-replay version meets ft-sdk minimum requirement
            // sessionreplay.VERSION_NAME >= ft.sdk.REPLAY_MINI_SUPPORT
            if (!VersionUtils.firstVerGreaterEqual(replayVersion, replayMiniSupport)) {
                LogUtils.e(TAG, String.format(
                        "Version mismatch: ft-session-replay version (%s) is lower than required minimum (%s). " +
                                "Please upgrade ft-session-replay to at least version %s",
                        replayVersion, replayMiniSupport, replayMiniSupport
                ));
                return true;
            }

            // Validation 2: Check if ft-sdk version meets session-replay minimum requirement
            // ft.sdk.FT_SDK_VERSION >= sessionreplay.MINI_AGENT_SUPPORT
            if (!VersionUtils.firstVerGreaterEqual(ftSdkVersion, miniAgentSupport)) {
                LogUtils.e(TAG, String.format(
                        "Version mismatch: ft-sdk version (%s) is lower than required minimum (%s). " +
                                "Please upgrade ft-sdk to at least version %s",
                        ftSdkVersion, miniAgentSupport, miniAgentSupport
                ));
                return true;
            }

            LogUtils.d(TAG, String.format(
                    "Version validation passed: ft-sdk=%s, ft-session-replay=%s",
                    ftSdkVersion, replayVersion
            ));
            return false;

        } catch (Exception e) {
            LogUtils.e(TAG, "Version validation failed with exception: " + LogUtils.getStackTraceString(e));
            return true;
        }
    }
}

