package com.ft.sdk.sessionreplay.internal.recorder.obfuscator;

import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;

public class FixedLengthStringObfuscator implements StringObfuscator {

    @Override
    public String obfuscate(String stringValue) {
        return TextViewMapper.FIXED_INPUT_MASK;
    }
}