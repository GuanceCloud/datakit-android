package com.ft.application;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.test.runner.AndroidJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * author: huangDianHua
 * time: 2020/9/2 17:27:01
 * description: InstrumentationRunner 应用 app-build.gradle defaultConfig.testInstrumentationRunner 中应用
 */
public class MockTestRunner extends AndroidJUnitRunner {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return super.newApplication(cl, MockApplication.class.getName(), context);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        try {
            Class<?> rt = Class.forName("org.jacoco.agent.rt.RT");
            Method getAgent = rt.getMethod("getAgent");
            Method dump = getAgent.getReturnType().getMethod("dump", boolean.class);
            Object agent = getAgent.invoke(null);
            dump.invoke(agent, false);
        } catch (Throwable e) {
            final String trace = Log.getStackTraceString(e);

            try {
                System.out.write(trace.getBytes(UTF8));
            } catch (IOException ignored) {
            }
        }

        super.finish(resultCode, results);
    }
}
