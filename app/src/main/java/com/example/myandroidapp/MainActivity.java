package com.example.myandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView locationInfoText;
    private Button getLocationButton;
    private LocationManager locationManager;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationInfoText = findViewById(R.id.locationInfoText);
        getLocationButton = findViewById(R.id.getLocationButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        getLocationButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                getLocationInfo();
            } else {
                requestPermissions();
            }
        });

        // 显示初始信息
        showInitialInfo();
    }

    private void showInitialInfo() {
        StringBuilder info = new StringBuilder();
        info.append("📱 手机定位信息应用\n\n");
        info.append("功能说明:\n");
        info.append("• 获取GPS和网络定位信息\n");
        info.append("• 显示详细坐标和地址信息\n");
        info.append("• 实时位置更新\n");
        info.append("• 设备网络状态\n\n");
        info.append("点击下方按钮开始获取位置信息...");
        
        locationInfoText.setText(info.toString());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
            PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationInfo();
            } else {
                Toast.makeText(this, "需要位置权限才能获取位置信息", Toast.LENGTH_LONG).show();
                showPermissionDeniedInfo();
            }
        }
    }

    private void showPermissionDeniedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("❌ 权限被拒绝\n\n");
        info.append("请在设置中手动授予位置权限:\n");
        info.append("设置 → 应用 → 手机定位信息 → 权限 → 位置");
        locationInfoText.setText(info.toString());
    }

    private void getLocationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🔄 正在获取位置信息...\n\n");

        // 获取设备信息
        info.append(LocationInfoHelper.getDeviceInfo(this));

        // 获取位置提供者信息
        info.append(LocationInfoHelper.getLocationProviderInfo(locationManager));

        // 检查是否有可用的位置提供者
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            info.append("❌ 无法获取位置信息\n");
            info.append("请启用GPS或网络定位\n");
            info.append("设置 → 位置信息 → 开启位置服务");
            locationInfoText.setText(info.toString());
            return;
        }

        try {
            // 获取最后已知位置
            Location lastKnownLocation = null;
            if (isGPSEnabled) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (lastKnownLocation == null && isNetworkEnabled) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                // 使用辅助类格式化位置信息
                info.append(LocationInfoHelper.formatLocationInfo(lastKnownLocation));

                // 获取地址信息
                String address = getAddressFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                info.append("🏠 地址信息:\n").append(address).append("\n\n");

                // 添加精度描述
                info.append("📊 精度评估:\n");
                info.append(LocationInfoHelper.getLocationAccuracyDescription(lastKnownLocation.getAccuracy())).append("\n\n");

                // 添加方向描述（如果有）
                if (lastKnownLocation.hasBearing()) {
                    info.append("🧭 方向:\n");
                    info.append(LocationInfoHelper.getDirectionDescription(lastKnownLocation.getBearing())).append("\n\n");
                }

            } else {
                info.append("❌ 无法获取位置信息\n");
                info.append("可能的原因:\n");
                info.append("• GPS信号弱\n");
                info.append("• 网络连接不稳定\n");
                info.append("• 位置服务刚开启，需要等待\n\n");
                info.append("正在请求位置更新...");
            }

            // 请求位置更新
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, this);
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, this);
            }

        } catch (SecurityException e) {
            info.append("❌ 权限错误: ").append(e.getMessage());
        }

        locationInfoText.setText(info.toString());
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                
                // 尝试获取完整地址
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (address.getAddressLine(i) != null) {
                        addressText.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) {
                            addressText.append("\n");
                        }
                    }
                }
                
                // 如果没有完整地址，显示基本信息
                if (addressText.length() == 0) {
                    if (address.getCountryName() != null) {
                        addressText.append("国家: ").append(address.getCountryName());
                    }
                    if (address.getAdminArea() != null) {
                        addressText.append("\n省份: ").append(address.getAdminArea());
                    }
                    if (address.getLocality() != null) {
                        addressText.append("\n城市: ").append(address.getLocality());
                    }
                    if (address.getSubLocality() != null) {
                        addressText.append("\n区域: ").append(address.getSubLocality());
                    }
                    if (address.getThoroughfare() != null) {
                        addressText.append("\n街道: ").append(address.getThoroughfare());
                    }
                    if (address.getSubThoroughfare() != null) {
                        addressText.append("\n门牌: ").append(address.getSubThoroughfare());
                    }
                }
                
                return addressText.toString();
            }
        } catch (IOException e) {
            return "无法获取地址信息: " + e.getMessage();
        }
        return "无法获取地址信息";
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // 位置更新时的回调
        StringBuilder info = new StringBuilder();
        info.append("🔄 位置已更新!\n\n");
        
        // 使用辅助类格式化位置信息
        info.append(LocationInfoHelper.formatLocationInfo(location));

        String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
        info.append("🏠 地址信息:\n").append(address).append("\n");

        // 添加精度描述
        info.append("📊 精度评估:\n");
        info.append(LocationInfoHelper.getLocationAccuracyDescription(location.getAccuracy())).append("\n");

        locationInfoText.setText(info.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
} 