package com.ft.test.utils;

import android.os.Build;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;

/**
 *
 * @author Brandon
 */
public class TestUtils {

    /**
     * 检测是否为虚拟机设备进行测试用例
     * @return
     */
    public static boolean isEmulator() {
        try {
            Field field = Whitebox.getField(Build.class, "IS_EMULATOR");
            field.setAccessible(true);
            return (boolean) (Boolean) field.get(Build.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
