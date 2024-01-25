package com.example.gitinfofetcher.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientResponseException(WebClientResponseException ex) {
        logger.error("WebClientResponseException: Status code: {} - Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);

        Map<String, Object> body = new HashMap<>();
        String errorMessage = "An error occurred while processing the request.";

        try {
            Map errorResponse = new ObjectMapper().readValue(ex.getResponseBodyAsString(), Map.class);
            errorMessage = (String) errorResponse.getOrDefault("message", "No message available");
        } catch (IOException e) {
            logger.error(errorMessage, e);
        }
        body.put("status", ex.getStatusCode().value());
        body.put("message", errorMessage);

        return new ResponseEntity<>(body, HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}
