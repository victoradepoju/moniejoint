package com.victor.filestorage_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GCPConfig {

    @Bean
    public Storage storage() throws IOException {
        InputStream keyStream = new FileInputStream("/Users/victoradepoju/ms/filestorage-service/trusty-fuze-456211-j9-c9a4dbbef9e1.json");
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyStream))
                .build()
                .getService();
    }
}
