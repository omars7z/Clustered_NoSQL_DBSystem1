package com.example.bootstrap.Controller;

import com.example.bootstrap.Model.Broadcast;
import com.example.bootstrap.Model.APIResponse;
import com.example.bootstrap.Service.Cluster;
import com.example.bootstrap.Service.Tokens;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Cluster cluster;
    private final Tokens tokens;
    private final APIResponse apiResponse;
    private final Broadcast broadcast;

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

        Tokens.getInstance().addNewToken(username, token, workerName);
        return apiResponse.generateSuccessResponse("User registered successfully", userCredentialsJSON.toString());
    }

    @GetMapping("/getAllUsers")
    public String getAllUsers() {
        return apiResponse.generateSuccessResponse("All users retrieved successfully", tokens.allUsers());
    }

    @DeleteMapping("/removeUser/{username}")
    public String removeUser(@PathVariable("username") String username) {
        tokens.removeUser(username); // Remove user by username
        return apiResponse.generateSuccessResponse("User removed successfully", null);
    }
}
