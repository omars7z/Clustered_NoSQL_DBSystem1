package com.example.bootstrap.Service;

import com.example.bootstrap.Model.APIResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class Cluster {

    private static final String BOOTSTRAPPING_NODE_USERNAME = "admin";
    private static final String BOOTSTRAPPING_NODE_TOKEN = "admin123";

    private static Cluster instance = null;
    private final HttpHeaders headers;
    private final RestTemplate restTemplate;
    private final Logger logger = Logger.getLogger(Cluster.class.getName());

    private Cluster() {
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.set("X-Username", BOOTSTRAPPING_NODE_USERNAME);
        headers.set("X-Token", BOOTSTRAPPING_NODE_TOKEN);
    }

    public static Cluster getInstance() {
        if (instance == null) {
            instance = new Cluster();
        }
        return instance;
    }

    public void setUpWorkersNames() {
        logger.info("Setting up workers names");
        for (int i = 1; i <= 3; i++) {
            String workerName = "worker" + i;
            String url = "http://" + workerName + ":8081/api/setCurrentWorkerName/" + workerName;
            sendGetRequest(url);
        }
    }

    public void setFirstWorkerAsAffinity() {
        logger.info("Setting worker1 as affinity");
        String url = "http://worker1:8081/api/setAffinity";
        sendGetRequest(url);
    }

    public void sendUserInfo(String username, String token, String workerName) {
        logger.info("Sending user info to " + workerName + " - username: " + username + ", token: " + token);
        String url = "http://" + workerName + ":8081/api/addAuthenticatedUser/" + username + "/" + token;
        sendGetRequest(url);
        logger.info("User info sent to " + workerName);
    }

    public void removeExpiredToken(String username, String token, String workerName) {
        logger.info("Removing expired token from " + workerName + "user name: " + username + " token: " + token);
        String url = "http://" + workerName + ":8081/api/removeAuthenticatedUser/" + username + "/" + token;
        sendDeleteRequest(url);
    }

    private void sendGetRequest(String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, APIResponse.class);
    }

    private void sendDeleteRequest(String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, APIResponse.class);
    }
}
