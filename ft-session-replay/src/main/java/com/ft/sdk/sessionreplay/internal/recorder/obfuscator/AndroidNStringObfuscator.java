package com.ft.sdk.sessionreplay.internal.recorder.obfuscator;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.stream.IntStream;

public class AndroidNStringObfuscator implements StringObfuscator {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public String obfuscate(String stringValue) {
        StringBuilder obfuscatedString = new StringBuilder(stringValue.length());
        IntStream codePoints = stringValue.codePoints();

        codePoints.forEach(codePoint -> {
            if (Character.isWhitespace(codePoint)) {
                try {
                    obfuscatedString.append(Character.toChars(codePoint));
                } catch (IllegalArgumentException e) {
                    obfuscatedString.append(CHARACTER_MASK);
                }
            } else {
                obfuscatedString.append(CHARACTER_MASK);
            }
        });

        return obfuscatedString.toString();
    }
}
