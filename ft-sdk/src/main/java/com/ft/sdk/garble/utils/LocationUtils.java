package com.ft.sdk.garble.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;

import org.json.JSONObject;

import java.net.HttpURLConnection;
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
    public static final String TAG = "LocationUtils";
    private static final String GEO_IP_ADDRESS_URL = "https://restapi.amap.com/v3/ip";
    private static final String GEO_CODE_URL = "https://restapi.amap.com/v3/geocode/regeo";
    private Context mContext;
    private static LocationUtils locationUtils;
    private LocationManager mLocationManager;
    private Address address;
    private double[] mLocation;
    //高德逆向解析API 的 key
    private String geoKey;
    //是否使用高德作为逆向地址解析
    private boolean useGeoKey;
    private Handler mHandler;
    //是否在监听位置变化
    private volatile boolean listenerIng;
    //是否正在请求地址
    private volatile boolean isRequest;

    public void setGeoKey(String geoKey) {
        this.geoKey = geoKey;
    }

    public void setUseGeoKey(boolean useGeoKey) {
        this.useGeoKey = useGeoKey;
    }

    public double[] getLocation() {
        return mLocation;
    }

    public Address getAddress() {
        return address;
    }

    private Handler getHandler() {
        return mHandler == null ? (mHandler = new Handler(Looper.getMainLooper())) : mHandler;
    }

    private LocationUtils() {
        //获取定位服务
        mLocationManager = (LocationManager) FTApplication.getApplication().getSystemService(Context.LOCATION_SERVICE);
        mContext = FTApplication.getApplication();
    }

    public static LocationUtils get() {
        if (locationUtils == null) {
            locationUtils = new LocationUtils();
        }
        return locationUtils;
    }

    /**
     * 检查当前是否有定位权限
     *
     * @return
     */
    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int state2 = mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            return state == PERMISSION_GRANTED && state2 == PERMISSION_GRANTED;
        }
        return true;
    }

