package com.javadream.SpringbootHumanitec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class WebClientHelper {

    private static final Logger log = LoggerFactory.getLogger(WebClientHelper.class);

    private final WebClient webClient;

    public WebClientHelper() {
        this.webClient = WebClient.builder()
                .build();
    }

    public <T> Mono<T> get(String url, Map<String, String> headers, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) headers.forEach(httpHeaders::add);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Unknown error")
                                .map(error -> new RuntimeException("Request failed: " + error))
                )
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("WebClient GET error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString()))
                .timeout(Duration.ofSeconds(10));
    }

    public <T> Mono<T> post(String url, Object payload, Map<String, String> headers, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) headers.forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Unknown error")
                                .map(error -> new RuntimeException("Request failed: " + error))
                )
                .bodyToMono(responseType)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("WebClient POST error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString()))
                .timeout(Duration.ofSeconds(10));
    }
}
