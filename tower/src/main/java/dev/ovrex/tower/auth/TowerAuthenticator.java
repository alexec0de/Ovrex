package dev.ovrex.tower.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TowerAuthenticator {
    private final Map<String, String> credentials = new ConcurrentHashMap<>();

    public void addCredential(String login, String password) {
        credentials.put(login, password);
    }

    public boolean authenticate(String login, String password) {
        String stored = credentials.get(login);
        return stored != null && stored.equals(password);
    }
}
