package com.example.worker.Services;

import com.example.worker.DAO.DAO;
import com.example.worker.Model.APIResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static com.example.worker.File.JSON.DATABASES_DIRECTORY;
import static com.example.worker.File.JSON.SCHEMAS_DIRECTORY;
import static com.example.worker.Services.AffinityServices.NODE_TOKEN;
import static com.example.worker.Services.AffinityServices.NODE_USERNAME;

@Service
public class DBService {

    private final DAO dao = new DAO();
    private final AffinityServices affinityServices = AffinityServices.getInstance();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Logger logger = LoggerFactory.getLogger(DBService.class);

    public APIResponse createDatabase(String name, boolean propagatedRequest) {
        File dbDirectory = new File(DATABASES_DIRECTORY + name);
        if (dbDirectory.exists() && dbDirectory.isDirectory()) {
            logger.warn("Database already exists.");
            return new APIResponse("Database already exists.", 400);
        }

        if (!propagatedRequest) {
            propagateCreateDatabaseRequest(name);
        } else {
            if (!dbDirectory.mkdirs()) {
                logger.error("Error creating database.");
                return new APIResponse("Error creating database.", 500);
            } else {
                File schemasDirectory = new File(DATABASES_DIRECTORY + name + SCHEMAS_DIRECTORY);
                schemasDirectory.mkdirs();
            }
        }

        logger.info("Database created successfully.");
        return new APIResponse("Database created successfully.", 200);
    }

    public APIResponse deleteDatabase(String name, boolean propagatedRequest) {
        File dbDirectory = new File(DATABASES_DIRECTORY + name);
        if (!dbDirectory.exists() && !dbDirectory.isDirectory()) {
            logger.warn("Database does not exist.");
            return new APIResponse("Database does not exist.", 400);
        }

        if (propagatedRequest) {
            try {
                dao.clearDBCache(dbDirectory);
                FileUtils.deleteDirectory(dbDirectory);
            } catch (Exception e) {
                return new APIResponse("Error deleting database.", 500);
            }
        } else {
            propagateDeleteDatabaseRequest(name);
        }

        logger.info("Database deleted successfully.");
        return new APIResponse("Database deleted successfully.", 200);
    }

    private void propagateCreateDatabaseRequest(String name) {
        logger.info("Propagating request to other nodes.");
        for (String worker : affinityServices.getWorkers()) {
            String url = "http://" + worker + ":8081/api/createDB/" + name;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Propagated-Request", "true");
            headers.set("X-Username", NODE_USERNAME);
            headers.set("X-Token", NODE_TOKEN);
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        }
    }

    private void propagateDeleteDatabaseRequest(String name) {
        logger.info("Propagating request to other nodes.");
        for (String worker : affinityServices.getWorkers()) {
            String url = "http://" + worker + ":8081/api/deleteDB/" + name;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Propagated-Request", "true");
            headers.set("X-Username", NODE_USERNAME);
            headers.set("X-Token", NODE_TOKEN);
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        }
    }
}
