package com.ft.sdk;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public static void startApp(Object object) {
        //LogUtils.d("[打入方法名：startApp ]  ：参数 Object=" + object);
        startApp();
    }

    public static void activityOnCreate(String cName, String rName) {
        //LogUtils.d("[打入方法名：activityOnCreate ]  ：参数 cName=" + cName + ",rName=" + rName);
        startPage(cName, rName);
    }

    public static void activityOnDestroy(String cName, String rName) {
        //LogUtils.d("[打入方法名：activityOnDestroy ]  ：参数 cName=" + cName + ",rName=" + rName);
        destoryPage(cName, rName);
    }

    public static void fragmentOnCreateView(String cName, String rName) {
        //LogUtils.d("[打入方法名：fragmentOnCreateView ]  ：参数 cName=" + cName + ",rName=" + rName);
        startPage(cName, rName);
    }

    public static void fragmentOnDestroyView(String cName, String rName) {
        //LogUtils.d("[打入方法名：fragmentOnDestroyView ]  ：参数 cName=" + cName + ",rName=" + rName);
        destoryPage(cName, rName);
    }

    public static void fragmentOnHiddenChanged(String cName, String rName, boolean isHidden) {
        //LogUtils.d("[打入方法名：fragmentOnHiddenChanged ]  ：参数 cName=" + cName + ",rName=" + rName + ",isHidden=" + isHidden);
        if (isHidden) {
            leavePage(cName, rName);
        } else {
            backPage(cName, rName);
        }
    }

    public static void trackViewOnClick(Object object, View view) {
        if (view == null) {
            return;
        }

        trackViewOnClick(object, view, view.isPressed());
    }

    public static void trackViewOnClick(Object object, View view, boolean isFromUser) {
        String text = "";
        if (view instanceof TextView || view instanceof Button) {
            text = ((TextView) view).getText().toString();
        }
        clickView(object.toString(), object.toString(), getViewTree(view));
        //LogUtils.d("[打入方法名：trackViewOnClick ]  ：参数 object=" + object + ",view.getID=" + view.getId() + ",view.text=" + text + ",view=" + view + ",isFromUser=" + isFromUser);
    }

    public static void trackMenuItem(MenuItem menuItem) {
        //LogUtils.d("[打入方法名：trackMenuItem ]  ：参数 MenuItem=" + menuItem);
        trackMenuItem(null, menuItem);
    }

    public static void trackMenuItem(Object object, MenuItem menuItem) {
        //LogUtils.d("[打入方法名：trackMenuItem ]  ：参数 MenuItem=" + menuItem + ",object=" + object);
        clickView(object.toString(), object.toString(), "");
    }


    public static void startApp() {
        putRecord(OP.LANC, null, null, null);
    }

    public static void startPage(String currentPage, String rootPage) {
        putRecord(OP.OPEN, currentPage, rootPage, null);
    }

    public static void destoryPage(String currentPage, String rootPage) {
        putRecord(OP.CLS, currentPage, rootPage, null);
    }

    public static void leavePage(String currentPage, String rootPage) {
        putRecord(OP.LEAVE, currentPage, rootPage, null);
    }

    public static void backPage(String currentPage, String rootPage) {
        putRecord(OP.BACK, currentPage, rootPage, null);
    }

    public static void clickView(String currentPage, String rootPage, String vtp) {
        putRecord(OP.CLK, currentPage, rootPage, vtp);
    }

    public static void putRecord(@NonNull OP op, @Nullable String currentPage, @Nullable String rootPage, @Nullable String vtp) {
        long time = System.currentTimeMillis();
        putRecord(time, op, currentPage, rootPage, vtp);
    }

    public static void putRecord(long time, @NonNull OP op, @Nullable String currentPage, @Nullable String rootPage, @Nullable String vtp) {
        final RecordData recordData = new RecordData();
        recordData.setTime(time);
        recordData.setOp(op);
        recordData.setCpn(currentPage);
        recordData.setRpn(rootPage);
        RecordData.OpData opData = new RecordData().new OpData();
        opData.setVtp(vtp);
        recordData.setOpdata(opData);
        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("存入数据库数据："+recordData.getJsonString());
                FTManager.getFTDBManager().insertFTOperation(recordData);
                FTManager.getSyncTaskManaget().executeSync();
            }
        });
    }

    private static String getViewTree(View view) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(view.getClass().getSimpleName() + "/");
        ViewParent viewParent = view.getParent();
        while (viewParent != null) {
            stringBuffer.append(viewParent.getClass().getSimpleName() + "/");
            viewParent = viewParent.getParent();
        }
        return stringBuffer.toString();
    }
}
