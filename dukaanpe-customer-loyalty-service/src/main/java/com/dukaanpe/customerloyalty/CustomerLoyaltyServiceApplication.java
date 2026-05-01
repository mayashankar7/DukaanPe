package com.dukaanpe.customerloyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerLoyaltyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerLoyaltyServiceApplication.class, args);
    }
}

