package com.ft.plugin.garble.asm;

import com.android.build.api.instrumentation.InstrumentationParameters;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public abstract class FTParameters implements InstrumentationParameters {
    @Input
    public abstract ListProperty<String> getIgnorePackages();

    @Input
    public abstract ListProperty<String> getKnownWebViewClasses();

    @Input
    public abstract Property<String> getAsmVersion();

    @Input
    public abstract Property<Boolean> getVerboseLog();
}