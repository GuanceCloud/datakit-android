package com.ft.sdk.sessionreplay.internal.recorder.obfuscator;

public class NoOpStringObfuscator implements StringObfuscator {

    @Override
    public String obfuscate(String stringValue) {
        return stringValue;
    }
}