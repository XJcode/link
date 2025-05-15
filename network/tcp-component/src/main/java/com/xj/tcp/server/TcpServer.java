package com.xj.tcp.server;

import com.xj.tcp.client.DeviceInfo;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class TcpServer {
    private final Vertx vertx;
    private final DeviceManager deviceManager = new DeviceManager();

    public TcpServer(Vertx vertx) {
        this.vertx = vertx;
    }

    public void start(int port) {
        NetServer server = vertx.createNetServer();
        server.connectHandler(this::handleConnection);
        server.listen(port, res -> {
            if (res.succeeded()) {
                System.out.println("Server started on port " + port);
                startHeartbeatCheck(); // 启动心跳检测
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    // 处理新连接
    private void handleConnection(NetSocket socket) {
        socket.handler(buffer -> {
            BaseMessage message = decodeMessage(buffer); // 解码消息
            if (message instanceof HeartbeatMessage) {
                handleHeartbeat((HeartbeatMessage) message, socket);
            }
        });

        socket.closeHandler(v -> {
            String deviceId = getDeviceIdBySocket(socket); // 根据socket查找设备ID
            deviceManager.removeDevice(deviceId);
        });
    }

    // 解码消息（示例）
    private BaseMessage decodeMessage(Buffer buffer) {
        // 实际解析协议，例如 JSON 或二进制协议
        return new HeartbeatMessage("device-001"); // 示例
    }

    // 处理心跳
    private void handleHeartbeat(HeartbeatMessage message, NetSocket socket) {
        String deviceId = message.getDeviceId();
        DeviceInfo device = new DeviceInfo(deviceId, socket, System.currentTimeMillis(), true);
        deviceManager.addDevice(device);
        deviceManager.updateActiveTime(deviceId);
    }

    // 心跳检测定时任务
    private void startHeartbeatCheck() {
        vertx.setPeriodic(60_000, timerId -> {
            long currentTime = System.currentTimeMillis();
            deviceManager.getAllDevices().forEach(device -> {
                if (currentTime - device.getLastActiveTime() > 120_000) { // 超过120秒无心跳
                    device.getSocket().close(); // 关闭连接
                    deviceManager.removeDevice(device.getDeviceId());
                }
            });
        });
    }

    // 向指定设备发送数据
    public void sendToDevice(String deviceId, String data) {
        DeviceInfo device = deviceManager.getDevice(deviceId);
        if (device != null && device.isOnline()) {
            device.getSocket().write(data);
        }
    }
}
