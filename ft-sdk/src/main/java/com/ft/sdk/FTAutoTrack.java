package com.ft.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import org.json.JSONObject;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    /**
     * 启动 APP
     */
    public static void startApp(Object object) {
        try {
            startApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity 开启
     * @param cName
     * @param rName
     */
    public static void activityOnCreate(String cName, String rName) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            startPage(cName, rName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity 关闭
     * @param cName
     * @param rName
     */
    public static void activityOnDestroy(String cName, String rName) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            destroyPage(cName, rName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fragment 打开
     * @param clazz
     * @param activity
     */
    public static void fragmentOnCreateView(Object clazz, Object activity) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            startPage(AopUtils.getClassName(clazz), AopUtils.getActivityName(activity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fragment 关闭
     * @param clazz
     * @param activity
     */
    public static void fragmentOnDestroyView(Object clazz, Object activity) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            destroyPage(AopUtils.getClassName(clazz), AopUtils.getActivityName(activity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     * @param view
     */
    public static void trackViewOnClick(View view) {
        if (view == null) {
            return;
        }
        trackViewOnClick(null, view, view.isPressed());
    }

    /**
     * RadioGroup的点击选择事件
     * @param group
     * @param checkedId
     */
    public static void trackRadioGroup(RadioGroup group, int checkedId){
        if (group == null) {
            return;
        }

        trackViewOnClick(null, group, true);
    }

    /**
     * listView点击事件
     * @param parent
     * @param v
     * @param position
     */
    public static void trackListView(AdapterView<?> parent, View v, int position){
        trackViewOnClick(null, v, true);
    }

    /**
     * ExpandableList 父点击事件
     * @param parent
     * @param v
     * @param position
     */
    public static void trackExpandableListViewOnGroupClick(ExpandableListView parent,View v,int position){
        trackViewOnClick(null, v, true);
    }

    /**
     * TabHost切换
     * @param tabName
     */
    public static void trackTabHost(String tabName) {
        //trackViewOnClick(null, v, true);
    }

    /**
     * ExpandableList 子点击事件
     * @param parent
     * @param v
     * @param parentPosition
     * @param childPosition
     */
    public static void trackExpandableListViewOnChildClick(ExpandableListView parent,View v,int parentPosition,int childPosition){
        trackViewOnClick(null, v, true);
    }

    /**
     * 点击事件
     * @param object
     * @param view
     */
    public static void trackViewOnClick(Object object, View view) {
        try {
            if (view == null) {
                return;
            }

            trackViewOnClick(object, view, view.isPressed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     * @param object
     * @param view
     * @param isFromUser
     */
    public static void trackViewOnClick(Object object, View view, boolean isFromUser) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            if(isFromUser) {
                if(object == null){
                    object = AopUtils.getActivityFromContext(view.getContext());
                }
                clickView(AopUtils.getClassName(object), AopUtils.getSupperClassName(object), AopUtils.getViewTree(view));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(MenuItem menuItem) {
        try {
            trackMenuItem(null, menuItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(Object object, MenuItem menuItem) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try {
            clickView(AopUtils.getClassName(object), AopUtils.getSupperClassName(object), "MenuItem/"+menuItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackDialog(DialogInterface dialogInterface, int whichButton) {
        if(!FTAutoTrackConfig.get().isAutoTrack()){
            return;
        }
        try{
            Dialog dialog = null;
            if(dialogInterface instanceof Dialog){
                dialog = (Dialog) dialogInterface;
            }

            if(dialog == null){
                return;
            }
            Context context = dialog.getContext();
            Activity activity = AopUtils.getActivityFromContext(context);
            if(activity == null){
                activity = dialog.getOwnerActivity();
            }

            clickView(AopUtils.getClassName(activity), AopUtils.getSupperClassName(activity), AopUtils.getDialogClickView(dialog,whichButton));
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

    public static void destroyPage(String currentPage, String rootPage) {
        putRecord(OP.CLS, currentPage, rootPage, null);
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
        JSONObject opData = new JSONObject();
        try {
            opData.put("vtp", vtp);
            recordData.setOpdata(opData.toString());
        } catch (Exception e) {
        }
        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("FTAutoTrack数据进数据库：" + recordData.getJsonString());
                FTManager.getFTDBManager().insertFTOperation(recordData);
                FTManager.getSyncTaskManager().executeSyncPoll();
            }
        });
    }
}
