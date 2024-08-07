package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class NoOpWindowCallback implements Window.Callback {

    @Override
    public void onActionModeFinished(ActionMode mode) {
        // No Op
    }

    @Override
    public View onCreatePanelView(int featureId) {
        // No Op
        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        // No Op
        return false;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        // No Op
        return null;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        // No Op
        return null;
    }

    @Override
    public void onAttachedToWindow() {
        // No Op
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // No Op
        return false;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // No Op
        return false;
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        // No Op
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // No Op
        return false;
    }

    @Override
    public void onDetachedFromWindow() {
        // No Op
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        // No Op
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        // No Op
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // No Op
    }

    @Override
    public void onContentChanged() {
        // No Op
    }

    @Override
    public boolean onSearchRequested() {
        // No Op
        return false;
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        // No Op
        return false;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        // No Op
    }
}
