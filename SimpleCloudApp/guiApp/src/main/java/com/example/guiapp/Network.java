package com.example.guiapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Network(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public String readString() throws IOException {
        return inputStream.readUTF();
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    public void writeMessage(String msg) throws IOException {
        outputStream.writeUTF(msg);
        outputStream.flush();
    }
}
