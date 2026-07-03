package com.cbcportal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CbcPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CbcPortalApplication.class, args);
        System.out.println("CBC Portal Backend is running!");
    }
}
