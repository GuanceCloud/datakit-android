package com.ft.sdk.garble.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;

import com.ft.sdk.FTApplication;

/**
 * create: by huangDianHua
 * time: 2020/4/24 14:58:08
 * description:
 */
public class SensorUtils {
    private static SensorUtils instance;
    private SensorManager sensorManager;

    private SensorUtils() {
        sensorManager = (SensorManager) FTApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorUtils get() {
        if (instance == null) {
            instance = new SensorUtils();
        }
        return instance;
    }

    public void getSensor() {
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        LogUtils.d(sensor.toString());
        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // alpha is calculated as t / (t + dT)
               // with t, the low-pass filter's time-constant
               // and dT, the event delivery rate
     
               final float alpha = 0.8f;
     
               gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
               gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
               gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
     
               linear_acceleration[0] = event.values[0] - gravity[0];
               linear_acceleration[1] = event.values[1] - gravity[1];
               linear_acceleration[2] = event.values[2] - gravity[2];
                LogUtils.d("timestamp:" + event.timestamp);
                for (float value : linear_acceleration) {
                    LogUtils.d("values:" + value);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        },sensor,1000*1000*10,1000*1000*10);
    }

}
