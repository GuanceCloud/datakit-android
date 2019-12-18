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
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false)

                )));
        ACTIVITY_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "activityOnDestroy",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false),
                        new FTSubMethodCell(FTMethodType.ALOAD, 0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false)

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

    public final static HashMap<String, FTMethodCell> LAMBDA_METHODS = new HashMap<>();
    public final static HashMap<String, FTMethodCell> mLambdaMethodCells = new HashMap<>();
    public final static HashMap<String, FTMethodCell> CLICK_METHODS_SYSTEM = new HashMap<>();
    static {
        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/view/View;)V",
                "Landroid/view/View$OnClickListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z)V",
                "Landroid/widget/CompoundButton$OnCheckedChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));
        addLambdaMethod(new FTMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ)V",
                "Landroid/widget/RatingBar$OnRatingBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;)V",
                "Landroid/widget/SeekBar$OnSeekBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "Landroid/widget/RadioGroup$OnCheckedChangeListener;",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I)V",
                "Landroid/content/DialogInterface$OnClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "Landroid/widget/AdapterView$OnItemClickListener;",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z",
                "Landroid/widget/ExpandableListView$OnGroupClickListener;",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));
        addLambdaMethod(new FTMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z",
                "Landroid/widget/ExpandableListView$OnChildClickListener;",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",
                1, 4,Arrays.asList(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;)V",
                "Landroid/widget/TabHost$OnTabChangeListener;",
                "trackTabHost",
                "(Ljava/lang/String;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/design/widget/NavigationView$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ)V",
                "Landroid/content/DialogInterface$OnMultiChoiceClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

    }

    static void addLambdaMethod(FTMethodCell ftMethodCell) {
        if (ftMethodCell != null) {
            LAMBDA_METHODS.put(ftMethodCell.parent + ftMethodCell.name + ftMethodCell.desc, ftMethodCell);
            CLICK_METHODS_SYSTEM.put(ftMethodCell.name + ftMethodCell.desc, ftMethodCell);
        }
    }
}
