//package com.example.worker.services;
//
//import com.example.worker.DAO.DAO;
//import com.example.worker.authentication.AuthenticationService;
//import com.example.worker.indexing.PropertyIndexManager;
//import com.example.worker.model.APIResponse;
//import com.example.worker.services.AffinityServices;
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Set;
//
//import static com.example.worker.File.JSON.DATABASES_DIRECTORY;
//import static com.example.worker.File.JSON.SCHEMAS_DIRECTORY;
//import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_TOKEN;
//import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_USERNAME;
//
//@Service
//public class DocService {
//
//    private DAO dao = new DAO();
//    private PropertyIndexManager propertyIndexManager = PropertyIndexManager.getInstance();
//    private AffinityServices affinityServices = AffinityServices.getInstance();
//    private RestTemplate restTemplate = new RestTemplate();
//    private AuthenticationService authenticationService;
//    private Logger logger = LoggerFactory.getLogger(DocService.class);
//
//    public DocService(AuthenticationService authenticationService) {
//        this.authenticationService = authenticationService;
//    }
//
////    public APIResponse insertDocument(String dbName, String collectionName, String json, boolean propagatedRequest,
////                                      String username, String token) {
////
////    }
////
////    public APIResponse getDocument(String dbName, String collectionName, String docId, String username, String token) {
////
////    }
////
////    public APIResponse getAllDocuments(String dbName, String collectionName, String username, String token) {
////
////    }
////
////    public APIResponse deleteDocument(String dbName, String collectionName, String docId, boolean propagatedRequest,
////                                      String username, String token) {
////
////    }
////
////    public APIResponse updateDocument(String dbName, String collectionName, String docId, String propertyName,
////                                      Object newValue, boolean propagatedRequest, String username, String token,
////                                      String oldValue) {
////
////    }
//
//    private boolean isDatabaseExists(File dbDirectory) {
//        return dbDirectory.exists() && dbDirectory.isDirectory();
//    }
//
//    private boolean isCollectionExists(File collectionFile) {
//        return collectionFile.exists() && !collectionFile.isDirectory();
//    }
//
//    private String getDataType(File schema, String property) {
//        try {
//            String schemaContent = new String(Files.readAllBytes(schema.toPath()));
//            JSONObject schemaObject = new JSONObject(schemaContent);
//            JSONObject properties = schemaObject.getJSONObject("properties");
//            JSONObject propertyObj = properties.getJSONObject(property);
//            return propertyObj.getString("type");
//        } catch (IOException e) {
//            logger.error("Error reading schema file: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    private boolean validJSONObject(String json, File schemaFile) {
//        try {
//            String schemaContent = new String(Files.readAllBytes(schemaFile.toPath()));
//            JSONObject schemaObject = new JSONObject(schemaContent);
//            JSONObject jsonDocument = new JSONObject(json);
//            // Perform validation logic here
//            return true; // For demonstration purposes, assuming JSON is always valid
//        } catch (IOException e) {
//            logger.error("Error reading schema file: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    private String readFileAsString(File file) {
//        try {
//            return new String(Files.readAllBytes(file.toPath()));
//        } catch (IOException e) {
//            logger.error("Error reading file: {}", e.getMessage());
//            return null;
//        }
//    }
//}
