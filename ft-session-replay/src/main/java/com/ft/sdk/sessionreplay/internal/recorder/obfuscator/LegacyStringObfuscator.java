package com.ft.sdk.sessionreplay.internal.recorder.obfuscator;

public class LegacyStringObfuscator implements StringObfuscator {

    @Override
    public String obfuscate(String stringValue) {
        char[] obfuscatedChars = new char[stringValue.length()];

        for (int i = 0; i < stringValue.length(); i++) {
            char character = stringValue.charAt(i);
            if (Character.isWhitespace(character)) {
                obfuscatedChars[i] = character;
            } else {
                obfuscatedChars[i] = CHARACTER_MASK;
            }
        }

        return new String(obfuscatedChars);
    }
}
