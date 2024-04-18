package com.example.bootstrap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class Cluster {
    private static Cluster instance = null;
    private final String BOOTSTRAPPING_NODE_USERNAME = "admin";
    private final String BOOTSTRAPPING_NODE_TOKEN = "admin123";
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private Logger logger = Logger.getLogger(Cluster.class.getName());


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

    public void removeExpiredToken(String username, String token, String workerName) {
        logger.info("Removing expired token from " + workerName + "user name: " + username + " token: " + token);
        String url = "http://" + workerName + ":8081/api/removeAuthenticatedUser/" + username + "/" + token;
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, ApiResponse.class);
    }

    public void setUpWorkersNames() {
        logger.info("Setting up workers names");
        for (int i = 1; i <= 3; i++) {
            String workerName = "worker" + i;
            String url = "http://" + workerName + ":8081/api/setCurrentWorkerName/" + workerName;
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, ApiResponse.class);
        }
    }

    public void setFirstWorkerAsAffinity() {
        logger.info("Setting worker1 as affinity");
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", "admin");
        headers.set("X-Token", "admin123");
        String url = "http://worker1:8081/api/setAffinity";
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, ApiResponse.class);
    }

    public void sendUserInfo(String username, String token, String workerName) {
        logger.info("Sending user info to " + workerName + "user name: " + username + " token: " + token);
        String url = "http://" + workerName + ":8081/api/addAuthenticatedUser/" + username + "/" + token;
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        logger.info("User info sent to " + workerName + "user name: " + username + " token: " + token);
    }
}
