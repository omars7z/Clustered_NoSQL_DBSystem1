package com.example.demo;

import com.example.demo.DataBase.DBSchema;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        startup();
    }

    private static void startup() {
        DBSchema.buildDatabaseSchema();
    }

}
