package com.example.demo.Services;

import java.util.HashMap;

public class WorkerService {

    private static WorkerService instance = null;
    private HashMap<String, Integer> tokenToWorkerPort = new HashMap<>();

    public static WorkerService getInstance() {
        if (instance == null) {
            instance = new WorkerService();
        }
        return instance;
    }

    public String getWorkerName(int port) {
        return "worker" + (port - 8080);
    }

    public void addWorkerPortToToken(String token, Integer workerPort) {
        tokenToWorkerPort.put(token, workerPort);
    }

    public int getWorkerPort(String token) {
        Integer workerPort = tokenToWorkerPort.get(token);
        if (workerPort != null) {
            return workerPort.intValue();
        } else {
            return 8081;
        }
    }


}
