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
     * Application中的方法
     */
    public static final HashMap<String,FTMethodCell> APPLICATION_METHODS = new HashMap<>();
    static {
        APPLICATION_METHODS.put("onCreate()V",new FTMethodCell(
                "onCreate",
                "()V",
                "startApp",
                "(Ljava/lang/Object;)V",
                Arrays.asList(
                        new FTSubMethodCell(FTMethodType.ALOAD,0),
                        new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
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

    public final static FTMethodCell CLICK_METHOD = new FTMethodCell(
            "","","trackViewOnClick","(Ljava/lang/Object;Landroid/view/View;)V",
            Arrays.asList(
                    new FTSubMethodCell(FTMethodType.ALOAD,0),
                    new FTSubMethodCell(FTMethodType.INVOKEVIRTUAL,"java/lang/Object", "getClass", "()Ljava/lang/Class;", false),
                    new FTSubMethodCell(FTMethodType.ALOAD,1)
            )
    );
}
