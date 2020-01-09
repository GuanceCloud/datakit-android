package com.ft.sdk.garble.utils;

import java.security.Permission;
import java.security.Permissions;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Build;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * from https://blog.csdn.net/giantgreen/article/details/33387661
 */
public class CameraUtils {

    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_NONE = -1;

    public static int HasBackCamera()
    {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACING_BACK) {
                return i;
            }
        }
        return CAMERA_NONE;
    }

    public static int HasFrontCamera()
    {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return CAMERA_NONE;
    }

    public static String getCameraPixels(Context context, int paramInt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = context.checkSelfPermission(Manifest.permission.CAMERA);
            if(state != PERMISSION_GRANTED){
                LogUtils.e("请先申请相机权限");
                return "N/A";
            }
        }
        if (paramInt == CAMERA_NONE)
            return "N/A";
        Camera localCamera = Camera.open(paramInt);
        Camera.Parameters localParameters = localCamera.getParameters();
        localParameters.set("camera-id", 1);
        List<Size> localList = localParameters.getSupportedPictureSizes();
        if (localList != null)
        {
            int heights[] = new int[localList.size()];
            int widths[] = new int[localList.size()];
            for (int i = 0; i < localList.size(); i++)
            {
                Size size = localList.get(i);
                int sizehieght = size.height;
                int sizewidth = size.width;
                heights[i] = sizehieght;
                widths[i] =sizewidth;
            }
            int pixels = getMaxNumber(heights) * getMaxNumber(widths);
            localCamera.release();
            return (pixels / 10000) + "万";

        }
        else return "N/A";

    }

    public static int getMaxNumber(int[] paramArray) {
        int temp = paramArray[0];
        for(int i = 0;i<paramArray.length;i++)
        {
            if(temp < paramArray[i])
            {
                temp = paramArray[i];
            }
        }
        return temp;
    }
}
