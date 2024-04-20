package com.example.bootstrap;

import com.example.bootstrap.Service.Cluster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);


        // Starting Up The Cluster
        Cluster.getInstance().setUpWorkersNames();
        Cluster.getInstance().setFirstWorkerAsAffinity();
    }
}
