package com.ft.sdk;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public static void activityOnCreate(String cName,String rName){
        LogUtils.d("[打入方法名：activityOnCreate ]  ：参数 cName="+cName+",rName="+rName);
    }

    public static void activityOnDestroy(String cName,String rName){
        LogUtils.d("[打入方法名：activityOnDestroy ]  ：参数 cName="+cName+",rName="+rName);

    }

    public static void fragmentOnCreateView(String cName,String rName){
        LogUtils.d("[打入方法名：fragmentOnCreateView ]  ：参数 cName="+cName+",rName="+rName);
    }

    public static void fragmentOnDestroyView(String cName,String rName){
        LogUtils.d("[打入方法名：fragmentOnDestroyView ]  ：参数 cName="+cName+",rName="+rName);
    }

    public static void fragmentOnHiddenChanged(String cName,String rName,boolean isHidden){
        LogUtils.d("[打入方法名：fragmentOnHiddenChanged ]  ：参数 cName="+cName+",rName="+rName+",isHidden="+isHidden);
    }

    public static void trackViewOnClick(View view) {
        if (view == null) {
            return;
        }

        trackViewOnClick(view, view.isPressed());
    }

    public static void trackViewOnClick(View view, boolean isFromUser) {
        String text="";
        if(view instanceof TextView || view instanceof Button){
            text = ((TextView) view).getText().toString();
        }
        LogUtils.d("[打入方法名：trackViewOnClick ]  ：参数 view.getID="+view.getId()+",view.text="+text+",view="+view+",isFromUser="+isFromUser);
    }


    private static HashMap<Integer, Long> eventTimestamp = new HashMap<>();

    private static boolean isDeBounceTrack(Object object) {
        boolean isDeBounceTrack = false;
        long currentOnClickTimestamp = System.currentTimeMillis();
        Object targetObject = eventTimestamp.get(object.hashCode());
        if (targetObject != null) {
            long lastOnClickTimestamp = (long) targetObject;
            if ((currentOnClickTimestamp - lastOnClickTimestamp) < 500) {
                isDeBounceTrack = true;
            }
        }

        eventTimestamp.put(object.hashCode(), currentOnClickTimestamp);
        return isDeBounceTrack;
    }

    private static void traverseView(String fragmentName, ViewGroup root) {
        LogUtils.d("[打入方法名：traverseView ]  ：参数 fragmentName="+fragmentName+", root="+root);
        try {
            if (TextUtils.isEmpty(fragmentName)) {
                return;
            }

            if (root == null) {
                return;
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);
                child.setTag(R.id.ft_tag_view_fragment_name, fragmentName);
                if (child instanceof ViewGroup && !(child instanceof ListView ||
                        child instanceof GridView ||
                        child instanceof Spinner ||
                        child instanceof RadioGroup)) {
                    traverseView(fragmentName, (ViewGroup) child);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isFragment(Object object) {
        LogUtils.d("[打入方法名：isFragment ]  ：参数 object="+object);
        try {
            if (object == null) {
                return false;
            }
            Class<?> supportFragmentClass = null;
            Class<?> androidXFragmentClass = null;
            Class<?> fragment = null;
            try {
                fragment = Class.forName("android.app.Fragment");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
                return false;
            }

            if ((supportFragmentClass != null && supportFragmentClass.isInstance(object)) ||
                    (androidXFragmentClass != null && androidXFragmentClass.isInstance(object)) ||
                    (fragment != null && fragment.isInstance(object))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void onFragmentViewCreated(Object object, View rootView, Bundle bundle) {
        LogUtils.d("[打入方法名：onFragmentViewCreated ]  ：参数 object="+object+",view="+rootView+",bundle="+bundle);
        try {
            if (!isFragment(object)) {
                return;
            }

            //Fragment名称
            String fragmentName = object.getClass().getName();
            rootView.setTag(R.id.ft_tag_view_fragment_name, fragmentName);

            if (rootView instanceof ViewGroup) {
                traverseView(fragmentName, (ViewGroup) rootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void trackFragmentResume(Object object) {
        LogUtils.d("[打入方法名：trackFragmentResume ]  ：参数 object="+object);
    }

    private static boolean fragmentGetUserVisibleHint(Object fragment) {
        LogUtils.d("[打入方法名：fragmentGetUserVisibleHint ]  ：参数 object="+fragment);
        try {
            Method getUserVisibleHintMethod = fragment.getClass().getMethod("getUserVisibleHint");
            if (getUserVisibleHintMethod != null) {
                return (boolean) getUserVisibleHintMethod.invoke(fragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean fragmentIsHidden(Object fragment) {
        LogUtils.d("[打入方法名：fragmentIsHidden ]  ：参数 object="+fragment);
        try {
            Method isHiddenMethod = fragment.getClass().getMethod("isHidden");
            if (isHiddenMethod != null) {
                return (boolean) isHiddenMethod.invoke(fragment);
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }

    public static void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser) {
        LogUtils.d("[打入方法名：trackFragmentSetUserVisibleHint ]  ：参数 object="+object+",isVisibleToUser="+isVisibleToUser);
    }

    private static boolean fragmentIsResumed(Object fragment) {
        LogUtils.d("[打入方法名：fragmentIsResumed ]  ：参数 object="+fragment);
        try {
            Method isResumedMethod = fragment.getClass().getMethod("isResumed");
            if (isResumedMethod != null) {
                return (boolean) isResumedMethod.invoke(fragment);
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }

    public static void trackOnHiddenChanged(Object object, boolean hidden) {
        LogUtils.d("[打入方法名：trackOnHiddenChanged ]  ：参数 object="+object+",hidden="+hidden);
    }

    public static void trackExpandableListViewOnGroupClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition) {
        LogUtils.d("[打入方法名：trackExpandableListViewOnGroupClick ]  ：参数 ExpandableListView="+expandableListView+",view="+view+",groupPosition"+groupPosition);
    }

    public static void trackExpandableListViewOnChildClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition, int childPosition) {
        LogUtils.d("[打入方法名：trackExpandableListViewOnChildClick ]  ：参数 ExpandableListView="+expandableListView+",view="+view+",groupPosition"+groupPosition+",childPosition"+childPosition);
    }

    public static void trackTabHost(String tabName) {
        LogUtils.d("[打入方法名：trackTabHost ]  ：参数 tabName="+tabName);
    }

    public static void trackTabLayoutSelected(Object object, Object tab) {
        LogUtils.d("[打入方法名：trackTabLayoutSelected ]  ：参数 object="+object+",tab="+tab);
    }

    public static void trackMenuItem(MenuItem menuItem) {
        LogUtils.d("[打入方法名：trackMenuItem ]  ：参数 MenuItem="+menuItem);
        trackMenuItem(null, menuItem);
    }

    public static void trackMenuItem(Object object, MenuItem menuItem) {
        LogUtils.d("[打入方法名：trackMenuItem ]  ：参数 MenuItem="+menuItem+",object="+object);
    }

    public static void trackRadioGroup(RadioGroup view, int checkedId) {
        LogUtils.d("[打入方法名：trackRadioGroup ]  ：参数 RadioGroup="+view+",checkedId="+checkedId);
    }

    public static void trackDialog(DialogInterface dialogInterface, int whichButton) {
        LogUtils.d("[打入方法名：trackDialog ]  ：参数 dialogInterface="+dialogInterface+",whichButton="+whichButton);
    }

    public static void trackListView(AdapterView<?> adapterView, View view, int position) {
        LogUtils.d("[打入方法名：trackListView ]  ：参数 adapterView="+adapterView+",view="+view+",position="+position);
    }

    public static void trackDrawerOpened(View view) {
        LogUtils.d("[打入方法名：trackDrawerOpened ]  ：参数 view="+view);
    }

    public static void trackDrawerClosed(View view) {
        LogUtils.d("[打入方法名：trackDrawerClosed ]  ：参数 view="+view);
    }

    public static void track(String eventName, String properties) {
        LogUtils.d("[打入方法名：track ]  ：参数 eventName="+eventName+",properties="+properties);
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
        System.out.println("即将插入："+recordData.getJsonString());
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().insertFTOperation(recordData);
                System.out.println("全部数据："+FTDBManager.get().queryFTOperation());
            }
        });
    }
}
