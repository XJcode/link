package com.xj.tcp.server;

import com.xj.tcp.client.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeviceManager {

    private final ConcurrentMap<String, DeviceInfo> devices = new ConcurrentHashMap<>();

    // 添加设备
    public void addDevice(DeviceInfo device) {
        devices.put(device.getDeviceId(), device);
    }

    // 移除设备
    public void removeDevice(String deviceId) {
        devices.remove(deviceId);
    }

    // 获取设备
    public DeviceInfo getDevice(String deviceId) {
        return devices.get(deviceId);
    }

    // 获取所有在线设备
    public List<DeviceInfo> getAllDevices() {
        return new ArrayList<>(devices.values());
    }

    // 更新设备活跃时间
    public void updateActiveTime(String deviceId) {
        DeviceInfo device = devices.get(deviceId);
        if (device != null) {
            device.setLastActiveTime(System.currentTimeMillis());
        }
    }
}
