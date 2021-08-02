package io.swagger.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Properties;


@SpringBootApplication
@EnableRetry
public class ApiApp {

    private static Properties properties = new Properties();

    public static void main(String[] args) {
        SpringApplication.run(ApiApp.class, args);
    }
}
