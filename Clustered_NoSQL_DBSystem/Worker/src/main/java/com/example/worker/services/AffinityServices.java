package com.example.worker.services;

import com.example.worker.caching.LRUCache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static com.example.worker.File.JSON.readFileAsString;

@Service
public class AffinityServices {
    public final static String BOOTSTRAPPING_NODE_USERNAME = "admin";
    public final static String BOOTSTRAPPING_NODE_TOKEN = "admin123";
    private static AffinityServices instance = null;
    private final String AFFINITY_FILE_PATH = "src/main/java/com/example/worker/services/document-affinity.json";
    private final Object lock = new Object();
    private List<String> workers = new ArrayList<>();
    private String currentWorkerName;
    private boolean isCurrentWorkerAffinity = false;
    private LRUCache<String, String> affinityCache = new LRUCache(100000); // cache for affinity.

    private AffinityServices() {
        workers.add("worker1");
        workers.add("worker2");
        workers.add("worker3");
    }

    public static AffinityServices getInstance() {
        if (instance == null) {
            instance = new AffinityServices();
        }
        return instance;
    }

    public String getCurrentWorkerName() {
        return currentWorkerName;
    }

    public void setCurrentWorkerName(String currentWorkerName) {
        this.currentWorkerName = currentWorkerName;
    }

    public List<String> getWorkers() {
        return workers;
    }

    public void propagateAddingAffinity(String docId, String docManagerName) {
        for (String worker : workers) {
            String url = "http://" + worker + ":8081/api/addAffinityData/" + docId + "/" + docManagerName;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Username", "admin");
            headers.set("X-Token", "admin123");
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            restTemplate.exchange(url, HttpMethod.GET, requestEntity, Void.class);
        }
    }

    public void addAffinity(String docId, String affinityName) {
        // adding to the cache
        affinityCache.put(docId, affinityName);

        // adding to the file
        File file = new File(AFFINITY_FILE_PATH);
        JSONArray jsonArray = new JSONArray(readFileAsString(file));

        try {
            FileWriter fileWriter = new FileWriter(file, false);

            // creating the json object.
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docId", docId);
            jsonObject.put("affinityName", affinityName);

            // adding the json object to the json array.
            jsonArray.put(jsonObject);

            // writing the json array to the file.
            synchronized (lock) {
                fileWriter.write(jsonArray.toString());
            }
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getAffinityName(String docId) {
        String affinityName = affinityCache.get(docId);
        if (affinityName != null) {
            return affinityName;
        }

        // look inside the affinity file.
        File file = new File(AFFINITY_FILE_PATH);
        JSONArray jsonArray = new JSONArray(readFileAsString(file));
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.get("docId").equals(docId)) {
                return (String) jsonObject.get("affinityName");
            }
        }

        return null;
    }

    public synchronized void setCurrentWorkerAffinity() {
        isCurrentWorkerAffinity = true;
    }

    public synchronized void unsetCurrentWorkerAffinity() {
        isCurrentWorkerAffinity = false;
    }

    public boolean isCurrentWorkerAffinity() {
        return isCurrentWorkerAffinity;
    }

    public synchronized void removeAffinity(String docId) {
        // removing from the cache.
        affinityCache.remove(docId);

        // removing from the file.
        File file = new File(AFFINITY_FILE_PATH);
        JSONArray jsonArray = new JSONArray(readFileAsString(file));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            if (jsonObject.get("docId").equals(docId)) {
                jsonArray.remove(i);
                break;
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(jsonArray.toString());
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
