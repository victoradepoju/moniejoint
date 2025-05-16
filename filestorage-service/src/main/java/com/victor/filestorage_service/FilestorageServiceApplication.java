package com.victor.filestorage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FilestorageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilestorageServiceApplication.class, args);
	}

}
