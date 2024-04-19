package com.example.worker.threading;

import com.example.worker.caching.LRUCache;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Threads {
    private static final Object lock = new Object();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void executeAsync(Runnable task) {
        executorService.submit(task);
    }

    public static void synchronizedPut(LRUCache<String, JSONArray> cache, File collectionFile, JSONArray collectionData) {
        synchronized (lock) {
            cache.put(collectionFile.getName(), collectionData);
        }
    }

    public static void closeExecutorService() {
        executorService.shutdown();
    }
}
