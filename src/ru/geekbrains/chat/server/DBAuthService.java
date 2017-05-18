package ru.geekbrains.chat.server;

import java.sql.*;

public class DBAuthService implements AuthService {
    private Connection connection;
    private Statement stmt;

    // реализация предписанного интерфесом метода старт
    @Override
    public void start() throws AuthServiceException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            throw new AuthServiceException();
        }
    }

    // реализация предписанного интерфесом метода стоп
    @Override
    public void stop() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // реализация предписанного интерфесом метода выдачи серверу ника по логину и паролю
    @Override
    public String getNickByLoginAndPass(String login, String password) {
        try {
            String sql = String.format("SELECT nick FROM users WHERE login = '%s' AND password = '%s'", login, password);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
