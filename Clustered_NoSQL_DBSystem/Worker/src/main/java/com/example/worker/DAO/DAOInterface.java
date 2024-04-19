package com.example.worker.DAO;

import com.example.worker.model.Schema;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public interface DAOInterface {

    void clearDBCache(File dbDirectory);

    String listDbs(File dbDirectory);

    void clearCollectionCaching(File collectionFile);

    String allCollections(File dbDirectory);

    void createCollection(File collectionFile, File schemaFile, Schema schema);

    String getAllDocs(File collectionFile);

    JSONObject getDocument(String dbName, String collectionName, String id);

    void addDocument(File collectionFile, String jsonOBJ);

    boolean updateDoc(File collectionFile, String docId, JSONObject newObj);

    boolean deleteDoc(String docContent, File collectionFile, String docId);

    JSONArray getFilteredData(File collectionFile, String attributeName, String attributeValue);
}
