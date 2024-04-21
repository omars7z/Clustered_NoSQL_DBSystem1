package com.example.bootstrap.Service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class Tokens {

    private static Tokens instance;
    private final File tokensFile;
    private final long expirationTime = TimeUnit.HOURS.toMillis(1);
    private final Logger logger = Logger.getLogger(Tokens.class.getName());

    private static final String TOKENS_FILE_PATH = "src/main/resources/static/tokens.json";
    private Tokens() {
        tokensFile = new File(TOKENS_FILE_PATH);
    }

    public static Tokens getInstance() {
        if (instance == null) {
            instance = new Tokens();
        }
        return instance;
    }

    private String readFileAsString(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getPath(), e);
        }
    }

    private void writeTokensToFile(JSONArray jsonArray) {
        try {
            Files.write(Paths.get(TOKENS_FILE_PATH), jsonArray.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + TOKENS_FILE_PATH, e);
        }
    }

    public String allUsers() {
        return readFileAsString(tokensFile);
    }

    public void addNewToken(String userName, String token, String workerName) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("token", token);
        jsonObject.put("workerName", workerName);
        jsonObject.put("expirationTime", System.currentTimeMillis() + expirationTime);

        String info = readFileAsString(tokensFile);
        JSONArray jsonArray = new JSONArray(info);
        jsonArray.put(jsonObject);
        writeTokensToFile(jsonArray);
    }

    public void removeUser(String token) {
        String info = readFileAsString(tokensFile);
        JSONArray jsonArray = new JSONArray(info);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String currentToken = jsonObject.getString("token");
            if (currentToken.equals(token)) {
                String userName = jsonObject.getString("userName");
                String workerName = jsonObject.getString("workerName");

                Cluster.getInstance().removeExpiredToken(userName, token, workerName); // responsible worker will remove user
                jsonArray.remove(i); // remove user from the JSONArray
                break;
            }
        }
        writeTokensToFile(jsonArray);
    }


    @Scheduled(fixedRate = 900000)
    public void removeExpiredTokens() {

        logger.info("Checking for expired tokens, every 15 mins");

        String info = readFileAsString(tokensFile);
        JSONArray jsonArray = new JSONArray(info);

        for (Iterator<Object> iterator = jsonArray.iterator(); iterator.hasNext(); ) {
            JSONObject jsonObject = (JSONObject) iterator.next();
            long expirationTime = jsonObject.getLong("expirationTime");

            if (System.currentTimeMillis() > expirationTime) {
                String userName = jsonObject.getString("userName");
                String token = jsonObject.getString("token");
                String workerName = jsonObject.getString("workerName");

                Cluster.getInstance().removeExpiredToken(userName, token, workerName); // responsible worker will remove user
                iterator.remove(); // remove expired token from the JSONArray
            }
        }
        writeTokensToFile(jsonArray);
    }
}
