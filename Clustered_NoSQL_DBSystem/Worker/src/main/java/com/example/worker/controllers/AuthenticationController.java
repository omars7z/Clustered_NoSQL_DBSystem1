package com.example.worker.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.worker.model.APIResponse;
import com.example.worker.services.AffinityServices;
import com.example.worker.authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private Logger logger = LoggerFactory.getLogger(AffinityController.class);

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/isAdmin/{username}/{token}")
    public boolean isAdmin(@PathVariable String username, @PathVariable String token) {
        return authenticationService.isAdmin(username, token);
    }

    @GetMapping("/isUser/{username}/{token}")
    public boolean isUser(@PathVariable String username, @PathVariable String token) {
        return authenticationService.isAuthenticatedUser(username, token);
    }

    @GetMapping("/addAuthenticatedUser/{username}/{token}")
    public APIResponse addAuthenticatedUser(@PathVariable("username") String username, @PathVariable("token") String token,
                                            @RequestHeader(value = "X-Username") String adminUserName,
                                            @RequestHeader(value = "X-Token") String adminToken) {
        logger.info("adding the user: " + username + " with token: " + token);
        if (!authenticationService.isAdmin(adminUserName, adminToken)) {
            logger.info("Non registered admin");
            return new APIResponse("Non registered admin!", 500);
        }

        authenticationService.addUser(username, token);
        return new APIResponse("User added successfully!", 200);
    }

    @GetMapping("addAdmin/{newAdminame}/{newAdminToken}")
    public APIResponse addAdmin(@PathVariable("newAdminame") String newUsername, @PathVariable("newAdminToken") String newAdminToken
            , @RequestHeader(value = "X-Username") String adminUserName
            , @RequestHeader(value = "X-Token") String adminToken) throws IOException {

        if (!authenticationService.isAdmin(adminUserName, adminToken)) {
            return new APIResponse("Non registered admin hasn't the ability to add new admins!", 500);
        }

        authenticationService.addAdmin(newUsername, newAdminToken);
        return new APIResponse("Admin added successfully!", 200);
    }

    @DeleteMapping("/removeAuthenticatedUser/{username}/{token}")
    public APIResponse removeAuthenticatedUser(
            @PathVariable("username") String username, @PathVariable("token") String token
            , @RequestHeader(value = "X-Username") String adminUserName
            , @RequestHeader(value = "X-Token") String adminToken) {

        logger.info("removing the user: " + username + " with token: " + token);
        if (!authenticationService.isAdmin(adminUserName, adminToken)) {
            logger.info("Non registered admin");
            return new APIResponse("Non registered admin!", 500);
        }

        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("User is not registered already!");
            return new APIResponse("User is not registered already!", 500);
        }

        authenticationService.removeUser(username, token);
        return new APIResponse("User removed successfully!", 200);
    }
}
