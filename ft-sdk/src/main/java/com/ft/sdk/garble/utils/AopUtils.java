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
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Method;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 https://github.com/sensorsdata/sa-sdk-android
 * 中的 AopUtil.java 类
 */
public class AopUtils {
    /**
     * 通过 View 的 ID 获取 View 上的字符串值
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
     * 通过 Context 获取当前的 Activity
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
            e.printStackTrace();
        }
        return activity;
    }

    /**
     * 返回当前类的名称
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
     * 返回父类名称
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
                e.printStackTrace();
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
     * 获取 View 视图树
     *
     * @param view
     * @return
     */
    public static String getViewTree(View view) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(view.getClass().getSimpleName() + "/");
        ViewParent viewParent = view.getParent();
        while (viewParent != null) {
            stringBuffer.insert(0, viewParent.getClass().getSimpleName() + "/");
            viewParent = viewParent.getParent();
        }
        stringBuffer.insert(0, view.getContext().getClass().getSimpleName() + "/");
        stringBuffer.append("#" + AopUtils.getViewId(view));
        return stringBuffer.toString();
    }

    /**
     * 获取 Dialog 上点击按钮上的字符
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
                return getViewTree(button);
            } else {
                ListView listView = alertDialog.getListView();
                if (listView != null) {
                    return getViewTree(listView);
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
                return getViewTree(button);
            } else {
                try {
                    Method getListViewMethod = dialog.getClass().getMethod("getListView");
                    if (getListViewMethod != null) {
                        ListView listView = (ListView) getListViewMethod.invoke(dialog);
                        if (listView != null) {
                            return getViewTree(listView);
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
