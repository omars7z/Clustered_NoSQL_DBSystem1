package com.example.worker;

import com.example.worker.indexing.PropertyIndexManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
        startup();
    }

    private static void startup(){
        PropertyIndexManager.buildIndexing();
    }
}
