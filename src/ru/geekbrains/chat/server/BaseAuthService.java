package ru.geekbrains.chat.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class BaseAuthService implements AuthService {
    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            if (login != null ? !login.equals(entry.login) : entry.login != null) return false;
            if (pass != null ? !pass.equals(entry.pass) : entry.pass != null) return false;
            return nick != null ? nick.equals(entry.nick) : entry.nick == null;
        }

        @Override
        public int hashCode() {
            int result = login != null ? login.hashCode() : 0;
            result = 31 * result + (pass != null ? pass.hashCode() : 0);
            result = 31 * result + (nick != null ? nick.hashCode() : 0);
            return result;
        }
    }

    private ArrayList<Entry> list;

    public BaseAuthService() {
        list = new ArrayList<>();
    }

    @Override
    public void start() throws AuthServiceException {
        for (int i = 1; i <= 30; i++) {
            list.add(new Entry("login" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public String getNickByLoginAndPass(String login, String password) {
        for (Entry o : list) {
            if (o.login.equals(login) && o.pass.equals(password)) {
                return o.nick;
            }
        }
        return null;
    }
}
