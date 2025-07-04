/*
 * Created by wangzhuohou on 2015/08/01.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ft.sdk.garble.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ft.sdk.FTApplication;

import java.lang.reflect.Method;

/**
 * This class is adapted and modified from <a href="https://github.com/sensorsdata/sa-sdk-android">Sensors Data project</a>
 * AopUtil.java class
 */
public class AopUtils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "AopUtils";

    /**
     * Get the string value on the View by View's ID
     *
     * @param view
     * @return
     */
    public static String getViewId(View view) {
        String idString = null;
        try {
            if (view.getId() != View.NO_ID) {
                idString = view.getContext().getResources().getResourceEntryName(view.getId());
            }
        } catch (Exception e) {

        }
        return idString;
    }

    /**
     * Get the current Activity from Context
     *
     * @param context
     * @return
     */
    public static Activity getActivityFromContext(Context context) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
        return activity;
    }

    /**
     * Return the current class
     *
     * @param object
     * @return
     */
    public static Class<?> getClass(Object object) {
        if (object == null) {
            return null;
        }
        return object.getClass();
    }


    /**
     * Return the name of the current class
     *
     * @param object
     * @return
     */
    public static String getClassName(Object object) {
        if (object == null) {
            return "";
        }
        if (object instanceof Class) {
            return ((Class) object).getSimpleName();
        }
        return object.getClass().getSimpleName();
    }

    /**
     * Return the name of the superclass
     *
     * @param object
     * @return
     */
    public static String getSupperClassName(Object object) {
        if (object == null) {
            return "";
        }
        if (object instanceof Class) {
            try {
                return ((Class) object).getSuperclass().getSimpleName();
            } catch (Exception e) {
                LogUtils.e(TAG, LogUtils.getStackTraceString(e));

            }
        }
        return object.getClass().getSuperclass().getSimpleName();
    }

    public static String getActivityName(Object object) {
        if (object == null) {
            return "";
        }
        return object.getClass().getSimpleName();
    }

    /**
     * Get the View tree
     *
     * @param view
     * @return
     */
    public static String getViewTree(View view) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(view.getClass().getSimpleName()).append("/");
        ViewParent viewParent = view.getParent();
        while (viewParent != null) {
            stringBuffer.insert(0, viewParent.getClass().getSimpleName() + "/");
            viewParent = viewParent.getParent();
        }
        stringBuffer.insert(0, view.getContext().getClass().getSimpleName() + "/");
        stringBuffer.append("#").append(AopUtils.getViewId(view));
        return stringBuffer.toString();
    }


    /**
     * Get MenuItem description  className/title#(Resource Entry Name)
     *
     * @param item
     * @return
     */
    public static String getMenuItem(MenuItem item) {
        try {
            return item.getClass().getSimpleName() + "/" + item.getTitle()
                    + "#" + FTApplication.getApplication().getResources().getResourceEntryName(item.getItemId());
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
        return "";

    }

    /**
     * Get View description className/text#(Resource Entry Name)
     *
     * @param view
     * @return
     */
    public static String getViewDesc(View view) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.insert(0, view.getClass().getSimpleName() + "/");
        if (view instanceof TextView) {
            stringBuffer.append(((TextView) view).getText().toString());
        } else if (view instanceof ViewGroup) {
            if (((ViewGroup) view).getChildCount() == 1) {
                View chiView = ((ViewGroup) view).getChildAt(0);
                if (chiView instanceof TextView) {
                    stringBuffer.append(((TextView) chiView).getText().toString());
                }
            }
        }
        String viewId = AopUtils.getViewId(view);
        if (viewId != null) {
            stringBuffer.append("#").append(viewId);
        }
        return stringBuffer.toString();
    }

    /**
     * Get the View tree
     *
     * @param view
     * @return
     */
    public static String getParentViewTree(View view) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(view.getClass().getSimpleName() + "/");
        ViewParent viewParent = view.getParent();
        while (viewParent != null) {
            stringBuffer.insert(0, viewParent.getClass().getSimpleName() + "/");
            viewParent = viewParent.getParent();
        }
        stringBuffer.insert(0, view.getContext().getClass().getSimpleName() + "/");
        return stringBuffer.toString();
    }

    /**
     * Get the character on the button clicked in Dialog
     *
     * @param dialog
     * @param whichButton
     * @return
     */
    public static String getDialogClickView(Dialog dialog, int whichButton) {
        Class<?> supportAlertDialogClass = null;
        Class<?> androidXAlertDialogClass = null;
        Class<?> currentAlertDialogClass;
        try {
            supportAlertDialogClass = Class.forName("android.support.v7.app.AlertDialog");
        } catch (Exception e) {
            //ignored
        }

        try {
            androidXAlertDialogClass = Class.forName("androidx.appcompat.app.AlertDialog");
        } catch (Exception e) {
            //ignored
        }

        if (supportAlertDialogClass == null && androidXAlertDialogClass == null) {
            return null;
        }

        if (supportAlertDialogClass != null) {
            currentAlertDialogClass = supportAlertDialogClass;
        } else {
            currentAlertDialogClass = androidXAlertDialogClass;
        }

        if (dialog instanceof android.app.AlertDialog) {
            android.app.AlertDialog alertDialog = (android.app.AlertDialog) dialog;
            Button button = alertDialog.getButton(whichButton);
            if (button != null) {
                return getViewDesc(button);
            } else {
                ListView listView = alertDialog.getListView();
                if (listView != null) {
                    return getViewDesc(listView);
                }
            }

        } else if (currentAlertDialogClass.isInstance(dialog)) {
            Button button = null;
            try {
                Method getButtonMethod = dialog.getClass().getMethod("getButton", int.class);
                if (getButtonMethod != null) {
                    button = (Button) getButtonMethod.invoke(dialog, whichButton);
                }
            } catch (Exception e) {
                //ignored
            }

            if (button != null) {
                return getViewDesc(button);
            } else {
                try {
                    Method getListViewMethod = dialog.getClass().getMethod("getListView");
                    if (getListViewMethod != null) {
                        ListView listView = (ListView) getListViewMethod.invoke(dialog);
                        if (listView != null) {
                            return getViewDesc(listView);
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        }
        return null;
    }
}