//    /**
//     * 注册位置变化监听器
//     */
//    public synchronized void startListener() {
//        if (!FTMonitorConfig.get().isMonitorType(MonitorType.LOCATION)) {
//            return;
//        }
//        if (!checkLocationPermission()) {
//            LogUtils.e(TAG, "请先申请位置权限");
//            return;
//        }
//        if (listenerIng) {
//            return;
//        }
//        List<String> providers = getProviderList();
//        if (providers != null) {
//            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
//                listenerIng = true;
//                LogUtils.d(TAG, "NETWORK_PROVIDER 方式监听位置信息变化");
//                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
//            } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
//                listenerIng = true;
//                LogUtils.d(TAG, "GPS_PROVIDER 方式监听位置信息变化");
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
//            }
//        }
//        startLocationCallBack(null);
//    }

    /**
     * 移除位置变化监听器
     */
    public void stopListener() {
        listenerIng = false;
        mLocationManager.removeUpdates(locationListener);
        LogUtils.d(TAG, "停止监听位置信息变化");
    }

    /**
     * 位置信息变化监听器
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mLocation = new double[]{location.getLatitude(), location.getLongitude()};
                String string = "onLocationChanged 方式 纬度为：" + location.getLatitude() + ",经度为：" + location.getLongitude();
                LogUtils.d(TAG, string);
                getAddress(location, null);
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

    public synchronized void startLocationCallBack(AsyncCallback syncCallback) {
        if (address != null) {
            callback(syncCallback, 0, "");
            return;
        }

        if (isRequest) {
            return;
        }
        if (!checkLocationPermission()) {//没有位置权限
            LogUtils.e(TAG, "请先申请位置权限");
            if (useGeoKey && !Utils.isNullOrEmpty(geoKey)) {//如果高德 KEY 不为空尝试用高德的基于 IP 的定位
                new Thread(() -> {
                    isRequest = true;
                    requestGeoIPAddress(syncCallback);
                    isRequest = false;
                }).start();
            } else {
                callback(syncCallback, NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "未能获取到位置权限");
            }
            return;
        }
        Location location = getLastLocation();//获取最近一次的定位经纬度
        if (location != null) {
            mLocation = new double[]{location.getLatitude(), location.getLongitude()};
            String string = "getLastLocation 方式 纬度为：" + location.getLatitude() + ",经度为：" + location.getLongitude();
            LogUtils.d(TAG, string);
            getAddress(location, syncCallback);//如果经纬度不为空，则调用地址解析 API
        } else if (useGeoKey && !Utils.isNullOrEmpty(geoKey)) {//如果经纬度为空，且高德的 key 不为空，则尝试用高德 IP 定位
            new Thread(() -> {
                isRequest = true;
                requestGeoIPAddress(syncCallback);
                isRequest = false;
            }).start();
        } else {//否则提示定位失败
            callback(syncCallback, NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "未能获取到位置信息");
            LogUtils.d(TAG, "地址请求失败");
        }
    }

    private Location getLastLocation() {
        List<String> providers = getProviderList();
        if (providers != null) {
            for (String provider : providers) {
                @SuppressLint("MissingPermission")
                Location location = mLocationManager.getLastKnownLocation(provider);
                if (location != null) {
                    return location;
                }
            }
        }
        return null;
    }

    private List<String> getProviderList() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) FTApplication.getApplication().getSystemService(Context.LOCATION_SERVICE);
        }
        //获取当前可用的位置控制器
        if (mLocationManager != null) {
            return mLocationManager.getProviders(true);
        }
        return null;
    }

    public boolean isOpenGps() {
        boolean openGps = false;
        //获取定位服务
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) FTApplication.getApplication().getSystemService(Context.LOCATION_SERVICE);
        }
        //获取当前可用的位置控制器
        List<String> list = mLocationManager.getProviders(true);
        if (list.contains(LocationManager.GPS_PROVIDER)) {
            openGps = true;
        }
        return openGps;
    }

    private void getAddress(Location location, AsyncCallback syncCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isRequest = true;
                if (useGeoKey) {
                    if (Utils.isNullOrEmpty(geoKey)) {
                        LogUtils.e(TAG, "使用高德进行地址逆向解析必须先设置 key");
                    } else {
                        requestGeoAddress(location, syncCallback);
                    }
                } else {
                    requestNative(location, syncCallback);
                }
                isRequest = false;
            }
        }).start();
    }

    private void callback(AsyncCallback syncCallback, int code, String message) {
        if (syncCallback != null) syncCallback.onResponse(code, message);
    }

    /**
     * 使用系统自身API进行地址逆向解析
     * * @param location
     */
    private void requestNative(Location location, AsyncCallback syncCallback) {
        String errorMessage = "";
        int code = 0;
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(mContext, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "地址解析异常,message=" + e.getMessage());
        }

        if (result != null && !result.isEmpty() && result.get(0) != null) {
            address = result.get(0);
            code = 0;
            errorMessage = "";
        } else {
            code = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
            errorMessage = "地址解析异常";
        }

        if (address != null) {
            int finalCode = code;
            String finalErrorMessage = errorMessage;
            getHandler().post(() -> {
                callback(syncCallback, finalCode, finalErrorMessage);
            });
        }
    }

    /**
     * 通过高德 API 逆向解析地址
     *
     * @param location
     */
    private void requestGeoAddress(Location location, AsyncCallback syncCallback) {
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
                    .setUrl(GEO_CODE_URL)
                    .setMethod(RequestMethod.GET)
                    .setParams(params)
                    .useDefaultHead(false)
                    .setShowLog(false)
                    .enableToken(false)
                    .executeSync(ResponseData.class);
            //LogUtils.d(TAG,"高德逆向解析地址返回数据：\n" + responseData.getData());
            try {
                JSONObject geo = new JSONObject(responseData.getData());
                if (geo.has("regeocode")) {
                    JSONObject regeCode = geo.getJSONObject("regeocode");
                    if (regeCode.has("addressComponent")) {
                        JSONObject addressObj = regeCode.getJSONObject("addressComponent");
                        String province = null;
                        String city = null;
                        String country = null;
                        try {
                            province = addressObj.getString("province");
                            city = addressObj.getString("city");
                            country = addressObj.getString("country ");
                        } catch (Exception e) {
                        }
                        if (province != null && !"[]".equals(province)) {
                            if (city == null || "[]".equals(city)) {
                                city = province;
                            }
                            Address address = new Address(Locale.CHINA);
                            address.setAdminArea(province);
                            address.setLocality(city);
                            address.setCountryName(country);
                            this.address = address;
                        }
                    }
                }
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
        int finalCode = code;
        String finalErrorMessage = errorMessage;
        getHandler().post(() -> {
            callback(syncCallback, finalCode, finalErrorMessage);
        });
    }

    /**
     * 高德 API 通过 IP 解析地址
     */
    private void requestGeoIPAddress(AsyncCallback syncCallback) {
        String errorMessage = "";
        int code = 0;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("key", geoKey);
        ResponseData responseData = HttpBuilder.Builder()
                .setUrl(GEO_IP_ADDRESS_URL)
                .setMethod(RequestMethod.GET)
                .setParams(params)
                .useDefaultHead(false)
                .setShowLog(false)
                .enableToken(false)
                .executeSync(ResponseData.class);
        //LogUtils.d(TAG, "高德IP地址返回数据：\n" + responseData.getData());
        try {
            if (responseData.getHttpCode() == HttpURLConnection.HTTP_OK) {
                JSONObject geo = new JSONObject(responseData.getData());
                String province = geo.optString("province");
                String city = geo.optString("city");
                String rectangle = geo.optString("rectangle");
                if (!Utils.isNullOrEmpty(province) && !Utils.isNullOrEmpty(city)) {
                    Locale locale = FTApplication.getApplication().getResources().getConfiguration().locale;
                    address = new Address(locale);
                    address.setAdminArea(province);
                    address.setLocality(city);
                    address.setCountryName(locale.getDisplayCountry());
                } else {
                    code = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
                    errorMessage = responseData.getData();
                }
                if (!Utils.isNullOrEmpty(rectangle)) {
                    String[] position = rectangle.split(";");
                    if (position.length > 1) {
                        String[] locations = position[0].split(",");
                        if (locations.length == 2) {
                            mLocation = new double[]{Double.parseDouble(locations[1]), Double.parseDouble(locations[0])};
                        }
                    }
                }
            } else {
                code = responseData.getHttpCode();
                errorMessage = responseData.getData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        int finalCode = code;
        String finalErrorMessage = errorMessage;
        getHandler().post(() -> {
            callback(syncCallback, finalCode, finalErrorMessage);
        });
    }
}
