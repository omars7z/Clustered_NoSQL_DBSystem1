package com.example.bootstrap.Model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Broadcast {
    private int nextWorkerIndex = 0;
    private final List<String> workers = new ArrayList<>();

    public Broadcast() {
        workers.add("worker1");
        workers.add("worker2");
        workers.add("worker3");
    }

    public int getWorkerPort(String workerName) {
        return switch (workerName) {
            case "worker1" -> 8081;
            case "worker2" -> 8082;
            case "worker3" -> 8083;
            default -> 8081;
        };
    }

    public String getNextWorker() {
        String nextWorker = workers.get(nextWorkerIndex);
        nextWorkerIndex = (nextWorkerIndex + 1) % workers.size();
        return nextWorker;
    }
}
