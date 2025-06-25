package com.example.myandroidapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationInfoHelper {
    private static final String TAG = "LocationInfoHelper";

    public static String getDeviceInfo(Context context) {
        StringBuilder info = new StringBuilder();
        info.append("📱 设备信息:\n");
        
        // 获取网络状态
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        info.append("网络连接: ").append(isConnected ? "已连接" : "未连接").append("\n");
        if (isConnected) {
            info.append("网络类型: ").append(activeNetwork.getTypeName()).append("\n");
        }
        
        // 获取运营商信息
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String operatorName = tm.getNetworkOperatorName();
            if (operatorName != null && !operatorName.isEmpty()) {
                info.append("运营商: ").append(operatorName).append("\n");
            }
        }
        
        info.append("\n");
        return info.toString();
    }

    public static String getLocationProviderInfo(LocationManager locationManager) {
        StringBuilder info = new StringBuilder();
        info.append("📡 位置提供者信息:\n");
        
        // GPS提供者
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        info.append("GPS: ").append(isGPSEnabled ? "✅ 可用" : "❌ 不可用").append("\n");
        
        // 网络提供者
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        info.append("网络定位: ").append(isNetworkEnabled ? "✅ 可用" : "❌ 不可用").append("\n");
        
        // 被动提供者
        boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        info.append("被动定位: ").append(isPassiveEnabled ? "✅ 可用" : "❌ 不可用").append("\n");
        
        info.append("\n");
        return info.toString();
    }

    public static String formatLocationInfo(Location location) {
        if (location == null) {
            return "❌ 位置信息为空";
        }
        
        StringBuilder info = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        info.append("📍 详细坐标信息:\n");
        info.append("纬度: ").append(String.format(Locale.getDefault(), "%.6f", location.getLatitude())).append("°\n");
        info.append("经度: ").append(String.format(Locale.getDefault(), "%.6f", location.getLongitude())).append("°\n");
        info.append("精度: ").append(location.getAccuracy()).append(" 米\n");
        
        if (location.hasAltitude()) {
            info.append("海拔: ").append(String.format(Locale.getDefault(), "%.1f", location.getAltitude())).append(" 米\n");
        }
        
        if (location.hasSpeed()) {
            double speedMps = location.getSpeed();
            double speedKph = speedMps * 3.6; // 转换为公里/小时
            info.append("速度: ").append(String.format(Locale.getDefault(), "%.1f", speedKph)).append(" km/h\n");
        }
        
        if (location.hasBearing()) {
            info.append("方向: ").append(String.format(Locale.getDefault(), "%.1f", location.getBearing())).append("°\n");
        }
        
        info.append("时间: ").append(sdf.format(new Date(location.getTime()))).append("\n");
        info.append("提供者: ").append(location.getProvider()).append("\n");
        
        if (location.isFromMockProvider()) {
            info.append("⚠️ 模拟位置: 是\n");
        }
        
        info.append("\n");
        return info.toString();
    }

    public static String getLocationAccuracyDescription(float accuracy) {
        if (accuracy <= 5) {
            return "极高精度 (< 5米)";
        } else if (accuracy <= 10) {
            return "高精度 (5-10米)";
        } else if (accuracy <= 20) {
            return "中等精度 (10-20米)";
        } else if (accuracy <= 50) {
            return "一般精度 (20-50米)";
        } else {
            return "低精度 (> 50米)";
        }
    }

    public static String getDirectionDescription(float bearing) {
        String[] directions = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int index = (int) Math.round(bearing / 45) % 8;
        return directions[index] + " (" + String.format(Locale.getDefault(), "%.1f", bearing) + "°)";
    }
} 