package com.ft.sdk.garble.gesture;

import android.os.Build;
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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ft.sdk.ActionEventWrapper;
import com.ft.sdk.ActionSourceType;
import com.ft.sdk.FTActionTrackingHandler;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.HandlerAction;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.lang.ref.WeakReference;

public class WindowCallbackWrapper implements Window.Callback {

    private static final String TAG = "WindowCallbackWrapper";
    private final Window.Callback wrappedCallback;
    private final WeakReference<Window> weekWindow;


    public WindowCallbackWrapper(Window window,
                                 Window.Callback wrappedCallback
    ) {
        this.weekWindow = new WeakReference<>(window);
        this.wrappedCallback = wrappedCallback == null ? new NoOpWindowCallback() : wrappedCallback;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event != null) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                LogUtils.d(TAG, event.toString());
                FTActionTrackingHandler handler = FTRUMConfigManager.get().getConfig().getActionTrackingHandler();

                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        if (handler != null) {
                            HandlerAction action = handler.resolveHandlerAction(new ActionEventWrapper(null,
                                    ActionSourceType.CLICK_BACK, null));
                            if (action != null) {
                                FTRUMGlobalManager.get().startAction(action.getActionName(),
                                        Constants.EVENT_NAME_CLICK, action.getProperty());
                            }

                        } else {
                            FTRUMGlobalManager.get().startAction(Constants.EVENT_NAME_ACTION_NAME_BACK,
                                    Constants.EVENT_NAME_CLICK);
                        }
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        if (handler != null) {
                            HandlerAction action = handler.resolveHandlerAction(new ActionEventWrapper(null,
                                    ActionSourceType.CLICK_MENU, null));
                            if (action != null) {
                                FTRUMGlobalManager.get().startAction(action.getActionName(),
                                        Constants.EVENT_NAME_CLICK, action.getProperty());
                            }
                        } else {
                            FTRUMGlobalManager.get().startAction(Constants.EVENT_NAME_ACTION_NAME_MENU,
                                    Constants.EVENT_NAME_CLICK);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        Window window = weekWindow.get();
                        if (window != null) {
                            View currentFocus = window.getCurrentFocus();
                            if (currentFocus != null) {
                                if (handler != null) {
                                    HandlerAction action = handler.resolveHandlerAction(new ActionEventWrapper(currentFocus,
                                            ActionSourceType.CLICK_VIEW, null));
                                    if (action != null) {
                                        FTRUMGlobalManager.get().startAction(action.getActionName(),
                                                Constants.EVENT_NAME_CLICK, action.getProperty());
                                    }
                                } else {
                                    FTRUMGlobalManager.get().startAction(AopUtils.getViewDesc(currentFocus),
                                            Constants.EVENT_NAME_CLICK);
                                }
                            } else {
                                if (handler != null) {
                                    HandlerAction action = handler.resolveHandlerAction(new ActionEventWrapper(null,
                                            ActionSourceType.CLICK_PAD_CENTER, null));
                                    if (action != null) {
                                        FTRUMGlobalManager.get().startAction(action.getActionName(),
                                                Constants.EVENT_NAME_CLICK, action.getProperty());
                                    }
                                } else {
                                    // not view focus ,generate dpad event default
                                    FTRUMGlobalManager.get().startAction(Constants.EVENT_NAME_ACTION_NAME_DPAD_CENTER,
                                            Constants.EVENT_NAME_CLICK);
                                }

                            }
                        }
                        break;
                }
            }
        }
        return wrappedCallback.dispatchKeyEvent(event);
    }


    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return wrappedCallback.dispatchKeyShortcutEvent(event);
    }

    @Override
    @MainThread
    public boolean dispatchTouchEvent(MotionEvent event) {
        return wrappedCallback.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return wrappedCallback.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return wrappedCallback.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return wrappedCallback.dispatchPopulateAccessibilityEvent(event);
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        return wrappedCallback.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        return wrappedCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
        return wrappedCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        return wrappedCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        return wrappedCallback.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        wrappedCallback.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        wrappedCallback.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        wrappedCallback.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        wrappedCallback.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        wrappedCallback.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        wrappedCallback.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return wrappedCallback.onSearchRequested();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return wrappedCallback.onSearchRequested(searchEvent);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return wrappedCallback.onWindowStartingActionMode(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return wrappedCallback.onWindowStartingActionMode(callback, type);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        wrappedCallback.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        wrappedCallback.onActionModeFinished(mode);
    }

    public Window.Callback getWrappedCallback() {
        return wrappedCallback;
    }
}
