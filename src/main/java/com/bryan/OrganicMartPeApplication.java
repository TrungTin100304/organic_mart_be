package com.bryan;

import com.bryan.config.RenderDatabaseUrlSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.bryan.config")
public class OrganicMartPeApplication {

    public static void main(String[] args) {
        RenderDatabaseUrlSupport.applyToSystemProperties();
        SpringApplication.run(OrganicMartPeApplication.class, args);
    }

}
