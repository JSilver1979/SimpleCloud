package com.example.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GuiHandler implements Runnable{

    private final String serverDir = "srv_files";
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public GuiHandler(Socket socket) throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client accepted");
        sendListOfFiles(serverDir);
    }

    private void sendListOfFiles(String dir) throws IOException {
        outputStream.writeUTF("#list#");
        List<String> files = getFiles(serverDir);
        outputStream.writeInt(files.size());
        for (String file : files) {
            outputStream.writeUTF(file);
        }
        outputStream.flush();
    }

    private void downloadFile(String dir) {

    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        try {
            while (true) {
                String command = inputStream.readUTF();
                System.out.println("received: " + command);
                if (command.equals("#upload#")) {
                    String fileName = inputStream.readUTF();
                    long fileLength = inputStream.readLong();
                    File uploadedFile = Path.of(serverDir).resolve(fileName).toFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(uploadedFile)) {
                        for (int i = 0; i < (fileLength + 255) / 256; i++) {
                            int read = inputStream.read(buffer);
                            fileOutputStream.write(buffer, 0 ,read);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendListOfFiles(serverDir);
                }
                if (command.equals("#download#")) {
                    String fileName = inputStream.readUTF();
                    File toDownload = Path.of(serverDir).resolve(fileName).toFile();
                    outputStream.writeUTF("#sendingFile#");
                    outputStream.writeUTF(fileName);
                    outputStream.writeLong(toDownload.length());
                    try (FileInputStream fileInputStream = new FileInputStream(toDownload)) {
                        while (fileInputStream.available() > 0) {
                            int read = fileInputStream.read(buffer);
                            outputStream.write(buffer, 0, read);
                        }
                    }
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Connection lost");
        }
    }

    private List<String> getFiles (String dir) {
        String[] list = new File(dir).list();
        return Arrays.asList(list);
    }
}
