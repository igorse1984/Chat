package ru.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public TextArea textArea;
    public TextField textField;

    public TextField loginField;
    public TextField passField;
    public Button btnLogin;

    public HBox loginPanel;
    public HBox msgPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean isAuthorized;
    private String myNick;

    // переводит окно в режим авторизации и обратно
    public void setIsAuthorized(boolean value) {
        // знание о собственном статусе авторизации
        isAuthorized = value;

        // выполнение в потоке интерфейса чтобы не получить Thread exception
        Platform.runLater(() -> {
            if (value) {
                loginPanel.setVisible(false);
                loginPanel.setManaged(false);
                msgPanel.setVisible(true);
                msgPanel.setManaged(true);
                textField.requestFocus();
            } else {
                loginPanel.setVisible(true);
                loginPanel.setManaged(true);
                msgPanel.setVisible(false);
                msgPanel.setManaged(false);
                myNick = "";
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // по-умолчанию при запуске клиента
        setIsAuthorized(false);
    }

    public void connect() {
        try {
            // новый сокет на стороне клиента
            socket = new Socket("localhost", 8189);

            // новые потоки ввода-вывода на стороне клиента
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            // параллельный поток обработки сетевых событий клиента
            Thread inputThread = new Thread(() -> {
                try {
                    String msg = null;

                    // цикл ожидания уникального ключа авторизации
                    while (true) {
                        // ожидание сообщений, приостановка цикла
                        msg = in.readUTF();
                        // ждем ключ
                        if (msg.equals("78s7d6fjh53987t5hkj&^KGujgd")) {
                            // клиент авторизован и переключается в режим чата
                            setIsAuthorized(true);
                            // прерывает цикл
                            break;
                        } else {
                            // без уникального ключа пишем сервисные сообщения под строку с логином и паролем
                            textArea.appendText(msg + "\n");
                        }
                    }

                    // цикл получения сообщений + обработка сообщений с префиксом
                    while (true) {
                        msg = in.readUTF();
                        // обработка сообщений с префиксом
                        if (msg.startsWith("/")) {
                            // обработка сообщений с ником
                            if (msg.startsWith("/yournickis")) {
                                // запоминаем свой ник
                                // разбивка на части по пробелу и чтение слова после префикса
                                myNick = msg.split("\\s")[1];
                                textArea.appendText("Вы вошли под ником " + myNick + "\n");
                                System.out.println(myNick);
                            }
                        } else {
                            // отображение полученных сообщений
                            textArea.appendText(msg + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Проблемы при обращении к серверу");
                    setIsAuthorized(false);
                }
            });
            inputThread.setDaemon(true);
            inputThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // по нажатию кнопки, отправка данных формы авторизации на сервер
    public void sendAuth() {
        try {
            if (socket == null || socket.isClosed())
                connect();
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
            loginField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // по нажатию кнопки, отправка сообщений от клиента на сервер
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            showAlert("Не получается отослать сообщение, проверьте подключение");
            e.printStackTrace();
        }
    }

    // отображение аллертов
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Возникли проблемы");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
