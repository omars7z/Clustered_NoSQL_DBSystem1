package com.example.worker.Controllers;

import com.example.worker.Model.APIResponse;
import com.example.worker.Services.AffinityServices;
import com.example.worker.Authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AffinityController {
    private AffinityServices affinityServices = AffinityServices.getInstance();
    private AuthenticationService authenticationService;
    private Logger logger = LoggerFactory.getLogger(AffinityController.class);

    @Autowired
    public AffinityController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/isAffinity")
    public boolean isAffinity(@RequestHeader(value = "X-Username") String username,
                              @RequestHeader(value = "X-Token") String token) {
        logger.info("Current worker" + affinityServices.getCurrentWorkerName() + (affinityServices.isCurrentWorkerAffinity() ? " is " : " is not ") + "affinity!");
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("Non registered user");
            return false;
        }
        return affinityServices.isCurrentWorkerAffinity();
    }

    @GetMapping("/setAffinity")
    public APIResponse setAffinity(
            @RequestHeader(value = "X-Username") String username,
            @RequestHeader(value = "X-Token") String token) {
        logger.info("Current worker (" + affinityServices.getCurrentWorkerName() + ") is set to be Affinity");
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("Non registered user");
            return new APIResponse("Non registered user!", 500);
        }
        affinityServices.setCurrentWorkerAffinity();
        return new APIResponse("Affinity set successfully!", 200);
    }

    @GetMapping("/unsetAffinity")
    public APIResponse unsetAffinity(@RequestHeader(value = "X-Username") String username,
                                     @RequestHeader(value = "X-Token") String token) {

        logger.info("Current worker" + affinityServices.getCurrentWorkerName() + " is set to be not affinity!");
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("Non registered user");
            return new APIResponse("Non registered user!", 500);
        }
        affinityServices.unsetCurrentWorkerAffinity();
        return new APIResponse("Affinity unset successfully!", 200);
    }


    @GetMapping("/setCurrentWorkerName/{name}")
    public APIResponse setCurrentWorkerName(@PathVariable("name") String name,
                                            @RequestHeader(value = "X-Username") String username,
                                            @RequestHeader(value = "X-Token") String token) {
        logger.info("Current worker name is set to: " + name);
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("Non registered user");
            return new APIResponse("Non registered user!", 500);
        }
        affinityServices.setCurrentWorkerName(name);
        return new APIResponse("Current worker name set successfully!", 200);
    }

    @GetMapping("/getCurrentWorkerName")
    public String getCurrentWorkerName(@RequestHeader(value = "X-Username") String username,
                                       @RequestHeader(value = "X-Token") String token) {
        logger.info("Current worker name is: " + affinityServices.getCurrentWorkerName());
        if (!authenticationService.isAuthenticatedUser(username, token)) {
            logger.info("Non registered user");
            return null;
        }
        return affinityServices.getCurrentWorkerName();
    }

    @GetMapping("/addAffinityData/{id}/{workerName}")
    public APIResponse addAffinityData(@PathVariable("id") String id,
                                       @PathVariable("workerName") String workerName,
                                       @RequestHeader(value = "X-Username") String adminUserName,
                                       @RequestHeader(value = "X-Token") String adminToken) {
        if (!authenticationService.isAdmin(adminUserName, adminToken)) {
            logger.info("Non registered admin");
            return new APIResponse("Non registered admin!", 500);
        }
        affinityServices.addAffinity(id, workerName);
        logger.info("added the affinity data for id: " + id + " and workerName: " + workerName);
        return new APIResponse("Affinity data added successfully!", 200);
    }
}
