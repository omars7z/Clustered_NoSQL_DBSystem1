package com.example.worker.controllers;

import com.example.worker.DAO.DAO;
import com.example.worker.authentication.AuthenticationService;
import com.example.worker.model.APIResponse;
import com.example.worker.services.AffinityServices;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static com.example.worker.File.JSON.DATABASES_DIRECTORY;
import static com.example.worker.File.JSON.SCHEMAS_DIRECTORY;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_TOKEN;
import static com.example.worker.services.AffinityServices.BOOTSTRAPPING_NODE_USERNAME;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    private final DAO dao;
    private final AuthenticationService authenticationService;
    private final RestTemplate restTemplate;
    private final AffinityServices affinityServices;
    private final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    public DatabaseController(DAO dao, AuthenticationService authenticationService, RestTemplate restTemplate, AffinityServices affinityServices) {
        this.dao = dao;
        this.authenticationService = authenticationService;
        this.restTemplate = restTemplate;
        this.affinityServices = affinityServices;
    }

    @GetMapping("/createDB/{name}")
    public APIResponse createDatabase(@PathVariable("name") String name,
                                      @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
                                      @RequestHeader(value = "X-Username") String username,
                                      @RequestHeader(value = "X-Token") String token) {

        logger.info("Creating database {} {}", name, (propagatedRequest ? "(propagated)" : ""));
        name = name.toLowerCase();
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.warn("User is not authorized.");
            return new APIResponse("User is not authorized.", HttpStatus.UNAUTHORIZED.value());
        }

        File dbDirectory = new File(DATABASES_DIRECTORY + name);
        if (dbDirectory.exists() && dbDirectory.isDirectory()) {
            logger.warn("Database already exists.");
            return new APIResponse("Database already exists.", HttpStatus.BAD_REQUEST.value());
        }

        if (propagatedRequest) {
            return handlePropagatedCreateDatabase(name, dbDirectory);
        } else {
            return propagateCreateDatabaseRequest(name);
        }
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

    @DeleteMapping("deleteDB/{name}")
    public APIResponse deleteDatabase(@PathVariable("name") String name,
                                      @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
                                      @RequestHeader(value = "X-Username") String username,
                                      @RequestHeader(value = "X-Token") String token) {

        logger.info("Deleting database {} {}", name, (propagatedRequest ? "(propagated)" : ""));
        name = name.toLowerCase();
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            return new APIResponse("User is not authorized.", HttpStatus.UNAUTHORIZED.value());
        }

        File dbDirectory = new File(DATABASES_DIRECTORY + name);
        if (!dbDirectory.exists() && !dbDirectory.isDirectory()) {
            logger.warn("Database does not exist.");
            return new APIResponse("Database does not exist.", HttpStatus.BAD_REQUEST.value());
        }

        if (propagatedRequest) {
            return handlePropagatedDeleteDatabase(name, dbDirectory);
        } else {
            return propagateDeleteDatabaseRequest(name);
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

    @GetMapping("/listDB")
    public APIResponse listDatabases(@RequestHeader(value = "X-Username") String username,
                                     @RequestHeader(value = "X-Token") String token) {
        logger.info("Requesting list of databases.");

        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.warn("User is not authorized.");
            return new APIResponse("User is not authorized.", HttpStatus.UNAUTHORIZED.value());
        }

        File dbDirectory = new File(DATABASES_DIRECTORY);

        return new APIResponse(dao.allDbs(dbDirectory), HttpStatus.OK.value());
    }
}
