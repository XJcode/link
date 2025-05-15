package com.xj.tcp.client;

import io.vertx.core.net.NetSocket;
import lombok.Data;

@Data
public class DeviceInfo {

    private String deviceId;
    private NetSocket socket;
    private long lastActiveTime; // 最后活跃时间戳
    private boolean online;      // 是否在线
}
