package com.zspt.blibli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BliBliApplication {

    public static void main(String[] args) {
        SpringApplication.run(BliBliApplication.class, args);
    }

}
