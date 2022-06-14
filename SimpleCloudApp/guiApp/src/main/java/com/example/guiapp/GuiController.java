package com.example.guiapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private String homeDir;

    private byte[] buffer;

    @FXML
    public ListView clientView;

    @FXML
    public ListView serverView;
    @FXML
    public Button upload;
    @FXML
    public Button download;
    private Network network;

    @FXML


    private void readLoop(){
        try {
            while(true) {
                String command = network.readString();
                if (command.equals("#list#")) {
                    Platform.runLater(()-> serverView.getItems().clear());
                    int length = network.readInt();
                    for (int i = 0; i < length; i++) {
                        String file = network.readString();
                        Platform.runLater(() -> serverView.getItems().add(file));
                    }
                }
                if (command.equals("#sendingFile#")) {
                    String newFileName = network.getInputStream().readUTF();
                    long fileLength = network.getInputStream().readLong();
                    File downloadedFile = Path.of(homeDir).resolve(newFileName).toFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile)) {
                        for (int i = 0; i < (fileLength + 255) / 256; i++) {
                            int read = network.getInputStream().read(buffer);
                            fileOutputStream.write(buffer, 0, read);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   Platform.runLater(()-> clientView.getItems().clear());
                   Platform.runLater(()-> clientView.getItems().addAll(getFiles(homeDir)));
                }

            }
        } catch (IOException e) {
            System.err.println("Connection lost");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            buffer = new byte[256];
            homeDir = System.getProperty("user.home");
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir));
            network = new Network(7990);
            Thread thread = new Thread(this::readLoop);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private List<String> getFiles (String dir) {
        String[] list = new File(dir).list();
        return Arrays.asList(list);
    }

    public void uploadAction(ActionEvent actionEvent) throws IOException {
        network.getOutputStream().writeUTF("#upload#");
        String fileName = clientView.getSelectionModel().getSelectedItem().toString();
        network.getOutputStream().writeUTF(fileName);
        File toSend = Path.of(homeDir).resolve(fileName).toFile();
        network.getOutputStream().writeLong(toSend.length());
        try (FileInputStream fileInputStream = new FileInputStream(toSend)) {
            while (fileInputStream.available() > 0) {
                int read = fileInputStream.read(buffer);
                network.getOutputStream().write(buffer, 0, read);
            }
        }
        network.getOutputStream().flush();
    }

    public void downloadAction(ActionEvent actionEvent) throws IOException {
        network.getOutputStream().writeUTF("#download#");
        String fileName = serverView.getSelectionModel().getSelectedItem().toString();
        network.getOutputStream().writeUTF(fileName);
        network.getOutputStream().flush();
    }
}