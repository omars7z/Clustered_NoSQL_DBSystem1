package com.example.worker.services;

import com.example.worker.DAO.DAO;
import com.example.worker.authentication.AuthenticationService;
import com.example.worker.indexing.PropertyIndexManager;
import com.example.worker.model.APIResponse;
import com.example.worker.model.Schema;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static com.example.worker.File.JSON.*;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_TOKEN;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_USERNAME;

@Service
public class ColService {

    private final RestTemplate restTemplate;
    private final DAO dao;
    private final PropertyIndexManager propertyIndexManager;
    private final AuthenticationService authenticationService;
    private final AffinityServices affinityServices;
    private final Object lock = new Object();

    private final Logger logger = LoggerFactory.getLogger(ColService.class);

    public ColService(RestTemplate restTemplate, DAO dao, PropertyIndexManager propertyIndexManager,
                      AuthenticationService authenticationService, AffinityServices affinityServices) {
        this.restTemplate = restTemplate;
        this.dao = dao;
        this.propertyIndexManager = propertyIndexManager;
        this.authenticationService = authenticationService;
        this.affinityServices = affinityServices;
    }

    public APIResponse propagateCreateCollectionRequest(String dbName, String collectionName, Schema schema) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Propagated-Request", "true");
        headers.set("X-Username", BOOTSTRAPPING_NODE_USERNAME);
        headers.set("X-Token", BOOTSTRAPPING_NODE_TOKEN);
        String urlPrefix = "http://";

        for (String worker : affinityServices.getWorkers()) {
            String url = urlPrefix + worker + ":8081/api/createCol/" + dbName + "/" + collectionName;
            HttpEntity<Schema> requestEntity = new HttpEntity<>(schema, headers);
            try {
                restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                logger.info("Propagated create collection request to worker: {}", worker);
            } catch (Exception e) {
                logger.error("Error propagating create collection request to worker {}: {}", worker, e.getMessage());
                // Handle the error case if needed
            }
        }
        return new APIResponse("Create collection request propagated successfully.", 200);
    }

    public APIResponse handlePropagatedCreateCollection(String dbName, String collectionName, File dbDirectory, Schema schema) {
        File collectionFile = new File(DATABASES_DIRECTORY + dbName + "/" + collectionName + ".json");
        APIResponse response = new APIResponse("", 200);

        if (collectionFile.exists()) {
            logger.warn("Collection already exists.");
            response.setMessage("Collection already exists.");
            response.setStatusCode(400);
        } else {
            File schemaFile = new File(DATABASES_DIRECTORY + dbName + SCHEMAS_DIRECTORY + collectionName + ".json");
            dao.createCollection(collectionFile, schemaFile, schema);
            response.setMessage("Collection created successfully.");
            response.setStatusCode(200);
        }

        return response;
    }

    public APIResponse handlePropagatedDeleteCollection(String dbName, String collectionName, File dbDirectory) {
        File collectionFile = new File(DATABASES_DIRECTORY + dbName + "/" + collectionName + ".json");
        APIResponse response = new APIResponse("", 200);

        if (!collectionFile.exists()) {
            logger.warn("Collection does not exist.");
            response.setMessage("Collection does not exist.");
            response.setStatusCode(400);
            return response;
        }

        // clearing the cache and the indexing
        try {
            dao.clearCollectionCaching(collectionFile);
            propertyIndexManager.clearCollectionIndexing(dbName, collectionName);
            File schemaFile = new File(DATABASES_DIRECTORY + dbName + SCHEMAS_DIRECTORY + collectionName + ".json");

            synchronized (lock) {
                collectionFile.delete();
                schemaFile.delete();
            }

            logger.info("Collection '{}.{}' deleted successfully.", dbName, collectionName);
            response.setMessage("Collection deleted successfully.");
            response.setStatusCode(200);

        } catch (Exception e) {
            logger.error("Error deleting collection '{}.{}'.", dbName, collectionName, e);
            response.setMessage("Error deleting collection.");
            response.setStatusCode(500);
        }

        return response;
    }


    public APIResponse propagateDeleteCollectionRequest(String dbName, String collectionName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Propagated-Request", "true");
        headers.set("X-Username", BOOTSTRAPPING_NODE_USERNAME);
        headers.set("X-Token", BOOTSTRAPPING_NODE_TOKEN);
        String urlPrefix = "http://";

        for (String worker : affinityServices.getWorkers()) {
            String url = urlPrefix + worker + ":8081/api/deleteCol/" + dbName.toLowerCase() + "/" + collectionName.toLowerCase();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            try {
                restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                logger.info("Propagated delete collection request to worker: {}", worker);
            } catch (Exception e) {
                logger.error("Error propagating delete collection request to worker {}: {}", worker, e.getMessage());
            }
        }
        return new APIResponse("Delete collection request propagated successfully.", 200);
    }

    public APIResponse filterCollection(String dbName, String collectionName, String attributeName, String attributeValue) {
        File collectionFile = new File(DATABASES_DIRECTORY + dbName + "/" + collectionName + ".json");
        if (!collectionFile.exists()) {
            logger.warn("Collection does not exist.");
            return new APIResponse("Collection does not exist.", 404);
        }

        JSONArray filteredData = dao.getFilteredData(collectionFile, attributeName, attributeValue);
        return new APIResponse(filteredData.toString(), 200);
    }

}