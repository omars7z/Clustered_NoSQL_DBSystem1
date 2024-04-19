package com.example.worker.threading;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class AuthLock {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void appendJsonObjectToFile(File file, String jsonObject) throws IOException {
        lock.lock();
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(jsonObject + "\n");
        } finally {
            lock.unlock();
        }
    }

    public static void writeJsonArrayToFile(File file, String jsonArray) throws IOException {
        lock.lock();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonArray);
        } finally {
            lock.unlock();
        }
    }
}
