package com.example.demo.Services;

import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

public class StudentServices {
    private static StudentServices instance = null;
    public static final String BOOTSTRAPPING_NODE_USERNAME = "admin";
    public static final String BOOTSTRAPPING_NODE_TOKEN = "admin123";
    private RestTemplate restTemplate = new RestTemplate();

    private StudentServices() {}

    public static StudentServices getInstance() {
        if (instance == null) {
            instance = new StudentServices();
        }
        return instance;
    }


    public String registerANewUser(String username) {
        String url = "http://localhost:8080/api/register/" + username;
        return restTemplate.getForObject(url, String.class);
    }

    public boolean isAuthorizedUser(String username, String token) {
        String url = "http://localhost:" + WorkerService.getInstance().getWorkerPort(token) + "/api/isUser/" + username + "/" + token;
        return restTemplate.getForObject(url, Boolean.class);
    }

    public boolean isAuthorizedAdmin(String username, String token) {
        String url = "http://localhost:" + 8081 + "/api/isAdmin/" + username + "/" + token;
        return restTemplate.getForObject(url, Boolean.class);
    }


    public void addNewStudent(JSONObject student, HttpSession session) {
        System.out.println("Adding new student to database" + student.toString());

        String workerPort = session.getAttribute("workerPort").toString();
        String token = session.getAttribute("token").toString();
        String username = session.getAttribute("username").toString();

        String url = "http://localhost:" + workerPort + "/api/insertDoc/classSystem/students";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(student.toString(), headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }



    public JSONObject getStudentByID(String id, HttpSession httpSession) {
        String workerPort = httpSession.getAttribute("workerPort").toString();
        String token = httpSession.getAttribute("token").toString();
        String username = httpSession.getAttribute("username").toString();

        String url = "http://localhost:" + workerPort + "/api/getDoc/classSystem/students/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);

        JSONObject responseJSON = new JSONObject(response.getBody());

        return new JSONObject(responseJSON.getString("message"));
    }

    public String allStudents(HttpSession httpSession) {
        String workerPort = httpSession.getAttribute("workerPort").toString();
        String token = httpSession.getAttribute("token").toString();
        String username = httpSession.getAttribute("username").toString();

        System.out.println("workerPort: " + workerPort);
        System.out.println("token: " + token);
        System.out.println("username: " + username);

        String url = "http://localhost:" + workerPort + "/api/getAllDocs/classSystem/students";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
    }


    public JSONArray allActiveUsers(HttpSession httpSession) {

        String url = "http://localhost:8080/api/getAllUsers";
        String username = httpSession.getAttribute("username").toString();
        String token = httpSession.getAttribute("token").toString();
        String users = restTemplate.getForObject(url, String.class);
        JSONArray usersJSON = new JSONArray(users);
        System.out.println("users: " + usersJSON.toString());
        return usersJSON;
    }


    public void removeUser(String token, HttpSession httpSession) {
        String url = "http://localhost:8080/api/removeUser/" + token;
        restTemplate.getForObject(url, String.class);
    }

    public void removeStudent(String studentID, HttpSession httpSession) {
        String workerPort = httpSession.getAttribute("workerPort").toString();
        String token = httpSession.getAttribute("token").toString();
        String username = httpSession.getAttribute("username").toString();
        System.out.println("workerPort: " + workerPort);
        System.out.println("Username responsible for deleting: " + username);
        System.out.println("Token responsible for deleting: " + token);

        String url = "http://localhost:" + workerPort + "/api/deleteDoc/classSystem/students/" + studentID;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
    }
}
