package com.example.worker.Indexing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.worker.File.JSON.DATABASES_DIRECTORY;
import static com.example.worker.File.JSON.readFileAsString;


// couldn't store on disk, so made it on memory

@Repository
public class BTreeIndex {

    private Map<String, BTreeNode> indexes; // Map<DBName, B-tree root>
    private static final int MAX_KEYS_PER_NODE = 5; // Example value, adjust as needed
    private static BTreeIndex instance = new BTreeIndex();
    public BTreeIndex() {
        this.indexes = new java.util.HashMap<>();
    }
    public static BTreeIndex getInstance() {
        return instance;
    }

    public void buildIndexing() {
        File dbsDir = new File(DATABASES_DIRECTORY);
        for (String dbName : dbsDir.list()) {
            File dbDir = new File(DATABASES_DIRECTORY + dbName);
            for (File collectionFile : dbDir.listFiles()) {
                if (collectionFile.isFile()) {
                    String collectionName = collectionFile.getName().replace(".json", "");
                    buildIndexingForCollection(dbName, collectionName);
                }
            }
        }
    }

    private void buildIndexingForCollection(String dbName, String collectionName) {
        BTreeNode root = new BTreeNode();
        root.dbName = dbName;
        indexes.putIfAbsent(dbName, root);

        // implementation to build indexing for a specific collection in the database
        // each document has a unique _id field
        JSONArray documents = readCollection(dbName, collectionName);
        if (documents != null) {
            for (int i = 0; i < documents.length(); i++) {
                JSONObject document = documents.getJSONObject(i);
                String documentId = document.optString("_id");
                if (documentId != null && !documentId.isEmpty()) {
                    addIndex(dbName, collectionName, "_id", documentId);
                }
            }
        }
    }

