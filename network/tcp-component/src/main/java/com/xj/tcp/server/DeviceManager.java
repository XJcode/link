package com.xj.tcp.server;

import com.xj.tcp.client.DeviceInfo;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeviceManager {

//    private final ConcurrentMap<String, DeviceInfo> devices = new ConcurrentHashMap<>();

//    // 添加设备
//    public void addDevice(DeviceInfo device) {
//        devices.put(device.getDeviceId(), device);
//    }

//    // 移除设备
//    public void removeDevice(String deviceId) {
//        devices.remove(deviceId);
//    }

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

    // 设备ID -> 设备信息
    private final ConcurrentMap<String, DeviceInfo> devices = new ConcurrentHashMap<>();
    // Socket -> 设备ID（反向映射）
    private final ConcurrentMap<NetSocket, String> socketToDeviceId = new ConcurrentHashMap<>();

    // 添加设备（更新双向映射）
    public void addDevice(DeviceInfo device) {
        devices.put(device.getDeviceId(), device);
        socketToDeviceId.put(device.getSocket(), device.getDeviceId());
    }

    // 移除设备（清理双向映射）
    public void removeDevice(String deviceId) {
        DeviceInfo device = devices.remove(deviceId);
        if (device != null) {
            socketToDeviceId.remove(device.getSocket());
        }
    }

    // 通过 Socket 获取设备ID
    public String getDeviceIdBySocket(NetSocket socket) {
        return socketToDeviceId.get(socket);
    }
}
