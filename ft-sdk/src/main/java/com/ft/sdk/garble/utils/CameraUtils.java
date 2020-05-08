package com.ft.sdk.garble.utils;


import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.CameraPx;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CameraUtils {
    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_NONE = -1;

    private List<CameraPx> cameraPxList = null;
    private CameraManager.TorchCallback torchCallback;
    private CameraManager cameraManager;

    private boolean torchState;
    private static CameraUtils cameraUtils;

    public boolean isTorchState() {
        return torchState;
    }

    private CameraUtils(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            torchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(@NonNull String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                }

                @Override
                public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    torchState = enabled;
                }
            };
        }
    }
    public static CameraUtils get(){
        if(cameraUtils == null){
            cameraUtils = new CameraUtils();
        }
        return cameraUtils;
    }

    public void release(){
        if (cameraManager != null && torchCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.unregisterTorchCallback(torchCallback);
            }
        }
        cameraUtils = null;
    }

    /**
     * 获取手机的相机像素信息（多个摄像头）
     * @param context
     * @return
     */
    public List<CameraPx> getCameraPxList(Context context){
        if(cameraPxList != null && !cameraPxList.isEmpty()){
            return cameraPxList;
        }
        // 大于 21 版本使用 Camera2 的API
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            cameraPxList = getCamera2IdList(context);
        }else{
            cameraPxList = getCameraIdList(context);
        }
        return cameraPxList;
    }
    /**
     * 通过 Camera2 获取相机的像素
     * @param context
     * @return
     */
    private List<CameraPx> getCamera2IdList(Context context){
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        List<CameraPx> cameraPxes = new ArrayList<>();
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                StreamConfigurationMap stream = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int face = characteristics.get(CameraCharacteristics.LENS_FACING);
                Size[] sizes = stream.getOutputSizes(ImageFormat.JPEG);
                int[] heights = new int[sizes.length];
                int[] widths = new int[sizes.length];
                for (int i = 0; i < sizes.length; i++) {
                    heights[i] = sizes[i].getHeight();
                    widths[i] = sizes[i].getWidth();
                }
                int px = getMaxNumber(heights) * getMaxNumber(widths);
                CameraPx cameraPx = new CameraPx();
                cameraPx.face = face;
                cameraPx.px = px/10000;
                cameraPx.id = id;
                cameraPxes.add(cameraPx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cameraPxes;
    }

    /**
     *  通过 Camera API 获取相机像素
     * @param context
     * @return
     */
    private List<CameraPx> getCameraIdList(Context context){
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        List<CameraPx> cameraPxes = new ArrayList<>();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            CameraPx cameraPx = new CameraPx();
            if (cameraInfo.facing == CAMERA_FACING_BACK) {
                cameraPx.face = CameraCharacteristics.LENS_FACING_BACK;
            }else{
                cameraPx.face = CameraCharacteristics.LENS_FACING_FRONT;
            }
            cameraPx.id = i+"";
            cameraPx.px = getCameraPixels(context,i);
            cameraPxes.add(cameraPx);
        }
        return cameraPxes;
    }

    /**
     * 获取某一个相机的像素
     * @param context
     * @param paramInt
     * @return
     */
    private long getCameraPixels(Context context, int paramInt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = context.checkSelfPermission(Manifest.permission.CAMERA);
            if (state != PERMISSION_GRANTED) {
                LogUtils.e("请先申请相机权限");
                return 0;
            }
        }
        if (paramInt == CAMERA_NONE)
            return 0;
        Camera localCamera = Camera.open(paramInt);
        Camera.Parameters localParameters = localCamera.getParameters();
        localParameters.set("camera-id", 1);
        List<Camera.Size> localList = localParameters.getSupportedPictureSizes();
        if (localList != null) {
            int[] heights = new int[localList.size()];
            int[] widths = new int[localList.size()];
            for (int i = 0; i < localList.size(); i++) {
                Camera.Size size = localList.get(i);
                int sizeHeight = size.height;
                int sizeWidth = size.width;
                heights[i] = sizeHeight;
                widths[i] = sizeWidth;
            }
            int pixels = getMaxNumber(heights) * getMaxNumber(widths);
            localCamera.release();
            return (pixels / 10000);

        } else return 0;

    }

    /**
     * 获取数组中的最大值
     * @param paramArray
     * @return
     */
    private int getMaxNumber(int[] paramArray) {
        int temp = paramArray[0];
        for (int i = 0; i < paramArray.length; i++) {
            if (temp < paramArray[i]) {
                temp = paramArray[i];
            }
        }
        return temp;
    }

    /**
     * 获得闪光灯状态
     * @return
     */
    public void start(){
        if(cameraManager == null){
            cameraManager = (CameraManager) FTApplication.getApplication().getSystemService(Context.CAMERA_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && torchCallback != null && cameraManager != null) {
            cameraManager.registerTorchCallback(torchCallback,null);

        }else {
            Camera camera = Camera.open();
            Camera.Parameters p = camera.getParameters();
            String state = p.getFlashMode();
            camera.release();
            if ("null".equals(state)) {
                torchState =  false;
            } else if ("off".equals(state)) {
                torchState = false;
            } else {
                torchState = true;
            }
        }
    }
}
