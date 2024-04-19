package com.example.worker.services;

import com.example.worker.DAO.DAO;
import com.example.worker.authentication.AuthenticationService;
import com.example.worker.controllers.DatabaseController;
import com.example.worker.model.APIResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static com.example.worker.File.JSON.DATABASES_DIRECTORY;
import static com.example.worker.File.JSON.SCHEMAS_DIRECTORY;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_TOKEN;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_USERNAME;

@Service
public class DBService {

    private DAO dao = new DAO();
    private AuthenticationService authenticationService;
    private RestTemplate restTemplate;
    private AffinityServices affinityServices;
    private final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    private APIResponse propagateCreateDatabaseRequest(String name) {
        logger.info("Propagating request to other nodes.");
        for (String worker : affinityServices.getWorkers()) {
            String url = "http://" + worker + ":8081/api/createDB/" + name;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Propagated-Request", "true");
            headers.set("X-Username", BOOTSTRAPPING_NODE_USERNAME);
            headers.set("X-Token", BOOTSTRAPPING_NODE_TOKEN);
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        }
        return new APIResponse("Propagating request to other nodes.", HttpStatus.OK.value());
    }

    private APIResponse handlePropagatedCreateDatabase(String name, File dbDirectory) {
        try {
            dbDirectory.mkdirs();
            File schemasDirectory = new File(DATABASES_DIRECTORY + name + SCHEMAS_DIRECTORY);
            schemasDirectory.mkdirs();
            logger.info("Database created successfully.");
            return new APIResponse("Database created successfully.", HttpStatus.OK.value());
        } catch (Exception e) {
            logger.error("Error creating database.", e);
            return new APIResponse("Error creating database.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private APIResponse handlePropagatedDeleteDatabase(String name, File dbDirectory) {
        try {
            dao.clearDBCache(dbDirectory);
            FileUtils.deleteDirectory(dbDirectory);
            logger.info("Database deleted successfully.");
            return new APIResponse("Database deleted successfully.", HttpStatus.OK.value());
        } catch (Exception e) {
            logger.error("Error deleting database.", e);
            return new APIResponse("Error deleting database.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private APIResponse propagateDeleteDatabaseRequest(String name) {
        logger.info("Propagating request to other nodes.");
        for (String worker : affinityServices.getWorkers()) {
            String url = "http://" + worker + ":8081/api/deleteDB/" + name;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Propagated-Request", "true");
            headers.set("X-Username", BOOTSTRAPPING_NODE_USERNAME);
            headers.set("X-Token", BOOTSTRAPPING_NODE_TOKEN);
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        }
        return new APIResponse("Propagating request to other nodes.", HttpStatus.OK.value());
    }
}
