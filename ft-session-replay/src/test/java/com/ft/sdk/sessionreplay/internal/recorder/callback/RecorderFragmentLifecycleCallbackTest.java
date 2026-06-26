package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.Window;

import androidx.fragment.app.DialogFragment;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecorderFragmentLifecycleCallbackTest {

    @Test
    public void onFragmentResumed_dialogFragmentWithoutDialog_shouldNotNotify() {
        TestWindowCallback callback = new TestWindowCallback();
        RecorderFragmentLifecycleCallback lifecycleCallback =
                new RecorderFragmentLifecycleCallback(callback);

        lifecycleCallback.onFragmentResumed(null, new AttachedDialogFragmentWithoutDialog());

        assertEquals(0, callback.addedCount);
    }

    @Test
    public void onFragmentPaused_dialogFragmentWithoutDialog_shouldNotNotify() {
        TestWindowCallback callback = new TestWindowCallback();
        RecorderFragmentLifecycleCallback lifecycleCallback =
                new RecorderFragmentLifecycleCallback(callback);

        lifecycleCallback.onFragmentPaused(null, new AttachedDialogFragmentWithoutDialog());

        assertEquals(0, callback.removedCount);
    }

    private static class AttachedDialogFragmentWithoutDialog extends DialogFragment {
        private final Context context = new ContextWrapper(null);

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public Dialog getDialog() {
            return null;
        }
    }

    private static class TestWindowCallback implements OnWindowRefreshedCallback {
        private int addedCount;
        private int removedCount;

        @Override
        public void onWindowsAdded(List<Window> windows) {
            addedCount += windows.size();
        }

        @Override
        public void onWindowsRemoved(List<Window> windows) {
            removedCount += windows.size();
        }
    }
}
