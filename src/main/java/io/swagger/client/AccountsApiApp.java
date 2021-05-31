package io.swagger.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication
@EnableRetry
public class AccountsApiApp {

    public static void main(String[] args) {
        SpringApplication.run(AccountsApiApp.class, args);
    }
}
