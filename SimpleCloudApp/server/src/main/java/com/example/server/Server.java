package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(7990)) {
            System.out.println("Server started");
            while (true) {
                Socket socket = server.accept();
                GuiHandler handler = new GuiHandler(socket);
                new Thread(handler).start();
            }
        }
    }
}
