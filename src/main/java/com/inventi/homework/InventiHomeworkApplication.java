package com.inventi.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.inventi.homework"})
public class InventiHomeworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventiHomeworkApplication.class, args);
    }
}