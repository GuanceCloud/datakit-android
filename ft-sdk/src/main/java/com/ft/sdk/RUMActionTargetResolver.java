package com.ft.sdk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabWidget;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.HashMap;

final class RUMActionTargetResolver {
    private static final String EXTRA_CHECKED_ID = "checkedId";
    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_TAB_ID = "tabId";

    private RUMActionTargetResolver() {
    }

    static View resolve(Object object, HashMap<String, Object> extra) {
        if (object instanceof RadioGroup) {
            return resolveRadioButton((RadioGroup) object, extra);
        }
        if (object instanceof View) {
            return (View) object;
        }
        if (object instanceof MenuItem) {
            return resolveMenuActionView((MenuItem) object);
        }
        if (object instanceof Dialog) {
            return resolveDialogTarget((Dialog) object, extra);
        }
        if (object == null && extra != null && extra.containsKey(EXTRA_TAB_ID)) {
            return resolveTabTarget(extra.get(EXTRA_TAB_ID));
        }
        return null;
    }

    private static View resolveRadioButton(RadioGroup group, HashMap<String, Object> extra) {
        Integer checkedId = getInteger(extra, EXTRA_CHECKED_ID);
        if (checkedId != null) {
            View checkedView = group.findViewById(checkedId);
            if (checkedView != null) {
                return checkedView;
            }
        }
        return group;
    }

    private static View resolveMenuActionView(MenuItem menuItem) {
        try {
            View actionView = menuItem.getActionView();
            if (actionView != null) {
                return actionView;
            }
        } catch (Exception ignored) {
        }
        View touchedView = RUMTouchPositionTracker.resolveLastTouchTarget();
        return matchesMenuItem(touchedView, menuItem) ? touchedView : null;
    }

    private static boolean matchesMenuItem(View view, MenuItem menuItem) {
        if (view == null) {
            return false;
        }
        int itemId = menuItem.getItemId();
        int viewId = view.getId();
        if (itemId != 0 && itemId != View.NO_ID
                && viewId != 0 && viewId != View.NO_ID) {
            return viewId == itemId;
        }

        CharSequence title = menuItem.getTitle();
        if (view instanceof TextView && equalsText(title, ((TextView) view).getText())) {
            return true;
        }
        CharSequence viewDescription = view.getContentDescription();
        if (equalsText(title, viewDescription)) {
            return true;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && equalsText(menuItem.getContentDescription(), viewDescription);
    }

    private static View resolveTabTarget(Object tabId) {
        View touchedView = RUMTouchPositionTracker.resolveLastTouchTarget();
        if (touchedView == null) {
            return null;
        }
        if (tabId != null && tabId.equals(touchedView.getTag())) {
            return touchedView;
        }

        View current = touchedView;
        while (current != null) {
            if (current instanceof TabWidget) {
                return touchedView;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return null;
    }

    private static boolean equalsText(CharSequence first, CharSequence second) {
        if (first == null || second == null) {
            return false;
        }
        String firstText = first.toString().trim();
        String secondText = second.toString().trim();
        return !firstText.isEmpty() && !secondText.isEmpty()
                && firstText.equalsIgnoreCase(secondText);
    }

    private static View resolveDialogTarget(Dialog dialog, HashMap<String, Object> extra) {
        Integer position = getInteger(extra, EXTRA_POSITION);
        if (position == null) {
            return null;
        }
        if (position < 0) {
            return selectDialogTarget(position, resolveDialogButton(dialog, position),
                    null, null);
        }

        ListView listView = resolveDialogListView(dialog);
        View listItem = null;
        if (listView != null) {
            int childIndex = position - listView.getFirstVisiblePosition();
            if (childIndex >= 0 && childIndex < listView.getChildCount()) {
                listItem = listView.getChildAt(childIndex);
            }
        }
        return selectDialogTarget(position, null, listItem, listView);
    }

    static View selectDialogTarget(int position, View button, View listItem, View listView) {
        if (position < 0) {
            return button;
        }
        return listItem != null ? listItem : listView;
    }

    private static View resolveDialogButton(Dialog dialog, int whichButton) {
        if (dialog instanceof AlertDialog) {
            return ((AlertDialog) dialog).getButton(whichButton);
        }
        try {
            Method method = dialog.getClass().getMethod("getButton", int.class);
            Object button = method.invoke(dialog, whichButton);
            return button instanceof View ? (View) button : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static ListView resolveDialogListView(Dialog dialog) {
        if (dialog instanceof AlertDialog) {
            return ((AlertDialog) dialog).getListView();
        }
        try {
            Method method = dialog.getClass().getMethod("getListView");
            Object listView = method.invoke(dialog);
            return listView instanceof ListView ? (ListView) listView : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Integer getInteger(HashMap<String, Object> extra, String key) {
        if (extra == null) {
            return null;
        }
        Object value = extra.get(key);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
