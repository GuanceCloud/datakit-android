package com.ft.sdk.sessionreplay.internal.recorder.obfuscator;

import android.os.Build;

public interface StringObfuscator {
    String obfuscate(String stringValue);
    public static final char CHARACTER_MASK = 'x';

    public static StringObfuscator getStringObfuscator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new AndroidNStringObfuscator();
        } else {
            return new LegacyStringObfuscator();
        }
    }
}
