package com.example.worker.controllers;

import com.example.worker.services.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.worker.authentication.AuthenticationService;
import com.example.worker.model.APIResponse;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private DocService docService;
    private AuthenticationService authenticationService;

    @Autowired
    public DocumentController(DocService docService, AuthenticationService authenticationService) {
        this.docService = docService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/insertDoc/{db_name}/{collection_name}")
    public APIResponse insertDoc(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @RequestBody String json,
            @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token
    ) {
        return docService.insertDocument(dbName, collectionName, json, propagatedRequest, username, token);
    }

    @GetMapping("/getDoc/{db_name}/{collection_name}/{doc_id}")
    @ResponseBody
    public APIResponse getDoc(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @PathVariable("doc_id") String docId,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token) {
        return docService.getDocument(dbName, collectionName, docId, username, token);
    }

    @GetMapping("/getAllDocs/{db_name}/{collection_name}")
    @ResponseBody
    public APIResponse getAll(
            @PathVariable("db_name") String db_name,
            @PathVariable("collection_name") String collection_name,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token
    ) {
        return docService.getAllDocuments(db_name, collection_name, username, token);
    }

    @DeleteMapping("/deleteDoc/{db_name}/{collection_name}/{doc_id}")
    @ResponseBody
    public APIResponse deleteDoc(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @PathVariable("doc_id") String docId,
            @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token) {
        return docService.deleteDocument(dbName, collectionName, docId, propagatedRequest, username, token);
    }

    @PostMapping("/updateDoc/{db_name}/{collection_name}/{doc_id}/{property_name}/{new_value}")
    @ResponseBody
    public APIResponse updateDoc(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @PathVariable("doc_id") String docId,
            @PathVariable("property_name") String propertyName,
            @PathVariable("new_value") Object newValue,
            @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token,
            @RequestHeader(value = "X-Old-Value", required = false) String oldValue
    ) {
        return docService.updateDocument(dbName, collectionName, docId, propertyName, newValue, propagatedRequest, username, token, oldValue);
    }
}
