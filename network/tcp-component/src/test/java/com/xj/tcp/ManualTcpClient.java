package com.xj.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import java.util.Scanner;

public class ManualTcpClient {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4567;
    private static final String DEVICE_ID = "test-device-001";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        NetClient client = vertx.createNetClient();

        client.connect(SERVER_PORT, SERVER_HOST, res -> {
            if (res.failed()) {
                System.out.println("连接失败: " + res.cause().getMessage());
                vertx.close();
                return;
            }

            NetSocket socket = res.result();
            System.out.println("Connected to server");

            // 启动心跳
            vertx.setPeriodic(60_000, id -> {
                String heartbeat = String.format(
                        "{\"deviceId\":\"%s\",\"type\":\"HEARTBEAT\"}",
                        DEVICE_ID
                );
                socket.write(Buffer.buffer(heartbeat));
                System.out.println("Sent heartbeat");
            });

            // 启动控制台输入
            startConsoleInput(socket, vertx);

            // 监听关闭事件
            socket.closeHandler(v -> {
                System.out.println("连接已关闭");
                vertx.close();
            });
        });
    }

    private static void startConsoleInput(NetSocket socket, Vertx vertx) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("输入消息 (exit退出): ");
                String input = scanner.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    socket.close();
                    vertx.close();
                    scanner.close();
                    break;
                }

                String message = String.format(
                        "{\"deviceId\":\"%s\",\"type\":\"DATA\",\"payload\":\"%s\"}",
                        DEVICE_ID, input
                );
                socket.write(Buffer.buffer(message));
                System.out.println("已发送: " + input);
            }
        }).start();
    }

}
