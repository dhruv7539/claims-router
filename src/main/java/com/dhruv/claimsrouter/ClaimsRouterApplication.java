package com.dhruv.claimsrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ClaimsRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsRouterApplication.class, args);
    }
}
