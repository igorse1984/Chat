package ru.geekbrains.chat.server;

public interface AuthService {
    void start() throws AuthServiceException;
    void stop();
    String getNickByLoginAndPass(String login, String password);
}
