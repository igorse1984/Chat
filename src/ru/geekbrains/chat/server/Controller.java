package ru.geekbrains.chat.server;

import javafx.application.Platform;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    Server srv;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        new Thread(() -> {
        srv = new Server();
//        }).start();
    }

    // по нажатию кнопки
    public void online() {
        srv = new Server();
    }

    // по нажатию кнопки остановка сервера
    public void offline() {
        srv.stopServer();
    }


}
