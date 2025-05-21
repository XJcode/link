package com.xj.tcp.server;

import com.alibaba.fastjson2.JSON;
import com.xj.tcp.client.DeviceInfo;
import com.xj.tcp.message.BaseMessage;
import com.xj.tcp.message.HeartbeatMessage;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer {

    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
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
            logger.info(deviceManager.getDeviceIdBySocket(socket));
        });

        socket.closeHandler(v -> {
            String deviceId = deviceManager.getDeviceIdBySocket(socket); // 根据socket查找设备ID
            if (deviceId != null) {
                deviceManager.removeDevice(deviceId);
                logger.info("Device {} disconnected", deviceId);
            }
        });
    }

    // 解码消息（示例）
    private BaseMessage decodeMessage(Buffer buffer) {
        String messageStr = buffer.toString();
        if (JSON.isValid(messageStr)) {
            // JSON
            BaseMessage message = JSON.parseObject(messageStr, BaseMessage.class);
            logger.info("收到json数据：{}", message);
        } else {
            // 二进制
        }

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
