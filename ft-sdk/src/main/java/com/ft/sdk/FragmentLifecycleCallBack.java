package com.ft.sdk;

/**
 * Callback interface used by fragment lifecycle dispatchers to notify about
 * key fragment lifecycle milestones. Implementations can use these events to
 * perform analytics, RUM view tracking, or other side effects.
 * <p>
 * The callback operates on a {@link FragmentWrapper} so that callers do not
 * need to depend on AndroidX directly.
 */
public interface FragmentLifecycleCallBack {
    /**
     * Called when the fragment is about to be attached to its context.
     *
     * @param wrapper wrapped fragment instance.
     */
    void onFragmentPreAttached(FragmentWrapper wrapper);

    /**
     * Called after the fragment has been created.
     *
     * @param wrapper wrapped fragment instance.
     */
    void onFragmentCreated(FragmentWrapper wrapper);

    /**
     * Called when the fragment has become active and is ready for user
     * interaction (resumed state).
     *
     * @param wrapper wrapped fragment instance.
     */
    void onFragmentResumed(FragmentWrapper wrapper);

    /**
     * Called when the fragment is no longer visible to the user (stopped
     * state).
     *
     * @param wrapper wrapped fragment instance.
     */
    void onFragmentStopped(FragmentWrapper wrapper);
}