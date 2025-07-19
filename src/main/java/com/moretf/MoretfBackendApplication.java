package com.moretf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoretfBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoretfBackendApplication.class, args);
    }

}
