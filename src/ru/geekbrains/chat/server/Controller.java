package ru.geekbrains.chat.server;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    Server srv;
    Thread th;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    // по нажатию кнопки
    public void online() {
        th = new Thread(() -> {
            srv = new Server();
        });
        th.start();
    }

    // по нажатию кнопки остановка сервера
    public void offline() {
        srv.stopServer();
    }

    public void clientsListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {

        }
    }


}
