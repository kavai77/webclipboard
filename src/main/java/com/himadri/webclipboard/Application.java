package com.himadri.webclipboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:/secrets.yml")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}