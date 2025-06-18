package com.parkandride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@Configuration: Tags the class as a source of bean definitions for the application context.
//@EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings
//@ComponentScan: Tells Spring to look for other components and services in the com.parkandride package.
@EnableScheduling
//allowing you to schedule methods to be executed at fixed intervals
public class ParkAndRideApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkAndRideApplication.class, args);
    }
} 