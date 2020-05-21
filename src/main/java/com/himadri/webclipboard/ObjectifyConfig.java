package com.himadri.webclipboard;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.DatastoreOptions;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.himadri.webclipboard.entity.Clipboard;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class ObjectifyConfig {

    @Bean
    public FilterRegistrationBean<ObjectifyFilter> objectifyFilterRegistration() {
        final FilterRegistrationBean<ObjectifyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ObjectifyFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public ServletListenerRegistrationBean<ObjectifyListener> listenerRegistrationBean(Application.GoogleCloudRuntime runtime) {
        ServletListenerRegistrationBean<ObjectifyListener> bean =
            new ServletListenerRegistrationBean<>();
        bean.setListener(new ObjectifyListener(runtime));
        return bean;
    }

    @WebListener
    public static class ObjectifyListener implements ServletContextListener {
        private final Application.GoogleCloudRuntime runtime;

        public ObjectifyListener(Application.GoogleCloudRuntime runtime) {
            this.runtime = runtime;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            if (runtime == Application.GoogleCloudRuntime.LOCAL) {
                try {
                    ObjectifyService.init(new ObjectifyFactory(
                        DatastoreOptions.newBuilder()
                            .setHost("localhost:8167")
                            .setProjectId("my-project")
                            .setCredentials(GoogleCredentials.fromStream(new FileInputStream(
                                System.getenv("HOME") + "/.config/gcloud/application_default_credentials.json")))
                            .build()
                            .getService()
                    ));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                ObjectifyService.init(new ObjectifyFactory(
                    DatastoreOptions.getDefaultInstance().getService()
                ));
            }

            ObjectifyService.register(Clipboard.class);
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {

        }
    }


}