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
import android.os.Handler;

import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;

import org.json.JSONObject;

import java.util.HashMap;
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
    //高德逆向解析API 的 key
    private String geoKey;
    //是否使用高德作为逆向地址解析
    private boolean useGeoKey;
    private Handler mHandler;

    private LocationUtils() {
    }

    public static LocationUtils get() {
        if (locationUtils == null) {
            locationUtils = new LocationUtils();
        }
        return locationUtils;
    }

    public String getGeoKey() {
        return geoKey;
    }

    public void setGeoKey(String geoKey) {
        this.geoKey = geoKey;
    }

    public boolean isUseGeoKey() {
        return useGeoKey;
    }

    public void setUseGeoKey(boolean useGeoKey) {
        this.useGeoKey = useGeoKey;
    }

    /**
     * 获取所在地址
     *
     * @return
     */
    public Address getCity() {
        return address;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                String string = "纬度为：" + location.getLatitude() + ",经度为：" + location.getLongitude();
                LogUtils.d(string);
                getAddress(mContext, location, null);
                if (address != null) {
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

    public void startLocation(Context context) {
        if (!FTMonitorConfig.get().isMonitorType(MonitorType.ALL) && !FTMonitorConfig.get().isMonitorType(MonitorType.LOCATION)) {
            return;
        }
        startLocationCallBack(context, null);
    }

    public void startLocationCallBack(Context context, SyncCallback syncCallback) {
        if (address != null) {
            callback(syncCallback, 0, "");
            return;
        }
        if (syncCallback != null) {
            mHandler = new Handler();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int state2 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (state != PERMISSION_GRANTED || state2 != PERMISSION_GRANTED) {
                LogUtils.e("请先申请位置权限");
                callback(syncCallback, NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "未能获取到位置权限");
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
        if (mProvider != null) {
            Location location = mLocationManager.getLastKnownLocation(mProvider);
            if (location != null) {
                String string = "纬度为：" + location.getLatitude() + ",经度为：" + location.getLongitude();
                LogUtils.d(string);
                getAddress(context, location, syncCallback);
            } else {
                mLocationManager.requestLocationUpdates(mProvider, 3000, 1, locationListener);
            }
        }
    }

    private void getAddress(Context context, Location location, SyncCallback syncCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //用来接收位置的详细信息
                if (useGeoKey) {
                    if (geoKey == null || geoKey.isEmpty()) {
                        LogUtils.e("使用高德进行地址逆向解析必须先设置 key");
                    } else {
                        requestGeoAddress(location, syncCallback);
                    }
                } else {
                    requestNative(context, location, syncCallback);
                }
            }
        }).start();
    }

    private void callback(SyncCallback syncCallback, int code, String message) {
        if (syncCallback != null) syncCallback.onResponse(code, message);
    }

    /**
     * 使用系统自身API进行地址逆向解析
     *
     * @param context
     * @param location
     */
    private int requestNative(Context context, Location location, SyncCallback syncCallback) {
        String errorMessage = "";
        int code = 0;
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(context, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            LogUtils.e("地址解析异常,message=" + e.getMessage());
        }

        if (result != null && !result.isEmpty() && result.get(0) != null) {
            address = result.get(0);
            code = 0;
            errorMessage = "";
            LogUtils.d("地址[province:" + address.getAdminArea() + ",city:" + address.getLocality());
        } else {
            code = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
            errorMessage = "地址解析异常";
        }

        if (mHandler != null && address != null) {
            int finalCode = code;
            String finalErrorMessage = errorMessage;
            mHandler.post(() -> {
                callback(syncCallback, finalCode, finalErrorMessage);
            });
        }
        return code;
    }

    /**
     * 通过 高德 API 逆向解析地址
     *
     * @param location
     */
    private int requestGeoAddress(Location location, SyncCallback syncCallback) {
        String errorMessage = "";
        int code = 0;
        if (location == null) {
            code = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
            errorMessage = "未能获取到位置信息";
        } else if (!Utils.isNetworkAvailable()) {
            code = NetCodeStatus.NETWORK_EXCEPTION_CODE;
            errorMessage = "未能连接到网络";
        } else {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("location", location.getLongitude() + "," + location.getLatitude());
            params.put("key", geoKey);
            params.put("radius", 1000);
            ResponseData responseData = HttpBuilder.Builder()
                    .setUrl("https://restapi.amap.com/v3/geocode/regeo")
                    .setMethod(RequestMethod.GET)
                    .setParams(params)
                    .useDefaultHead(false)
                    .setShowLog(false)
                    .executeSync(ResponseData.class);
            LogUtils.d("高德逆向解析地址返回数据：\n" + responseData.getData());
            try {
                JSONObject geo = new JSONObject(responseData.getData());
                if (geo.has("regeocode")) {
                    JSONObject regeCode = geo.getJSONObject("regeocode");
                    if (regeCode.has("addressComponent")) {
                        JSONObject addressObj = regeCode.getJSONObject("addressComponent");
                        String province = null;
                        String city = null;
                        try {
                            province = addressObj.getString("province");
                            city = addressObj.getString("city");
                        } catch (Exception e) {
                        }
                        if (province != null && !"[]".equals(province)) {
                            if (city == null || "[]".equals(city)) {
                                city = province;
                            }
                            Address address = new Address(Locale.CHINA);
                            address.setAdminArea(province);
                            address.setLocality(city);
                            this.address = address;
                        }
                    }
                }
                LogUtils.d("Address:" + address.toString());
                if (address == null) {
                    code = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
                    errorMessage = "解析数据异常";
                } else {
                    code = 0;
                    errorMessage = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mHandler != null) {
            int finalCode = code;
            String finalErrorMessage = errorMessage;
            mHandler.post(() -> {
                callback(syncCallback, finalCode, finalErrorMessage);
            });
        }
        return code;
    }
}
