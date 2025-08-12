package com.ft.sdk;

/**
 * A lightweight wrapper around a Fragment instance that abstracts the concrete
 * fragment type across Android platform fragments (android.app.Fragment) and
 * AndroidX fragments (androidx.fragment.app.Fragment).
 * <p>
 * This wrapper enables lifecycle helpers to work with either implementation at
 * runtime without introducing hard dependencies on AndroidX. It performs a
 * best-effort detection of AndroidX presence and provides convenience accessors
 * for the underlying fragment object and its class information.
 */
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

    /**
     * Create a new wrapper for the given fragment object.
     *
     * @param fragment concrete fragment instance, either
     *                 {@code android.app.Fragment} or
     *                 {@code androidx.fragment.app.Fragment}.
     */
    public FragmentWrapper(Object fragment) {
        this.realFragment = fragment;
    }

    /**
     * Whether the wrapped fragment is an AndroidX fragment.
     *
     * @return true if AndroidX is available at runtime and the underlying
     * fragment is an instance of {@code androidx.fragment.app.Fragment}.
     */
    public boolean isAndroidX() {
        return hasAndroidX && androidxFragmentClass.isInstance(realFragment);
    }

    /**
     * Returns the original fragment object.
     *
     * @return the wrapped fragment instance.
     */
    public Object getRealFragment() {
        return realFragment;
    }

    /**
     * Returns the fully qualified class name of the wrapped fragment.
     *
     * @return fragment class name, e.g. "com.example.MyFragment".
     */
    public String getClassName() {
        return realFragment.getClass().getName();
    }

    /**
     * Returns the simple class name of the wrapped fragment.
     *
     * @return fragment simple class name, e.g. "MyFragment".
     */
    public String getSimpleClassName() {
        return realFragment.getClass().getSimpleName();
    }
}