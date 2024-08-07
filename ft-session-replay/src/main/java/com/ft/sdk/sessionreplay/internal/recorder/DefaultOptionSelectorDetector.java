package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.ViewGroup;
import androidx.appcompat.widget.AppCompatSpinner;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

import java.util.HashSet;
import java.util.Set;

public class DefaultOptionSelectorDetector implements OptionSelectorDetector {

    private static final String DROP_DOWN_LIST_CLASS_NAME = "android.widget.DropDownListView";
    private static final String APPCOMPAT_DROP_DOWN_LIST_CLASS_NAME = "androidx.appcompat.widget.DropDownListView";
    private static final String MATERIAL_TIME_PICKER_CLASS_NAME = "com.google.android.material.timepicker.TimePickerView";
    private static final String MATERIAL_CALENDAR_GRID_CLASS_NAME = "com.google.android.material.datepicker.MaterialCalendarGridView";

    private static final Set<String> OPTION_SELECTORS_CLASS_NAMES_SET = new HashSet<>();
    static {
        OPTION_SELECTORS_CLASS_NAMES_SET.add(DROP_DOWN_LIST_CLASS_NAME);
        OPTION_SELECTORS_CLASS_NAMES_SET.add(APPCOMPAT_DROP_DOWN_LIST_CLASS_NAME);
        OPTION_SELECTORS_CLASS_NAMES_SET.add(MATERIAL_TIME_PICKER_CLASS_NAME);
        OPTION_SELECTORS_CLASS_NAMES_SET.add(MATERIAL_CALENDAR_GRID_CLASS_NAME);
    }

    @Override
    public boolean isOptionSelector(ViewGroup view) {
        String viewClassName = view.getClass().getCanonicalName();
        if (viewClassName == null) {
            viewClassName = "";
        }

        boolean isAppCompatSpinner = AppCompatSpinner.class.isAssignableFrom(view.getClass());

        return OPTION_SELECTORS_CLASS_NAMES_SET.contains(viewClassName) ||
                isAppCompatSpinner;
    }
}
