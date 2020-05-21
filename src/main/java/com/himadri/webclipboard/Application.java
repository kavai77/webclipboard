package com.himadri.webclipboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        if (System.getenv("PORT") != null) {
            String port = System.getenv("PORT");
            app.setDefaultProperties(Collections.singletonMap("server.port", port));
        }

        app.run(args);
    }

    @Bean
    public GoogleCloudRuntime runtime() {
        return System.getenv("GAE_SERVICE") != null ? GoogleCloudRuntime.CLOUD : GoogleCloudRuntime.LOCAL;
    }

    public enum GoogleCloudRuntime {
        CLOUD, LOCAL
    }
}