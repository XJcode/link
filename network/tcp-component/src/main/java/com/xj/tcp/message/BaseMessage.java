package com.xj.tcp.message;

import lombok.Data;

@Data
public abstract class BaseMessage<T> {

    private String deviceId;     // 设备ID
    private MessageType type;    // 消息类型
    private T payload;           // 消息内容（泛型）

    public enum MessageType {
        ONLINE, OFFLINE, HEARTBEAT, DATA, COMMAND
    }
}
