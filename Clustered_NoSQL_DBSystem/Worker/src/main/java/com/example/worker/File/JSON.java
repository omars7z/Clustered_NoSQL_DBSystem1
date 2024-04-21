package com.example.worker.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class JSON {

    public static final String DATABASES_DIRECTORY = "./src/main/java/com/example/worker/databases/";
    public static final String SCHEMAS_DIRECTORY = "/schemas/";


    public static String readFileAsString(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDatabaseExists(File file) {
        return file.exists() && file.isDirectory();
    }

    public static boolean isCollectionExists(File file) {
        return file.exists() && file.isFile();
    }

    public String getDataType(File Schema, String property) {
        JSONObject schema = new JSONObject(readFileAsString(Schema));
        String dataType = schema.getJSONObject("properties").getJSONObject(property).getString("type");
        return dataType;
    }

    public static String addIdToDocument(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);
            if (!node.has("_id")) {
                UUID uuid = UUID.randomUUID();
                ((ObjectNode) node).put("_id", uuid.toString());
            }

            String newJson = mapper.writeValueAsString(node);
            return newJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String removeIdFromDoc(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);
            if (node.has("_id")) {
                ((ObjectNode) node).remove("_id");
            }
            String newJson = mapper.writeValueAsString(node);
            return newJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}