package com.example.bootstrap.Controller;

import com.example.bootstrap.Model.Broadcast;
import com.example.bootstrap.Model.APIResponse;
import com.example.bootstrap.Service.Cluster;
import com.example.bootstrap.Service.Tokens;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Cluster cluster;
    private final Tokens tokens;
    private final APIResponse apiResponse;
    private final Broadcast broadcast;
    private final Logger logger = Logger.getLogger(Controller.class.getName());

    @Autowired
    public Controller(Cluster cluster, Tokens tokens, APIResponse apiResponse, Broadcast broadcast) {
        this.cluster = cluster;
        this.tokens = tokens;
        this.apiResponse = apiResponse;
        this.broadcast = broadcast;
    }

    @GetMapping("/register/{username}")
    public String register(@PathVariable("username") String username) {
        String token = UUID.randomUUID().toString();
        String workerName = broadcast.getNextWorker();
        cluster.sendUserInfo(username, token, workerName);

        JSONObject userCredentialsJSON = new JSONObject();
        int port = broadcast.getWorkerPort(workerName);
        userCredentialsJSON.put("username", username);
        userCredentialsJSON.put("token", token);
        userCredentialsJSON.put("workerPort", port);

        tokens.addNewToken(username, token, workerName);
        apiResponse.generateSuccessResponse("User registered successfully", userCredentialsJSON.toString());
        return userCredentialsJSON.toString();
    }

    @GetMapping("/getAllUsers")
    public void getAllUsers() {
        logger.info("Received request to get all users");
        tokens.allUsers();
        apiResponse.generateSuccessResponse("Viewed all users successfully", tokens.allUsers());
    }

    @GetMapping("removeUser/{token}")
    public void removeUser(@PathVariable("token") String token) {
        logger.info("Received request to remove user with token " + token);
        tokens.removeUser(token); //remove user by token because its unique
        apiResponse.generateSuccessResponse("User registered successfully", tokens.allUsers());
    }
}
