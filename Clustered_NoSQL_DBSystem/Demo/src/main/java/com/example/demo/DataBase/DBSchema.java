package com.example.demo.DataBase;


import com.example.demo.Services.StudentServices;
//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

public class DBSchema {
    private final HttpHeaders headers;
    private final Logger logger = Logger.getLogger(DBSchema.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();

    public DBSchema(String databaseIP) {
        this.headers = new HttpHeaders();
        headers.set("X-Username", StudentServices.NODE_USERNAME);
        headers.set("X-Token", StudentServices.NODE_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public static void buildDatabaseSchema() {
        DBSchema schemaBuilder = new DBSchema("localhost");
        schemaBuilder.build();
    }

    public void build() {
        createDatabase();
        createCollection("students", getStudentSchema());
        createCollection("grades", getGradesSchema());
    }

    private void createDatabase() {
        String url = "http://localhost:8081/api/createDB/classSystem";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        logger.info("Creating classSystem database: " + response.getBody());
    }

    private void createCollection(String collectionName, String schema) {
        String url = "http://localhost:8081/api/createCol/classSystem/" + collectionName;
        HttpEntity<String> requestEntity = new HttpEntity<>(schema, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        logger.info("Creating " + collectionName + " collection: " + response.getBody());
    }

//    @Contract(pure = true)
    private String getStudentSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "name": { "type": "string" },
                        "place": { "type": "string" },
                        "phone": { "type": "string" },
                        "GPA": { "type": "number" }
                    },
                    "required": ["name", "place", "phone", "GPA"],
                    "additionalProperties": false
                }""";
    }

//    @Contract(pure = true)
    private String getGradesSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "studentID": { "type": "string" },
                        "gradeAmount": { "type": "string" }
                    },
                    "required": ["studentID", "gradeAmount"],
                    "additionalProperties": false
                }""";
    }
}
