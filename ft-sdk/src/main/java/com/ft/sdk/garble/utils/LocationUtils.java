package com.ft.sdk.garble.utils;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTMonitorConfig;

import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * BY huangDianHua
 * DATE:2020-01-09 16:09
 * Description: 位置获取
 */
public class LocationUtils {
    private Context mContext;
    private static LocationUtils locationUtils;
    private LocationManager mLocationManager;
    private String mProvider;
    private Address address;
    private LocationUtils(){ }
    public static LocationUtils get(){
        if(locationUtils == null){
            locationUtils = new LocationUtils();
        }
        return locationUtils;
    }

    /**
     * 获取所在地址
     * @return
     */
    public Address getCity(){
        return address;
    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null){
                String string = "纬度为：" + location.getLatitude() + ",经度为："+ location.getLongitude();
                LogUtils.d(string);
                getAddress(mContext,location);
                if(address != null){
                    mLocationManager.removeUpdates(locationListener);
                }
            }
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };

    public void startLocation(Context context){
        if(!FTMonitorConfig.get().isMonitorType(MonitorType.ALL) && !FTMonitorConfig.get().isMonitorType(MonitorType.LOCATION)){
            return;
        }
        if(address != null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int state2 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(state != PERMISSION_GRANTED || state2 != PERMISSION_GRANTED){
                LogUtils.e("请先申请位置权限");
                return;
            }
        }
        mContext = context;
        mProvider = null;
        //获取定位服务
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = mLocationManager.getProviders(true);
        if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            mProvider = LocationManager.NETWORK_PROVIDER;
        } else if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            mProvider = LocationManager.GPS_PROVIDER;
        } else if (list.contains(LocationManager.PASSIVE_PROVIDER)) {
            mProvider = LocationManager.PASSIVE_PROVIDER;
        }
        if(mProvider != null){
            Location location = mLocationManager.getLastKnownLocation(mProvider);
            if(location!=null){
                String string = "纬度为：" + location.getLatitude() + ",经度为："+ location.getLongitude();
                getAddress(context,location);
            }else{
                mLocationManager.requestLocationUpdates(mProvider, 3000, 1, locationListener);
            }
        }
    }

    private void getAddress(Context context,Location location){
        //用来接收位置的详细信息
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(context, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            LogUtils.e("地址解析异常,message="+e.getMessage());
        }

        if(result != null && !result.isEmpty() && result.get(0) != null){
            address = result.get(0);
            LogUtils.d("地址[province:"+address.getAdminArea()+",city:"+address.getLocality());
        }
    }
}
