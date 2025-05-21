package com.xj.tcp;

import com.xj.tcp.server.TcpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TcpTest {

    private static final String DEVICE_ID = "test-device-001";
    private static final int SERVER_PORT = 4567;

    // 启动服务器（假设你的 TcpServer 类有 start 方法）
//    @BeforeEach
//    void deployServer(Vertx vertx, VertxTestContext testContext) {
//        TcpServer server = new TcpServer(vertx);
//        server.start(SERVER_PORT);
//        testContext.completeNow();
//    }

    @Test
    void testHeartbeatAndMessage(Vertx vertx) throws Throwable {
        NetClient client = vertx.createNetClient();
        AtomicInteger heartbeatCounter = new AtomicInteger(0);

        // 1. 连接服务器
        client.connect(SERVER_PORT, "127.0.0.1", res -> {
            if (res.failed()) {
                System.out.println("连接失败");
                return;
            }

            NetSocket socket = res.result();
            System.out.println("Connected to server");

            // 2. 启动心跳定时任务（60秒一次）
            long timerId = vertx.setPeriodic(60_000, id -> {
                String heartbeat = String.format(
                        "{\"deviceId\":\"%s\",\"type\":\"HEARTBEAT\",\"payload\":{}}",
                        DEVICE_ID
                );
                socket.write(Buffer.buffer(heartbeat));
                heartbeatCounter.incrementAndGet();
                System.out.println("Sent heartbeat");
            });

            // 3. 发送测试消息
            String testMessage = "Hello Server!";
            String dataMsg = String.format(
                    "{\"deviceId\":\"%s\",\"type\":\"DATA\",\"payload\":\"%s\"}",
                    DEVICE_ID, testMessage
            );
            socket.write(Buffer.buffer(dataMsg));

            // 4. 监听服务器响应（示例：假设服务器会回复 "ACK"）
            socket.handler(buffer -> {
                String response = buffer.toString();
                System.out.println(response);
//                if (response.contains("ACK")) {
//                    testContext.verify(() -> {
//                        assertTrue(response.contains(testMessage));
//                        System.out.println("Received ACK: " + response);
//                    });
//                }
            });

            this.startConsoleInput(socket);

            // 4. 连接关闭处理
            socket.closeHandler(v -> {
                System.out.println("Connection closed");
                vertx.close();
            });

            socket.exceptionHandler(e -> {
                System.err.println("Connection error: " + e.getMessage());
            });

            // 5. 验证心跳和连接保持
//            vertx.setTimer(130_000, delay -> { // 130秒后验证（超过2次心跳间隔）
//                testContext.verify(() -> {
//                    assertTrue(heartbeatCounter.get() >= 2, "至少发送2次心跳");
//                    // 可在此添加设备在线状态的断言（需访问 DeviceManager）
//                });
//
//                // 关闭连接并完成测试
//                vertx.cancelTimer(timerId);
//                client.close();
//                testContext.completeNow();
//            });
        });

        // 等待测试完成（超时时间需大于130秒）
//        assertTrue(testContext.awaitCompletion(140, TimeUnit.SECONDS));
    }

    // 启动控制台输入线程（独立于事件循环）
    private void startConsoleInput(NetSocket socket) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    System.out.print("Enter message (type 'exit' to quit): ");
                    String input = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(input)) {
                        socket.close();
                        scanner.close();
                        break;
                    }

                    // 发送自定义消息
                    String message = String.format(
                            "{\"deviceId\":\"%s\",\"type\":\"DATA\",\"payload\":\"%s\"}",
                            DEVICE_ID, input
                    );
                    socket.write(Buffer.buffer(message));
                    System.out.println("Sent: " + input);

                } catch (Exception e) {
                    System.err.println("Input error: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }
}
