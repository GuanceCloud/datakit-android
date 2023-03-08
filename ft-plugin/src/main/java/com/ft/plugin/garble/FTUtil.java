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
package com.ft.plugin.garble;

import org.objectweb.asm.Opcodes;

import java.util.HashSet;

/**
 * BY huangDianHua
 * DATE:2019-12-03 11:43
 * Description:
 * 本类借鉴修改了来自 Sensors Data 的项目 <a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * 中的 SensorsAnalyticsUtil.groovy 类
 */
public class FTUtil {
    public static final int ASM_VERSION = Opcodes.ASM7;
    private static final HashSet<String> targetFragmentClass = new HashSet<>();
    private static final HashSet<String> targetXFragmentClass = new HashSet<>();
    private static final HashSet<String> targetV4FragmentClass = new HashSet<>();
    private static final HashSet<String> targetActivityClass = new HashSet<>();
    private static final HashSet<String> targetMenuMethodDesc = new HashSet<>();
    private static final HashSet<String> specialClass = new HashSet<>();
    private static final HashSet<String> targetApplicationClass = new HashSet<>();


    static {
        /*
         * Menu
         */
        targetMenuMethodDesc.add("onContextItemSelected(Landroid/view/MenuItem;)Z");
        targetMenuMethodDesc.add("onOptionsItemSelected(Landroid/view/MenuItem;)Z");
        targetMenuMethodDesc.add("onNavigationItemSelected(Landroid/view/MenuItem;)Z");

        /*
          For Android Application
         */

        targetApplicationClass.add("androidx/multidex/MultiDexApplication");
        targetApplicationClass.add("android/app/Application");

        /*
         * For Android App Fragment
         */
        targetFragmentClass.add("android/app/Fragment");
        targetFragmentClass.add("android/app/ListFragment");
        targetFragmentClass.add("android/app/DialogFragment");

        /*
         * For Support V4 Fragment
         */
        targetV4FragmentClass.add("android/support/v4/app/Fragment");
        targetV4FragmentClass.add("android/support/v4/app/ListFragment");
        targetV4FragmentClass.add("android/support/v4/app/DialogFragment");

        /*
         * For AndroidX Fragment
         */
        targetXFragmentClass.add("androidx/fragment/app/Fragment");
        targetXFragmentClass.add("androidx/fragment/app/ListFragment");
        targetXFragmentClass.add("androidx/fragment/app/DialogFragment");

        /*
         * For Android App Activity
         */
        targetActivityClass.add("android/app/Activity");

        /*
         * For AndroidX Activity
         */
        targetActivityClass.add("androidx/fragment/app/FragmentActivity");

        /*将一些特例需要排除在外 */
        specialClass.add("com/bumptech/glide/manager/SupportRequestManagerFragment");

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

    public static boolean isInstanceOfV4Fragment(String superName) {
        return targetV4FragmentClass.contains(superName);
    }

    public static boolean isInstanceOfActivity(String superName) {
        return targetActivityClass.contains(superName);
    }

    public static boolean isTargetClassInSpecial(String className) {
        return specialClass.contains(className);
    }

    public static boolean isInstanceOfApplication(String nameDesc) {
        return targetApplicationClass.contains(nameDesc);
    }

}
