package com.dukaanpe.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductInventoryServiceApplication.class, args);
    }
}

