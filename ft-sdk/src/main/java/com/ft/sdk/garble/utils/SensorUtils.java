package com.ft.sdk.garble.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.FTMonitorConfig;

import java.util.ArrayList;
import java.util.List;

import static com.ft.sdk.MonitorType.SENSOR;
import static com.ft.sdk.MonitorType.SENSOR_ACCELERATION;
import static com.ft.sdk.MonitorType.SENSOR_BRIGHTNESS;
import static com.ft.sdk.MonitorType.SENSOR_MAGNETIC;
import static com.ft.sdk.MonitorType.SENSOR_PROXIMITY;
import static com.ft.sdk.MonitorType.SENSOR_ROTATION;
import static com.ft.sdk.MonitorType.SENSOR_STEP;

/**
 * create: by huangDianHua
 * time: 2020/4/24 14:58:08
 * description:
 */
public class SensorUtils {
    private static SensorUtils instance;
    private SensorManager sensorManager;
    //光线强度
    private float sensorLight;
    //步数
    private float stepCounter;
    //今天步数
    private float todayStep;
    //近程传感器
    private float distance;
    //线性加速度
    private float[] acceleration;
    //陀螺仪
    private float[] gyroscope;
    //地磁场
    private float[] magnetic;
    private List<Sensor> sensorList;


    private SensorUtils() {
        sensorManager = (SensorManager) FTApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorUtils get() {
        if (instance == null) {
            instance = new SensorUtils();
        }
        return instance;
    }

    public void register() {
        if (sensorList == null) {
            sensorList = new ArrayList<>();
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_BRIGHTNESS)) {
            sensorList.add(registerSensor(Sensor.TYPE_LIGHT));
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_STEP)) {
            sensorList.add(registerSensor(Sensor.TYPE_STEP_COUNTER));
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_PROXIMITY)) {
            sensorList.add(registerSensor(Sensor.TYPE_PROXIMITY));
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_ACCELERATION)) {
            sensorList.add(registerSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_ROTATION)) {
            sensorList.add(registerSensor(Sensor.TYPE_GYROSCOPE));
        }
        if (FTMonitorConfig.get().isMonitorType(SENSOR) || FTMonitorConfig.get().isMonitorType(SENSOR_MAGNETIC)) {
            sensorList.add(registerSensor(Sensor.TYPE_MAGNETIC_FIELD));
        }
    }

    private Sensor registerSensor(int type) {
        //距离传感器，一般值为物体离传感器的距离（单位cm），有些是返回近距离，和远距离的值。
        Sensor sensor = sensorManager.getDefaultSensor(type);
        sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return sensor;
    }

    private SensorEventListener eventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    sensorLight = event.values[0];
                } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    stepCounter = event.values[0];
                } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    distance = event.values[0];
                } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    acceleration = event.values;
                } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gyroscope = event.values;
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    magnetic = event.values;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public float getTodayStep() {
        calculateStep();
        return todayStep;
    }

    public float getSensorLight() {
        return sensorLight;
    }

    public float getDistance() {
        return distance;
    }

    public float[] getAcceleration() {
        return acceleration;
    }

    public float[] getGyroscope() {
        return gyroscope;
    }

    public float[] getMagnetic() {
        return magnetic;
    }

    private void calculateStep() {
        String dateCurrent = Utils.getDateString();
        String dateLast = Utils.querySharePreference(Constants.SHARE_PRE_STEP_DATE, String.class, "");
        if (Utils.isNullOrEmpty(dateLast)) {
            Utils.saveSharePreference(Constants.SHARE_PRE_STEP_DATE, dateCurrent);
            Utils.saveSharePreference(Constants.SHARE_PRE_STEP_HISTORY, stepCounter);
        }
        if (!dateCurrent.equals(dateLast)) {
            Utils.saveSharePreference(Constants.SHARE_PRE_STEP_HISTORY, stepCounter);
            Utils.saveSharePreference(Constants.SHARE_PRE_STEP_DATE, dateCurrent);
        } else {
            float historyStep = Utils.querySharePreference(Constants.SHARE_PRE_STEP_HISTORY, Float.class, 0f);
            float todayStep = stepCounter - historyStep;
            if (todayStep < 0) {
                Utils.saveSharePreference(Constants.SHARE_PRE_STEP_HISTORY, stepCounter);
                this.todayStep = 0;
            } else {
                this.todayStep = todayStep;
            }
        }
    }

    public void release() {
        for (Sensor sensor : sensorList) {
            if (sensorManager != null) {
                sensorManager.unregisterListener(eventListener, sensor);
            }
        }
    }
}
