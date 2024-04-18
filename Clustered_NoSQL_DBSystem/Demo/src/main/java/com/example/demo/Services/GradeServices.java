package com.example.demo.Services;

import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class GradeServices {

    private static GradeServices instance = null;
    private RestTemplate restTemplate = new RestTemplate();

    public static GradeServices getInstance() {
        if (instance == null) {
            instance = new GradeServices();
        }
        return instance;
    }

    public JSONArray getStudentGrades(String studentId, HttpSession httpSession) {


        String workerPort = httpSession.getAttribute("workerPort").toString();
        String token = httpSession.getAttribute("token").toString();
        String username = httpSession.getAttribute("username").toString();

        System.out.println("workerPort: " + workerPort);
        System.out.println("token: " + token);
        System.out.println("username: " + username);

        String url = "http://localhost:" + workerPort + "/api/filter/classSystem/grades?" +
                "attributeName=studentAccountID&attributeValue=" + studentId;


        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        headers.set("Content-Type", "application/json");


        String response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class).getBody();
        System.out.println("response: " + response);

        JSONObject responseJSON = new JSONObject(response);
        JSONArray grades = new JSONArray(responseJSON.getString("message"));
        System.out.println("grades: " + grades.toString());

        return grades;
    }


    public boolean changeGrade(String studentAccountID, String amount, HttpSession httpSession) {


        String workerPort = httpSession.getAttribute("workerPort").toString();
        String token = httpSession.getAttribute("token").toString();
        String username = httpSession.getAttribute("username").toString();

        String url = "http://localhost:" + workerPort + "/api/insertOne/classSystem/grades";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Username", username);
        headers.set("X-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject grades = new JSONObject();
        grades.put("studentAccountID", studentAccountID);
        grades.put("gradeAmount", amount);

        HttpEntity<String> requestEntity = new HttpEntity<>(grades.toString(), headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        StudentServices networkManager = StudentServices.getInstance();
        JSONObject student = networkManager.getStudentByID(studentAccountID, httpSession);
        int amountInt = Integer.parseInt(amount);
        int oldGPA = student.getInt("GPA");
        int newGPA = oldGPA + amountInt;

        url = "http://localhost:" + workerPort + "/api/updateDoc/classSystem/students/" + studentAccountID
                + "/GPA/" + newGPA;

        HttpEntity<String> requestEntity2 = new HttpEntity<>("", headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity2, String.class);
        return true;
    }
}
