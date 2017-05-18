package ru.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Обработчик сетевых запросов
 * кол-во экземпляров класса равно кол-ву подключенных клиентов
 */

public class ClientHandler {

    // поля
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    public String getNick() {
        return nick;
    }

    // конструктор
    // здесь основной код
    public ClientHandler(Server server, Socket socket) {
        try {
            // ссылка на родителя данного класса
            this.server = server;
            // сокет, дается от родителя, для каждого экземпляра разный
            this.socket = socket;
            // зная сокет, создаем сетевые потоки ввода-вывода
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // создаем отдельный поток обработки сетевых событий
            new Thread(() -> {
                try {
                    String msg = null;


                    // цикл авторизации клиента на сервере
                    while (true) {
                        // ставим таймаут сокету
                        socket.setSoTimeout(10000);

                        // получаем строку из входящего потока
                        msg = in.readUTF(); // приостанавливает цикл до момента получения данных

                        // если присутствует префикс ауэнтификации
                        if (msg.startsWith("/auth")) {
                            // дробим через пробел данную строку в массив
                            String[] data = msg.split("\\s");
                            // из геттера сервера вытаскиваем адрес экземпляра класса авторизации по базе,
                            // и в имеющемся там методе запрашиваем ник клиента зная логин и пароль,
                            // которые подключающийся клиент предоставил
                            String newNick = server.getAuthService().getNickByLoginAndPass(data[1], data[2]);
                            // если ник есть в базе т.е. логин и пароль подошли
                            if (newNick != null) {
                                // проверяем нет ли попытки авторизоваться повторно
                                if (!server.isNickBusy(newNick)) {
                                    // отправка уникального ключа авторизации клиенту,
                                    // который переведет клиента в режим чата т.е. авторизует
                                    sendMsg("78s7d6fjh53987t5hkj&^KGujgd");
                                    // отправка ника клиенту
                                    sendMsg("/yournickis " + newNick);
                                    // экземпляр обработчика запомнит свой ник и не даст по нему авторизоваться
                                    nick = newNick;
                                    // подписка на рассылку чата
                                    server.subscribe(this);
                                    // прерываем цикл авторизации пока не произойдет авторизация
                                    break;
                                } else {
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        } else {
                            sendMsg("Необходимо авторизоваться");
                        }
                    }

                    // переходим к следующему циклу, когда прерван цикл с авторизацией т.е. авторизация прошла
                    while (true) {
                        // ждем сообщений во входящем потоке


                        msg = in.readUTF();

                        // обработка сообщений с префиксом
                        if (msg.startsWith("/")) {
                            // если есть префикс завершения, прерываем цикл, клиент отключен
                            if (msg.equals("/end")) break;

                            // отправка личных сообщений способом "/w nick message"
                            // если в сообщении от отправителя присутствует нужный префикс
                            if (msg.startsWith("/w")) {
                                // дробим сообщение на части по ключу "пробел"
                                String[] data = msg.split("\\s");
                                // если получатель существует и авторизован
                                if (server.isNickBusy(data[1])) {
                                    String clientMsg;
                                    // если текст отправителя присутствует
                                    if (data.length > 2) {
                                        clientMsg = msg.substring((data[1].concat(data[2])).length());
                                    } else clientMsg = "";
                                    // отправляем получателю обработанное сообщение от отправителя
                                    server.sendMessageClient(data[1], nick + ": " + clientMsg);
                                } else
                                    sendMsg("Клиент с ником " + data[1] + " не в сети");
                            }
                        } else {
                            // делаем броадкаст если нет префикса
                            System.out.println("from client: " + msg);
                            server.broadcastMsg(nick + ": " + msg);
                        }
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Клиент с сокетом " + socket + " отключен по таймауту");
                    sendMsg("Соединение прервано по таймауту");
                } catch (IOException e) {
//                    e.printStackTrace();
                } finally {
                    // при отключении
                    System.out.println("Client disconnected");
                    try {
                        // закрываем корректно сокет
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Какие-то ошибки при закрытии сокета");
                        e.printStackTrace();
                    }
                    // сервер отписывает клиента от рассылки
                    server.unsubscribe(this);
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // отправка индивидуальных сообщений клиенту от сервера
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
