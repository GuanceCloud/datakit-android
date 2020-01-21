package com.ft.sdk.garble.bean;

import android.hardware.camera2.CameraCharacteristics;

import androidx.annotation.NonNull;

/**
 * BY huangDianHua
 * DATE:2020-01-14 13:39
 * Description:
 */
public class CameraPx{
    public int face;
    public long px;
    public String id;


    public String[] getPx() {
        String pxStr = px==0?"N/A":""+px;
        if(face == CameraCharacteristics.LENS_FACING_FRONT){
            return new String[]{"camera_front"+id+"_px",pxStr+"万像素"};
        }else{
            return new String[]{"camera_back"+id+"_px",pxStr+"万像素"};
        }
    }
}
