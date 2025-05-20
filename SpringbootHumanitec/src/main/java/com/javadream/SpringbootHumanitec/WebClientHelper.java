package com.javadream.SpringbootHumanitec;

import com.javadream.SpringbootHumanitec.exception.CustomApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        this.webClient = WebClient.builder().build();
    }

    public <T> Mono<T> get(String url, Map<String, String> headers, Class<T> responseType) {
        String location = "WebClientHelper::GET " + url;
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) headers.forEach(httpHeaders::add);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Unknown error")
                                .flatMap(errorBody ->
                                        Mono.error(new CustomApiException(response.statusCode().value(), errorBody, location))
                                )
                )
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("WebClient GET error at {}: {} - StackTrace:", location, ex.getMessage(), ex);
                    return Mono.error(new CustomApiException(
                            ex.getStatusCode().value(),
                            ex.getResponseBodyAsString(),
                            location
                    ));
                })
                .onErrorResume(ex -> {
                    log.error("Unknown WebClient GET error at {}: {}", location, ex.getMessage(), ex);
                    return Mono.error(new CustomApiException(500, "Unexpected error occurred while calling API", location));
                });
    }

    public <T> Mono<T> post(String url, Object payload, Map<String, String> headers, Class<T> responseType) {
        String location = "WebClientHelper::POST " + url;
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
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Unknown error")
                                .flatMap(errorBody ->
                                        Mono.error(new CustomApiException(response.statusCode().value(), errorBody, location))
                                )
                )
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("WebClient POST error at {}: {} - StackTrace:", location, ex.getMessage(), ex);
                    return Mono.error(new CustomApiException(
                            ex.getStatusCode().value(), // <-- use this instead of getRawStatusCode()
                            ex.getResponseBodyAsString(),
                            location
                    ));
                })
                .onErrorResume(ex -> {
                    log.error("Unknown WebClient POST error at {}: {}", location, ex.getMessage(), ex);
                    return Mono.error(new CustomApiException(500, "Unexpected error occurred while calling API", location));
                });
    }
}
