package com.ft.plugin.garble;


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
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSuperclass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false)

                )));
        ACTIVITY_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "activityOnDestroy",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSuperclass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false)

                )));

    }

    /**
     * FragmentX中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_X_METHODS = new HashMap<>();

    static {
        FRAGMENT_X_METHODS.put("onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;", new FTMethodCell(
                "onCreateView",
                "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;",
                "fragmentOnCreateView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"androidx/fragment/app/Fragment","getActivity","()Landroidx/fragment/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"androidx/fragment/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_X_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"androidx/fragment/app/Fragment","getActivity","()Landroidx/fragment/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"androidx/fragment/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_X_METHODS.put("onHiddenChanged(Z)V", new FTMethodCell(
                "onHiddenChanged",
                "(Z)V",
                "fragmentOnHiddenChanged",
                "(Ljava/lang/String;Ljava/lang/String;Z)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"androidx/fragment/app/Fragment","getActivity","()Landroidx/fragment/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"androidx/fragment/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ILOAD,1)
                )));
    }


    /**
     * FragmentX中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_METHODS = new HashMap<>();

    static {
        FRAGMENT_METHODS.put("onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;", new FTMethodCell(
                "onCreateView",
                "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;",
                "fragmentOnCreateView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/app/Fragment","getActivity","()Landroid/app/Activity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/app/Activity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/app/Fragment","getActivity","()Landroid/app/Activity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/app/Activity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_METHODS.put("onHiddenChanged(Z)V", new FTMethodCell(
                "onHiddenChanged",
                "(Z)V",
                "fragmentOnHiddenChanged",
                "(Ljava/lang/String;Ljava/lang/String;Z)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/app/Fragment","getActivity","()Landroid/app/Activity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/app/Activity","getLocalClassName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ILOAD,1)
                )));
    }

    /**
     * FragmentV4中的方法
     */
    public final static HashMap<String, FTMethodCell> FRAGMENT_V4_METHODS = new HashMap<>();

    static {
        FRAGMENT_V4_METHODS.put("onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;", new FTMethodCell(
                "onCreateView",
                "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;",
                "fragmentOnCreateView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/support/v4/app/Fragment","getActivity","()Landroid/support/v4/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/support/v4/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_V4_METHODS.put("onDestroy()V", new FTMethodCell(
                "onDestroy",
                "()V",
                "fragmentOnDestroyView",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/support/v4/app/Fragment","getActivity","()Landroid/support/v4/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/support/v4/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false)

                )));
        FRAGMENT_V4_METHODS.put("onHiddenChanged(Z)V", new FTMethodCell(
                "onHiddenChanged",
                "(Z)V",
                "fragmentOnHiddenChanged",
                "(Ljava/lang/String;Ljava/lang/String;Z)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object","getClass","()Ljava/lang/Class;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKESPECIAL,"android/support/v4/app/Fragment","getActivity","()Landroid/support/v4/app/FragmentActivity;",false),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"android/support/v4/app/FragmentActivity","getLocalClassName","()Ljava/lang/String;",false),
                        new FTSubMethodCell(FTMethodType.ILOAD,1)
                )));
    }

    public final static FTMethodCell MENU_METHODS = new FTMethodCell(
            "","","trackMenuItem","(Ljava/lang/Object;Landroid/view/MenuItem;)V",
            Arrays.asList(
                    new FTSubMethodCell(FTMethodType.ALOAD,0),
                    new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                    new FTSubMethodCell(FTMethodType.ALOAD,0)
            )
    );


    public final static HashMap<String, FTMethodCell> INTERFACE_METHODS = new HashMap<>();
