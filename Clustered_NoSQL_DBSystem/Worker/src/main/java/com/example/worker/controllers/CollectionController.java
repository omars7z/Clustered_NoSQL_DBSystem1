package com.example.worker.controllers;

import com.example.worker.authentication.AuthenticationService;
import com.example.worker.model.APIResponse;
    import com.example.worker.model.Schema;
import com.example.worker.services.ColService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;

import static com.example.worker.File.JSON.*;

@RestController
@RequestMapping("/api")
public class CollectionController {

    private final ColService colService;
    private final AuthenticationService authenticationService;
    private final Logger logger = LoggerFactory.getLogger(CollectionController.class);

    public CollectionController(ColService colService, AuthenticationService authenticationService) {
        this.colService = colService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("createCol/{db_name}/{collection_name}")
    @ResponseBody
    public APIResponse addCollection(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @RequestBody Schema schema,
            @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token
    ) {
        logger.info("Received request to create collection {} in database {} {} from user: {}", collectionName, dbName,
                (propagatedRequest ? "(propagated successfully!)" : ""), username);

        dbName = dbName.toLowerCase();
        collectionName = collectionName.toLowerCase();

        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.warn("User was not authorized.");
            return new APIResponse("User is not authorized.", 401);
        }

        if (propagatedRequest) {
            return colService.handlePropagatedCreateCollection(dbName, collectionName, new File(DATABASES_DIRECTORY + dbName), schema);
        } else {
            return colService.propagateCreateCollectionRequest(dbName, collectionName, schema);
        }
    }

    @DeleteMapping("/deleteCol/{db_name}/{collection_name}")
    @ResponseBody
    public APIResponse deleteCollection(
            @PathVariable("db_name") String dbName,
            @PathVariable("collection_name") String collectionName,
            @RequestHeader(value = "X-Propagated-Request", defaultValue = "false") boolean propagatedRequest,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token
    ) {
        logger.info("Received request to delete collection '{}.{}' {}", dbName, collectionName,
                (propagatedRequest ? "(propagated)" : ""));

        dbName = dbName.toLowerCase();
        collectionName = collectionName.toLowerCase();

        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.warn("User is not authorized to delete collection '{}.{}'.", dbName, collectionName);
            return new APIResponse("User is not authorized.", 401);
        }

        if (propagatedRequest) {
            return colService.handlePropagatedDeleteCollection(dbName, collectionName, new File(DATABASES_DIRECTORY + dbName));
        } else {
            return colService.propagateDeleteCollectionRequest(dbName, collectionName);
        }
    }

    @GetMapping("/filter/{db_name}/{collectionName}")
    @ResponseBody
    public APIResponse filterCollection(
            @PathVariable("db_name") String dbName,
            @PathVariable("collectionName") String collectionName,
            @RequestParam("attributeName") String attributeName,
            @RequestParam("attributeValue") String attributeValue,
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token
    ) {
        logger.info("Received request to filter collection '{}.{}' by attribute '{}={}'", dbName, collectionName, attributeName, attributeValue);
        dbName = dbName.toLowerCase();
        collectionName = collectionName.toLowerCase();
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.warn("User is not authorized to filter collection '{}.{}'.", dbName, collectionName);
            return new APIResponse("User is not authorized.", 401);
        }

        return colService.filterCollection(dbName, collectionName, attributeName, attributeValue);
    }

//    @GetMapping("getCollections/{db_name}")
//    public APIResponse getCollections(@PathVariable("db_name") String dbName,
//                                      @RequestHeader(value = "X-Username") String username,
//                                      @RequestHeader(value = "X-Token") String token) {
//        logger.info("Received request to get all collections in database '{}'", dbName);
//
//        dbName = dbName.toLowerCase();
//        if (!authenticationService.isAuthenticatedUser(username, token)) {
//            return new APIResponse("User is not authorized.", 401);
//        }
//
//        return colService.getCollections(dbName);
//    }

}