    private JSONArray readCollection(String dbName, String collectionName) {
        String collectionFilePath = DATABASES_DIRECTORY + dbName + "/" + collectionName + ".json";
        File collectionFile = new File(collectionFilePath);

        if (!collectionFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(collectionFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return new JSONArray(content.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addIndex(String dbName, String collectionName, String propertyName, String documentId) {
        BTreeNode root = indexes.getOrDefault(dbName, new BTreeNode());
        insert(root, propertyName, documentId);
        indexes.put(dbName, root);
    }

    private void insert(BTreeNode node, String key, String documentId) {
        int idx = Collections.binarySearch(node.keys, key);
        if (idx >= 0) { // Key already exists
            node.documentIds.add(idx, documentId);
        } else {
            idx = -(idx + 1); // Insertion point
            if (node.isLeaf) {
                node.keys.add(idx, key);
                node.documentIds.add(idx, documentId);
            } else {
                insert(node.children.get(idx), key, documentId);
                if (node.keys.size() == node.children.size() - 1) {
                    splitChild(node, idx);
                }
            }
        }
    }

    private void splitChild(BTreeNode parentNode, int idx) {
        BTreeNode leftChild = parentNode.children.get(idx);
        BTreeNode rightChild = new BTreeNode();
        rightChild.isLeaf = leftChild.isLeaf;

        int midIdx = leftChild.keys.size() / 2;
        rightChild.keys.addAll(leftChild.keys.subList(midIdx, leftChild.keys.size()));
        rightChild.documentIds.addAll(leftChild.documentIds.subList(midIdx, leftChild.documentIds.size()));
        leftChild.keys.subList(midIdx, leftChild.keys.size()).clear();
        leftChild.documentIds.subList(midIdx, leftChild.documentIds.size()).clear();

        parentNode.keys.add(idx, leftChild.keys.get(midIdx - 1));
        parentNode.children.add(idx + 1, rightChild);
    }

    public List<String> search(String dbName, String propertyName, String key) {
        BTreeNode root = indexes.get(dbName);
        if (root == null) return new java.util.ArrayList<>(); // Database not found
        return search(root, propertyName, key);
    }

    private List<String> search(BTreeNode node, String propertyName, String key) {
        int idx = Collections.binarySearch(node.keys, key);
        if (idx >= 0) { // Key found
            if (node.isLeaf) {
                return node.documentIds.subList(idx, idx + 1);
            } else {
                return search(node.children.get(idx + 1), propertyName, key);
            }
        } else {
            idx = -(idx + 1); // Insertion point
            if (node.isLeaf) {
                return new java.util.ArrayList<>(); // Key not found
            } else {
                return search(node.children.get(idx), propertyName, key);
            }
        }
    }

    public JSONArray getMatchingDocs(String dbName, String collectionName, String propertyName, String key) {
        BTreeNode root = indexes.get(dbName);
        if (root == null) return new JSONArray(); // Database not found
        List<String> documentIds = search(root, propertyName, key);
        JSONArray result = new JSONArray();
        for (String documentId : documentIds) {
            JSONObject document = getDocumentById(dbName, collectionName, documentId);
            if (document != null) {
                result.put(document);
            }
        }
        return result;
    }

    public JSONObject getDocumentById(String dbName, String collectionName, String id) {
        BTreeNode root = indexes.get(dbName);
        if (root == null) {
            return new JSONObject(); // Database not found, return an empty JSONObject
        }
        return searchById(root, collectionName, id);
    }

    private JSONObject searchById(BTreeNode node, String collectionName, String id) {
        if (node == null) {
            return new JSONObject(); // Node is null, return an empty JSONObject
        }

        if (node.isLeaf) {
            // If the node is a leaf, iterate through its documentIds to find the document with the given ID
            for (int i = 0; i < node.documentIds.size(); i++) {
                if (Objects.equals(node.documentIds.get(i), id)) {
                    // Read the collection file corresponding to the collectionName
                    String collectionFilePath = DATABASES_DIRECTORY + node.dbName + "/" + collectionName + ".json";
                    File collectionFile = new File(collectionFilePath);
                    JSONArray collectionData = new JSONArray(readFileAsString(collectionFile));
                    // Return the document as a JSONObject
                    return collectionData.getJSONObject(i);
                }
            }
            return new JSONObject(); // Document with the given ID not found, return an empty JSONObject
        } else {
            // If the node is not a leaf, recursively search in its children
            int i = 0;
            while (i < node.keys.size() && id.compareTo(node.keys.get(i)) > 0) {
                i++;
            }
            if (i < node.keys.size() && id.compareTo(node.keys.get(i)) == 0) {
                // If the current node contains the ID, search in its right child
                return searchById(node.children.get(i + 1), collectionName, id);
            } else {
                // If the current node does not contain the ID, search in its left child
                return searchById(node.children.get(i), collectionName, id);
            }
        }
    }

    // Placeholder method for updating document indexing
    public void updateDocumentIndexing(String dbName, String collectionName, JSONObject updatedDoc) {
        BTreeNode root = indexes.getOrDefault(dbName, new BTreeNode());
        updateDocument(root, dbName, collectionName, updatedDoc);
        indexes.put(dbName, root);
    }

    private void updateDocument(BTreeNode node, String dbName, String collectionName, JSONObject updatedDoc) {
        if (node.isLeaf) {
            // If the node is a leaf, iterate through its keys to find the document
            for (int i = 0; i < node.keys.size(); i++) {
                if (node.documentIds.get(i).equals(updatedDoc.getString("_id"))) {
                    // Found the document, update its properties in the index
                    node.keys.set(i, updatedDoc.getString("propertyName")); // Update property name
                    node.documentIds.set(i, updatedDoc.getString("_id")); // Update document ID
                    // You may need to update other properties depending on your index structure
                    break;
                }
            }
        } else {
            // If the node is not a leaf, recursively search in its children
            int i;
            for (i = 0; i < node.keys.size(); i++) {
                if (updatedDoc.getString("propertyName").compareTo(node.keys.get(i)) < 0) {
                    // Search in the left child
                    updateDocument(node.children.get(i), dbName, collectionName, updatedDoc);
                    break;
                }
            }
            if (i == node.keys.size()) {
                // Search in the rightmost child
                updateDocument(node.children.get(i), dbName, collectionName, updatedDoc);
            }
        }
    }

    public void indexingNewObject(String dbName, String collectionName, JSONObject jsonObject) {
        BTreeNode root = indexes.getOrDefault(dbName, new BTreeNode());
        insertNewObject(root, dbName, collectionName, jsonObject);
        indexes.put(dbName, root);
    }

    // insert the new object into the B-tree
    // Traverse the B-tree to find the appropriate leaf node
    // Insert the new object into the leaf node
    // Split the leaf node or propagate any changes up to the parent nodes if necessary
    // Update the B-tree root node if it has changed
    private void insertNewObject(BTreeNode node, String dbName, String collectionName, JSONObject jsonObject) {
        if (node.isLeaf) {
            // If the node is a leaf, insert the new object into it
            insertIntoLeafNode(node, collectionName, jsonObject);
            if (node.keys.size() > MAX_KEYS_PER_NODE) {
                // If the leaf node is full after the insertion, split it
                splitLeafNode(node, dbName, collectionName);
            }
        } else {
            // If the node is not a leaf, recursively traverse its children to find the appropriate leaf node
            int i = 0;
            while (i < node.keys.size() && jsonObject.getString("_id").compareTo(node.keys.get(i)) > 0) {
                i++;
            }
            insertNewObject(node.children.get(i), dbName, collectionName, jsonObject);
            if (node.keys.size() > MAX_KEYS_PER_NODE) {
                // If the child node split and caused the current node to overflow, split the current node
                splitInternalNode(node, dbName, collectionName);
            }
        }
    }
    private void insertIntoLeafNode(BTreeNode node, String collectionName, JSONObject jsonObject) {
        // Insert the new object into the leaf node in sorted order
        int idx = Collections.binarySearch(node.keys, jsonObject.getString("_id"));
        if (idx >= 0) {
            // If the object with the same _id already exists, update it
            node.documentIds.set(idx, jsonObject.getString("_id"));
        } else {
            idx = -(idx + 1);
            node.keys.add(idx, jsonObject.getString("_id"));
            node.documentIds.add(idx, jsonObject.getString("_id"));
        }
    }

    private void splitLeafNode(BTreeNode node, String dbName, String collectionName) {
        // Split the leaf node into two nodes
        BTreeNode leftNode = new BTreeNode();
        BTreeNode rightNode = new BTreeNode();

        // Copy half of the keys and documentIds to the right node
        int midIdx = node.keys.size() / 2;
        rightNode.keys.addAll(node.keys.subList(midIdx, node.keys.size()));
        rightNode.documentIds.addAll(node.documentIds.subList(midIdx, node.documentIds.size()));
        node.keys.subList(midIdx, node.keys.size()).clear();
        node.documentIds.subList(midIdx, node.documentIds.size()).clear();

        // Update the links of the leaf nodes
        leftNode.nextLeaf = rightNode;
        rightNode.prevLeaf = leftNode;

        // Update the parent node
        if (node.parent == null) {
            // If the node is the root, create a new root node
            BTreeNode newRoot = new BTreeNode();
            newRoot.keys.add(rightNode.keys.get(0));
            newRoot.children.add(leftNode);
            newRoot.children.add(rightNode);
            leftNode.parent = newRoot;
            rightNode.parent = newRoot;
            indexes.put(dbName, newRoot);
        } else {
            // Otherwise, insert the median key into the parent node
            int parentIdx = Collections.binarySearch(node.parent.keys, node.keys.get(0));
            if (parentIdx >= 0) {
                node.parent.documentIds.set(parentIdx, rightNode.keys.get(0));
            } else {
                parentIdx = -(parentIdx + 1);
                node.parent.keys.add(parentIdx, rightNode.keys.get(0));
                node.parent.children.add(parentIdx + 1, rightNode);
                rightNode.parent = node.parent;
            }
        }
    }

    private void splitInternalNode(BTreeNode node, String dbName, String collectionName) {
        // Split the internal node into two nodes
        BTreeNode leftNode = new BTreeNode();
        BTreeNode rightNode = new BTreeNode();

        // Copy half of the keys and children to the right node
        int midIdx = node.keys.size() / 2;
        rightNode.keys.addAll(node.keys.subList(midIdx + 1, node.keys.size()));
        rightNode.children.addAll(node.children.subList(midIdx + 1, node.children.size()));
        node.keys.subList(midIdx, node.keys.size()).clear();
        node.children.subList(midIdx + 1, node.children.size()).clear();

        // Update the parent node
        if (node.parent == null) {
            // If the node is the root, create a new root node
            BTreeNode newRoot = new BTreeNode();
            newRoot.keys.add(node.keys.get(midIdx));
            newRoot.children.add(leftNode);
            newRoot.children.add(rightNode);
            leftNode.parent = newRoot;
            rightNode.parent = newRoot;
            indexes.put(dbName, newRoot);
        } else {
            // Otherwise, insert the median key into the parent node
            int parentIdx = Collections.binarySearch(node.parent.keys, node.keys.get(midIdx));
            if (parentIdx >= 0) {
                node.parent.documentIds.set(parentIdx, node.keys.get(midIdx));
            } else {
                parentIdx = -(parentIdx + 1);
                node.parent.keys.add(parentIdx, node.keys.get(midIdx));
                node.parent.children.add(parentIdx + 1, rightNode);
                rightNode.parent = node.parent;
            }
        }
    }

    public void clearIndex(String dbName) {
        indexes.remove(dbName);
    }

    public void clearCollectionIndexing(String dbName, String collectionName) {
        BTreeNode root = indexes.get(dbName);
        if (root != null) {
            clearCollection(root, collectionName);
        }
    }

    private void clearCollection(BTreeNode node, String collectionName) {
        if (node == null) {
            return;
        }

        if (node.isLeaf) {
            for (int i = 0; i < node.keys.size(); i++) {
                if (node.keys.get(i).equals(collectionName)) {
                    node.keys.remove(i);
                    node.documentIds.remove(i);
                    break;
                }
            }
        } else {
            for (BTreeNode child : node.children) {
                clearCollection(child, collectionName);
            }
        }
    }

    // method for removing document indexing
    public void clearDocumentIndexing(String dbName, String collectionName, String docId) {
        BTreeNode root = indexes.getOrDefault(dbName, new BTreeNode());
        removeDocument(root, dbName, collectionName, docId);
        indexes.put(dbName, root);
    }

    private void removeDocument(BTreeNode node, String dbName, String collectionName, String docId) {
        if (node.isLeaf) {
            // If the node is a leaf, iterate through its keys to find the document
            for (int i = 0; i < node.keys.size(); i++) {
                if (node.documentIds.get(i).equals(docId)) {
                    // Found the document, remove its properties from the index
                    node.keys.remove(i); // Remove property name
                    node.documentIds.remove(i); // Remove document ID
                    // You may need to remove other properties depending on your index structure
                    break;
                }
            }
        } else {
            // If the node is not a leaf, recursively search in its children
            int i;
            for (i = 0; i < node.keys.size(); i++) {
                if (docId.compareTo(node.keys.get(i)) < 0) {
                    // Search in the left child
                    removeDocument(node.children.get(i), dbName, collectionName, docId);
                    break;
                }
            }
            if (i == node.keys.size()) {
                // Search in the rightmost child
                removeDocument(node.children.get(i), dbName, collectionName, docId);
            }
        }
    }

}
