package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    // хранилище обработчиков подключенных клиентов
    private Vector<ClientHandler> clients;
    // интерфейс объявляем за зачем?
    private DBAuthService authService;

    // возвращает созданный экземпляр класса взаимодействия с базой
    public DBAuthService getAuthService() {
        return authService;
    }

    // констуктор
    public Server() {
        // создаем экземпляр хранилища обработчиков
        clients = new Vector<>();
        // создаем экземпляр класса авторизации через базу
        authService = new DBAuthService();

        // новый экземпляр сервера сокетов на вручную указанном порту
        try (ServerSocket server = new ServerSocket(8189)) {

            // старт базы
            authService.start();
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                // ожидание подключения клиента с одновременым получения адреса сокета
                Socket socket = server.accept();
                // делаем экземпляр класса обработчика каждому подключенному клиенту
                new ClientHandler(this, socket);
                System.out.println("Client connected");
            }
        } catch (AuthServiceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // закрытие базы
            authService.stop();
        }
    }

    // броадкаст сообщения
    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    // проверка занятости ника, пройдясь по всем экземплярам обработчиков
    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients)
            if (o.getNick().equals(nick))
                return true;
        return false;
    }

    // отправка личных сообщений
    public void sendMessageClient(String nick, String msg) {
        for (ClientHandler o : clients)
            if (o.getNick().equals(nick)) o.sendMsg(msg);
    }

    // подписка на рассылку
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    // отписка от рассылки
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}
