package com.victor.saving_group_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.saving_group_service.exception.WalletServiceException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Slf4j
public class WalletServiceErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String url = response.request().url();
        log.info("Request to url: {}", url);
        String message = "Wallet service error";
        try {
            if (response.body() != null) {
                // If the response has a body, try to extract the error message
                String body = Util.toString(response.body().asReader(Util.UTF_8));
                message = extractErrorMessage(body);
            }
        } catch (IOException ignored) {
            // If we can't read the body, use the default message
        }

        // Handle specific status codes
        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            return new WalletServiceException(
                    "Wallet not found: " + message,
                    HttpStatus.NOT_FOUND
            );
        }

        if (response.status() == HttpStatus.BAD_REQUEST.value()) {
            return new WalletServiceException(
                    "Invalid wallet request: " + message,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (response.status() == HttpStatus.CONFLICT.value()) {
            return new WalletServiceException(
                    "Wallet operation conflict: " + message,
                    HttpStatus.CONFLICT
            );
        }

        if (response.status() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
            return new WalletServiceException(
                    "Unprocessable wallet operation: " + message,
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // For all other cases, use the default decoder
        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String extractErrorMessage(String body) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(body);
            if (rootNode.has("message")) {
                return rootNode.get("message").asText();
            }
            if (rootNode.has("error")) {
                return rootNode.get("error").asText();
            }
        } catch (IOException e) {
            // If we can't parse the JSON, return the raw body
        }
        return body;
    }
}
