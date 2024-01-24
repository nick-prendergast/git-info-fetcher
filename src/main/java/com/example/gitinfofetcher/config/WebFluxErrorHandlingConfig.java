package com.example.gitinfofetcher.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.NotAcceptableStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebFluxErrorHandlingConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebFluxErrorHandlingConfig.class);

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectMapper objectMapper) {
        return (exchange, ex) -> {
            if (ex instanceof NotAcceptableStatusException) {
                logger.error("NotAcceptableStatusException: ", ex);
                Map<String, Object> body = new HashMap<>();
                body.put("status", HttpStatus.NOT_ACCEPTABLE.value());
                body.put("Message", "The requested media type is not supported");
                byte[] bytes;
                try {
                    bytes = objectMapper.writeValueAsBytes(body);
                } catch (JsonProcessingException e) {
                    logger.error("Error while processing JSON", e);
                    return Mono.error(e);
                }

                exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                logger.debug("Writing custom not acceptable response.");
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
            logger.error("Unhandled exception: ", ex);
            return Mono.error(ex);
        };
    }

}

