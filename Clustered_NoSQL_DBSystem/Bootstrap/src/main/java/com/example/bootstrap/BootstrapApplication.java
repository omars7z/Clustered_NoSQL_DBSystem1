package com.example.bootstrap;

import com.example.bootstrap.Service.Cluster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
        startApp();
    }

    private static void startApp(){
        Cluster.getInstance().setUpWorkersNames();
        Cluster.getInstance().setFirstWorkerAsAffinity();
    }
}
