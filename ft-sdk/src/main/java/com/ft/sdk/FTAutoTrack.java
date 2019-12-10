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
        try {
            startApp();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void activityOnCreate(String cName, String rName) {
        try {
            startPage(cName, rName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void activityOnDestroy(String cName, String rName) {
        try {
            destoryPage(cName, rName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void fragmentOnCreateView(String cName, String rName) {
        try {
            startPage(cName, rName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void fragmentOnDestroyView(String cName, String rName) {
        try {
            destoryPage(cName, rName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void fragmentOnHiddenChanged(String cName, String rName, boolean isHidden) {
        try {
            if (isHidden) {
                leavePage(cName, rName);
            } else {
                backPage(cName, rName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void trackViewOnClick(Object object, View view) {
        try {
            if (view == null) {
                return;
            }

            trackViewOnClick(object, view, view.isPressed());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void trackViewOnClick(Object object, View view, boolean isFromUser) {
        try {
            clickView(getClassName(object), getSupperClassName(object), getViewTree(view));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(MenuItem menuItem) {
        try {
            trackMenuItem(null, menuItem);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(Object object, MenuItem menuItem) {
        try {
            clickView(getClassName(object),getSupperClassName(object), "");
        }catch (Exception e){
            e.printStackTrace();
        }
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
        recordData.setOp(op.value);
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
                FTManager.getSyncTaskManaget().executeSyncPoll();
            }
        });
    }

    private static String getViewTree(View view) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(view.getClass().getSimpleName() + "/");
        ViewParent viewParent = view.getParent();
        while (viewParent != null) {
            stringBuffer.insert(0,viewParent.getClass().getSimpleName() + "/");
            viewParent = viewParent.getParent();
        }
        stringBuffer.append("#"+view.getId());
        if(view instanceof TextView){
            stringBuffer.append("_"+((TextView) view).getText());
        }
        return stringBuffer.toString();
    }

    private static String getClassName(Object object){
        if(object == null){
            return "";
        }
        if(object instanceof Class){
            return ((Class) object).getSimpleName();
        }
        return object.getClass().getSimpleName();
    }

    private static String getSupperClassName(Object object){
        if(object == null){
            return "";
        }
        if(object instanceof Class){
            try {
                Class clazz = Class.forName(((Class) object).getName());
                return clazz.getSuperclass().getSimpleName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return object.getClass().getSuperclass().getSimpleName();
    }
}
