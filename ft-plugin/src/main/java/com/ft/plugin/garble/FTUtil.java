package com.ft.plugin.garble;

import org.objectweb.asm.Opcodes;

import java.util.HashSet;

/**
 * BY huangDianHua
 * DATE:2019-12-03 11:43
 * Description:
 */
public class FTUtil {
    public static final int ASM_VERSION = Opcodes.ASM6;
    private static final HashSet<String> targetFragmentClass = new HashSet<>();
    private static final HashSet<String> targetXFragmentClass = new HashSet<>();
    private static final HashSet<String> targetActivityClass = new HashSet<>();
    private static final HashSet<String> targetMenuMethodDesc = new HashSet<>();
    private static final HashSet<String> specialClass = new HashSet<>();

    static {
        /**
         * Menu
         */
        targetMenuMethodDesc.add("onContextItemSelected(Landroid/view/MenuItem;);Z");
        targetMenuMethodDesc.add("onOptionsItemSelected(Landroid/view/MenuItem;);Z");
        targetMenuMethodDesc.add("onNavigationItemSelected(Landroid/view/MenuItem;);Z");

        /**
         * For Android App Fragment
         */
        targetFragmentClass.add("android/app/Fragment");
        targetFragmentClass.add("android/app/ListFragment");
        targetFragmentClass.add("android/app/DialogFragment");

        /**
         * For Support V4 Fragment
         */
        targetFragmentClass.add("android/support/v4/app/Fragment");
        targetFragmentClass.add("android/support/v4/app/ListFragment");
        targetFragmentClass.add("android/support/v4/app/DialogFragment");

        /**
         * For AndroidX Fragment
         */
        targetXFragmentClass.add("androidx/fragment/app/Fragment");
        targetXFragmentClass.add("androidx/fragment/app/ListFragment");
        targetXFragmentClass.add("androidx/fragment/app/DialogFragment");

        /**
         * For Android App Activity
         */
        targetActivityClass.add("android/app/activity");

        /**
         * For AndroidX Activity
         */
        targetActivityClass.add("androidx/appcompat/app/AppCompatActivity");

        /** 将一些特例需要排除在外 */
        specialClass.add("android.support.design.widget.TabLayout$ViewPagerOnTabSelectedListener");
        specialClass.add("com.google.android.material.tabs.TabLayout$ViewPagerOnTabSelectedListener");
        specialClass.add("android.support.v7.app.ActionBarDrawerToggle");
        specialClass.add("androidx.appcompat.app.ActionBarDrawerToggle");

    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0;
    }

    public static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isTargetMenuMethodDesc(String nameDesc) {
        return targetMenuMethodDesc.contains(nameDesc);
    }

    public static boolean isInstanceOfFragment(String superName) {
        return targetFragmentClass.contains(superName);
    }

    public static boolean isInstanceOfXFragment(String superName) {
        return targetXFragmentClass.contains(superName);
    }

    public static boolean isInstanceOfActivity(String superName) {
        return targetActivityClass.contains(superName);
    }

    public static boolean isTargetClassInSpecial(String className) {
        return specialClass.contains(className);
    }

}
