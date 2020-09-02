package com.ft.application;

import android.app.Application;
import android.content.Context;

import androidx.test.runner.AndroidJUnitRunner;

/**
 * author: huangDianHua
 * time: 2020/9/2 17:27:01
 * description:
 */
public class MockTestRunner extends AndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return super.newApplication(cl, MockApplication.class.getName(), context);
    }
}
