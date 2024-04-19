package com.example.worker.authentication;

import com.example.worker.caching.LRUCache;
import com.example.worker.threading.AuthLock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static com.example.worker.File.JSON.readFileAsString;

@Component
public class AuthenticationService {

    private final String authDirectory = "src/main/java/com/example/worker/authentication/";
    private final LRUCache<String, String> userCache = new LRUCache<>(1000);
    private final LRUCache<String, String> adminCache = new LRUCache<>(1000);
    private final AuthLock authLock;

    public AuthenticationService(AuthLock authLock) {
        this.authLock = authLock;
    }

    public boolean isAuthenticatedUser(String username, String token) {
        validateInput(username, token);
        if (userCache.get(token) != null && userCache.get(token).equals(username)) {
            return true;
        }
        return checkAuthFile("users.json", username, token);
    }

    public void addUser(String username, String token) {
        validateInput(username, token);
        userCache.put(token, username);
        updateAuthFile("users.json", username, token);
    }

    public void addAdmin(String username, String token) {
        validateInput(username, token);
        addUser(username, token);
        adminCache.put(token, username);
        updateAuthFile("admins.json", username, token);
    }

    public synchronized void removeUser(String username, String token) {
        validateInput(username, token);
        userCache.remove(token);
        removeAuthEntry("users.json", username, token);
    }

    public boolean isAdmin(String username, String token) {
        validateInput(username, token);
        if (adminCache.get(token) != null && adminCache.get(token).equals(username)) {
            return true;
        }
        return checkAuthFile("admins.json", username, token);
    }

    private void validateInput(String username, String token) {
        if (username == null || token == null) {
            throw new IllegalArgumentException("Username or token is null");
        }
    }

    private boolean checkAuthFile(String fileName, String username, String token) {
        String content = readFileAsString(new File(authDirectory + fileName));
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.get("token").equals(token) && jsonObject.get("username").equals(username)) {
                userCache.put(username, token);
                return true;
            }
        }
        return false;
    }

    private void updateAuthFile(String fileName, String username, String token) {
        String content = readFileAsString(new File(authDirectory + fileName));
        JSONArray jsonArray = new JSONArray(content);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("token", token);
        jsonArray.put(jsonObject);
        try {
            authLock.writeJsonArrayToFile(new File(authDirectory + fileName), jsonArray.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeAuthEntry(String fileName, String username, String token) {
        String content = readFileAsString(new File(authDirectory + fileName));
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.get("token").equals(token) && jsonObject.get("username").equals(username)) {
                jsonArray.remove(i);
                break;
            }
        }
        try {
            authLock.writeJsonArrayToFile(new File(authDirectory + fileName), jsonArray.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
