package com.xj.tcp.message;

public class HeartbeatMessage extends BaseMessage<Void> {

    public HeartbeatMessage(String deviceId) {
        setDeviceId(deviceId);
        setType(MessageType.HEARTBEAT);
        setPayload(null); // 心跳消息无需内容
    }
}
