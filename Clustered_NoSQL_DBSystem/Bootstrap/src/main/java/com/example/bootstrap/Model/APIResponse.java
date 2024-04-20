package com.example.bootstrap.Model;

import org.springframework.stereotype.Component;

@Component
public class APIResponse {

    public String generateSuccessResponse(String message, Object data) {
        return "{\"success\": true, \"message\": \"" + message + "\", \"data\": " + data + "}";
    }

    public String generateErrorResponse(String message) {
        return "{\"success\": false, \"message\": \"" + message + "\", \"data\": null}";
    }
}
