package com.ft.sdk;

public class FragmentWrapper {
    private final Object realFragment;
    private static final boolean hasAndroidX;
    private static final Class<?> androidxFragmentClass;

    static {
        Class<?> cls = null;
        try {
            cls = Class.forName("androidx.fragment.app.Fragment");
        } catch (Throwable ignore) {
        }
        androidxFragmentClass = cls;
        hasAndroidX = cls != null;
    }

    public FragmentWrapper(Object fragment) {
        this.realFragment = fragment;
    }

    public boolean isAndroidX() {
        return hasAndroidX && androidxFragmentClass.isInstance(realFragment);
    }

    public Object getRealFragment() {
        return realFragment;
    }

    public String getClassName() {
        return realFragment.getClass().getName();
    }

    public String getSimpleClassName() {
        return realFragment.getClass().getSimpleName();
    }
}