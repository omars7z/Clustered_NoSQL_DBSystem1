package com.example.worker.DAO;

import com.example.worker.caching.LRUCache;
import com.example.worker.indexing.PropertyIndexManager;
import com.example.worker.model.Schema;
import com.example.worker.threading.Threads;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.worker.File.JSON.readFileAsString;

public class DAO {
    private final Object lock = new Object();
    private final LRUCache<String, JSONArray> cache = new LRUCache<>(1000);
    private final PropertyIndexManager propertyIndexManager = PropertyIndexManager.getInstance();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Async
    public void clearCollectionCaching(File collectionFile) {
        cache.remove(collectionFile.getName());
    }

    @Async
    public void clearDBCache(File dbDirectory) {
        File[] files = dbDirectory.listFiles();
        if (files != null) {
            for (File collectionFile : files) {
                if (!collectionFile.getName().equals("schemas")) {
                    clearCollectionCaching(collectionFile);
                }
            }
        }
    }

    public String allCollections(File dbDirectory) {
        File[] files = dbDirectory.listFiles();
        StringBuilder collections = new StringBuilder();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals("schemas")) {
                    collections.append(file.getName(), 0, file.getName().lastIndexOf('.'));
                    collections.append(", ");
                }
            }
        }
        return collections.toString();
    }

    @Async
    public void createCollection(File collectionFile, File schemaFile, Schema schema) {
        try {
            collectionFile.createNewFile();
            FileWriter writer = new FileWriter(collectionFile, true);
            writer.write("[]");
            writer.close();

            schemaFile.createNewFile();
            schema.setAdditionalProperties(false);
            writer = new FileWriter(schemaFile);
            writer.write(new Gson().toJson(schema));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getDocument(String dbName, String collectionName, String id) {
        JSONArray jsonArray = propertyIndexManager.getMatchingDocs(dbName, collectionName, "_id", id);
        if (!jsonArray.isEmpty()) {
            return jsonArray.getJSONObject(0);
        }

        File collectionFile = new File("databases/" + dbName + "/" + collectionName + ".json");
        JSONArray data = getFilteredData(collectionFile, "_id", id);

        if (data.isEmpty()) {
            return new JSONObject("{}");
        }

        return data.getJSONObject(0);
    }

    @Async
    public boolean updateDoc(File collectionFile, String docId, JSONObject newObj) {
        JSONArray jsonArray = new JSONArray(readFileAsString(collectionFile));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            if (obj.getString("_id").equals(docId)) {
                jsonArray.put(i, newObj);
                try {
                    synchronized (lock) {
                        FileWriter writer = new FileWriter(collectionFile, false);
                        writer.write(jsonArray.toString());
                        writer.close();
                        cache.put(collectionFile.getName(), jsonArray);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
        return false;
    }

    @Async
    public void addDocument(File collectionFile, String jsonOBJ) {
        JSONArray jsonArray = new JSONArray(readFileAsString(collectionFile));
        JSONObject jsonObject = new JSONObject(jsonOBJ);
        jsonArray.put(jsonObject);

        try {
            FileWriter writer = new FileWriter(collectionFile, false);
            writer.write(jsonArray.toString());
            writer.close();
            cache.put(collectionFile.getName(), jsonArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAllDocs(File collectionFile) {
        JSONArray jsonArray = cache.get(collectionFile.getName());
        if (jsonArray != null) {
            return jsonArray.toString();
        }
        try {
            return FileUtils.readFileToString(collectionFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONArray getFilteredData(File collectionFile, String attributeName, String attributeValue) {
        JSONArray collectionData = cache.get(collectionFile.getName());

        if (collectionData == null) {
            collectionData = new JSONArray(readFileAsString(collectionFile));
        }

        JSONArray filteredArray = new JSONArray();
        for (int i = 0; i < collectionData.length(); i++) {
            JSONObject obj = collectionData.getJSONObject(i);
            if (obj.has(attributeName) && obj.get(attributeName).toString().equals(attributeValue)) {
                filteredArray.put(obj);
            }
        }

        Threads.synchronizedPut(cache, collectionFile, collectionData); // Safely put data into cache
        return filteredArray;
    }

    public boolean deleteDoc(String docContent, File collectionFile, String docId) {
        JSONArray jsonArray = cache.get(collectionFile.getName());
        if (jsonArray == null) {
            jsonArray = new JSONArray(readFileAsString(collectionFile));
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            if (obj.getString("_id").equals(docId)) {
                jsonArray.remove(i);
                try {
                    Threads.synchronizedPut(cache, collectionFile, jsonArray); // Safely put data into cache
                    FileWriter writer = new FileWriter(collectionFile, false);
                    writer.write(jsonArray.toString());
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
        return false;
    }

    public String listDbs(File dbDirectory) {
        String[] databases = dbDirectory.list();
        StringBuilder sb = new StringBuilder();
        if (databases != null) {
            for (int i = 0; i < databases.length; i++) {
                sb.append(databases[i]);
                if (i != databases.length - 1) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }
}
