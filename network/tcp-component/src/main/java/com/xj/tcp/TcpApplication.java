package com.xj.tcp;

import com.xj.tcp.server.TcpServer;
import io.vertx.core.Vertx;

public class TcpApplication {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        TcpServer server = new TcpServer(vertx);
        server.start(4567);
    }
}