/**
    static {
        addInterfaceMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z);V",
                "android/widget/CompoundButton$OnCheckedChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ);V",
                "android/widget/RatingBar$OnRatingBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,
                Arrays.asList(Opcodes.ALOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;);V",
                "android/widget/SeekBar$OnSeekBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,
                Arrays.asList(Opcodes.ALOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I);V",
                "android/widget/RadioGroup$OnCheckedChangeListener",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I);V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I);V",
                "android/content/DialogInterface$OnClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I);V",
                1, 2,
                Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onItemSelected",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ);V",
                "android/widget/AdapterView$OnItemSelectedListener",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I);V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD, Opcodes.ILOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ);Z",
                "android/widget/ExpandableListView$OnGroupClickListener",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I);V",
                1, 3,
                Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD, Opcodes.ILOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ);Z",
                "android/widget/ExpandableListView$OnChildClickListener",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II);V",
                1, 4,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD, Opcodes.ILOAD,Opcodes.ILOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;);V",
                "android/widget/TabHost$OnTabChangeListener",
                "trackTabHost",
                "(Ljava/lang/String;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));
        addInterfaceMethod(new FTMethodCell(
                "onTabSelected",
                "(Landroid/support/design/widget/TabLayout$Tab;);V",
                "android/support/design/widget/TabLayout$OnTabSelectedListener",
                "trackTabLayoutSelected",
                "(Ljava/lang/Object;Ljava/lang/Object;);V",
                0, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onTabSelected",
                "(Lcom/google/android/material/tabs/TabLayout$Tab;);V",
                "com/google/android/material/tabs/TabLayout$OnTabSelectedListener",
                "trackTabLayoutSelected",
                "(Ljava/lang/Object;Ljava/lang/Object;);V",
                0, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "android/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "android/support/v7/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "androidx/appcompat/widget/Toolbar$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ);V",
                "android/content/DialogInterface$OnMultiChoiceClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I);V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "android/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "androidx/appcompat/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addInterfaceMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "android/support/v7/widget/PopupMenu$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

    }*/

    static void addInterfaceMethod(FTMethodCell FTMethodCell) {
        if (FTMethodCell != null); {
            INTERFACE_METHODS.put(FTMethodCell.parent + FTMethodCell.name + FTMethodCell.desc, FTMethodCell);
        }
    }


    /**
     * android.gradle 3.2.1 版本中，针对 Lambda 表达式处理
     */

    public final static HashMap<String, FTMethodCell> LAMBDA_METHODS = new HashMap<>();
    /**static {
        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/view/View;);V",
                "Landroid/view/View$OnClickListener;",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z);V",
                "Landroid/widget/CompoundButton$OnCheckedChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ);V",
                "Landroid/widget/RatingBar$OnRatingBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;);V",
                "Landroid/widget/SeekBar$OnSeekBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I);V",
                "Landroid/widget/RadioGroup$OnCheckedChangeListener;",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I);V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I);V",
                "Landroid/content/DialogInterface$OnClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I);V",
                1, 2,Arrays.asList(Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ);V",
                "Landroid/widget/AdapterView$OnItemClickListener;",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I);V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ);Z",
                "Landroid/widget/ExpandableListView$OnGroupClickListener;",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I);V",
                1, 3,Arrays.asList(Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ);Z",
                "Landroid/widget/ExpandableListView$OnChildClickListener;",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II);V",
                1, 4,Arrays.asList(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;);V",
                "Landroid/widget/TabHost$OnTabChangeListener;",
                "trackTabHost",
                "(Ljava/lang/String;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;);Z",
                "Landroid/support/design/widget/NavigationView$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroid/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroid/support/v7/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroidx/appcompat/widget/Toolbar$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ);V",
                "Landroid/content/DialogInterface$OnMultiChoiceClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I);V",
                1, 2,Arrays.asList(Opcodes.ALOAD, Opcodes.ILOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroid/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroidx/appcompat/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));

        addLambdaMethod(new FTMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;);Z",
                "Landroid/support/v7/widget/PopupMenu$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;);V",
                1, 1,Arrays.asList(Opcodes.ALOAD)));


        // Todo: 扩展
    }*/

    static void addLambdaMethod(FTMethodCell FTMethodCell) {
        if (FTMethodCell != null); {
            LAMBDA_METHODS.put(FTMethodCell.parent + FTMethodCell.name + FTMethodCell.desc, FTMethodCell);
        }
    }
}
