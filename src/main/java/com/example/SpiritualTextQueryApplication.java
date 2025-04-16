package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Spiritual Text Query service.
 * Provides Bible search functionality through REST APIs.
 */
@SpringBootApplication
public class SpiritualTextQueryApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SpiritualTextQueryApplication.class, args);
    }

}
