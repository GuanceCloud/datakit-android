package com.ft.plugin.garble;


import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-03 14:39
 * Description:
 */
public class FTHookConfig {

    public static final String FT_SDK_API = "com/ft/sdk/FTAutoTrack";

    /**
     * Application中的方法
     */
    public static final HashMap<String, FTMethodCell> APPLICATION_METHODS = new HashMap<>();

    static {
        APPLICATION_METHODS.put("onCreate()V", new FTMethodCell(
                "onCreate",
                "()V",
                "startApp",
                "(Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
                )
        ));
    }

    /**
     * Activity中的方法
     */
    public final static HashMap<String, FTMethodCell> ACTIVITY_METHODS = new HashMap<>();

    static {
        ACTIVITY_METHODS.put("onCreate(Landroid/os/Bundle;)V", new FTMethodCell(
                "onCreate",
                "(Landroid/os/Bundle;)V",
                "activityOnCreate",
                "(Ljava/lang/Class;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
                )));
        ACTIVITY_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "activityOnDestroy",
                "(Ljava/lang/Class;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
                )));

    }

    /**
     * FragmentX中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_X_METHODS = new HashMap<>();

    static {
        FRAGMENT_X_METHODS.put("onCreate(Landroid/os/Bundle;)V", new FTMethodCell(
                "onCreate",
                "(Landroid/os/Bundle;)V",
                "fragmentOnCreateView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "androidx/fragment/app/Fragment", "getActivity", "()Landroidx/fragment/app/FragmentActivity;", false)
                )));
        FRAGMENT_X_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "androidx/fragment/app/Fragment", "getActivity", "()Landroidx/fragment/app/FragmentActivity;", false)
                )));
    }


    /**
     * FragmentX中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_METHODS = new HashMap<>();

    static {
        FRAGMENT_METHODS.put("onCreate(Landroid/os/Bundle;)V", new FTMethodCell(
                "onCreate",
                "(Landroid/os/Bundle;)V",
                "fragmentOnCreateView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "android/app/Fragment", "getActivity", "()Landroid/app/Activity;", false)
                )));
        FRAGMENT_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "android/app/Fragment", "getActivity", "()Landroid/app/Activity;", false)
                )));
    }

    /**
     * FragmentV4中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_V4_METHODS = new HashMap<>();

    static {
        FRAGMENT_V4_METHODS.put("onCreate(Landroid/os/Bundle;)V", new FTMethodCell(
                "onCreate",
                "(Landroid/os/Bundle;)V",
                "fragmentOnCreateView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "android/support/v4/app/Fragment", "getActivity", "()Landroid/support/v4/app/FragmentActivity;", false)
                )));
        FRAGMENT_V4_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "android/support/v4/app/Fragment", "getActivity", "()Landroid/support/v4/app/FragmentActivity;", false)
                )));

    }

    public final static FTMethodCell MENU_METHODS = new FTMethodCell(
            "", "", "trackMenuItem", "(Ljava/lang/Object;Landroid/view/MenuItem;)V",
            Arrays.asList(
                    new FTSubMethodCell(FTMethodType.ALOAD, 0),
                    new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                    new FTSubMethodCell(FTMethodType.ALOAD, 1)
            )
    );

    public final static FTMethodCell CLICK_METHOD = new FTMethodCell(
            "", "", "trackViewOnClick", "(Ljava/lang/Object;Landroid/view/View;)V",
            Arrays.asList(
                    new FTSubMethodCell(FTMethodType.ALOAD, 0),
                    new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                    new FTSubMethodCell(FTMethodType.ALOAD, 1)
            )
    );


    /*
     * Created by wangzhuozhou on 2015/08/12.
     * Copyright 2015－2019 Sensors Data Inc.
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

    public final static HashMap<String, FTMethodCell> CLICK_METHODS_SYSTEM = new HashMap<>();

    static {
        addInterfaceMethod(new FTMethodCell(
                "onClick",
                "(Landroid/view/View;)V",
                "android/view/View$OnClickListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z)V",
                "android/widget/CompoundButton$OnCheckedChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//
        addInterfaceMethod(new FTMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ)V",
                "android/widget/RatingBar$OnRatingBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;)V",
                "android/widget/SeekBar$OnSeekBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "android/widget/RadioGroup$OnCheckedChangeListener",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "android/widget/AdapterView$OnItemClickListener",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onItemSelected",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "android/widget/AdapterView$OnItemSelectedListener",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));//


        addInterfaceMethod(new FTMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z",
                "android/widget/ExpandableListView$OnGroupClickListener",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));//
        addInterfaceMethod(new FTMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z",
                "android/widget/ExpandableListView$OnChildClickListener",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",
                1, 4,Arrays.asList(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;)V",
                "android/widget/TabHost$OnTabChangeListener",
                "trackTabHost",
                "(Ljava/lang/String;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "android/support/design/widget/NavigationView$OnNavigationItemSelectedListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/support/v7/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "androidx/appcompat/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ)V",
                "android/content/DialogInterface$OnMultiChoiceClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//
        addInterfaceMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I)V",
                "android/content/DialogInterface$OnClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "androidx/appcompat/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/support/v7/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

    }

    static void addInterfaceMethod(FTMethodCell ftMethodCell) {
        if (ftMethodCell != null) {
            CLICK_METHODS_SYSTEM.put(ftMethodCell.parent+ftMethodCell.name + ftMethodCell.desc, ftMethodCell);
        }
    }

    public final static HashMap<String, FTMethodCell> LAMBDA_METHODS = new HashMap<>();
    public final static HashMap<String, FTMethodCell> mLambdaMethodCells = new HashMap<>();
    static {
        addLambdaMethod1(new FTMethodCell(
                "onClick",
                "(Landroid/view/View;)V",
                "Landroid/view/View$OnClickListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z)V",
                "Landroid/widget/CompoundButton$OnCheckedChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//
        addLambdaMethod1(new FTMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ)V",
                "Landroid/widget/RatingBar$OnRatingBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;)V",
                "Landroid/widget/SeekBar$OnSeekBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "Landroid/widget/RadioGroup$OnCheckedChangeListener;",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "Landroid/widget/AdapterView$OnItemClickListener;",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z",
                "Landroid/widget/ExpandableListView$OnGroupClickListener;",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));//
        addLambdaMethod1(new FTMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z",
                "Landroid/widget/ExpandableListView$OnChildClickListener;",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",
                1, 4,Arrays.asList(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;)V",
                "Landroid/widget/TabHost$OnTabChangeListener;",
                "trackTabHost",
                "(Ljava/lang/String;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/design/widget/NavigationView$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ)V",
                "Landroid/content/DialogInterface$OnMultiChoiceClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//
        addLambdaMethod1(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I)V",
                "Landroid/content/DialogInterface$OnClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));//

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod1(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

    }

    static void addLambdaMethod1(FTMethodCell ftMethodCell) {
        if (ftMethodCell != null) {
            LAMBDA_METHODS.put(ftMethodCell.parent + ftMethodCell.name + ftMethodCell.desc, ftMethodCell);
        }
    }
}
