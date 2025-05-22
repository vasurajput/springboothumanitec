package com.javadream.common.webclient;

import com.javadream.common.exception.ExternalApiException;
import com.javadream.common.mapping.ApiMappingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    private final WebClient.Builder webClientBuilder;
    private final ApiMappingService apiMappingService;

    public <T> Mono<T> callMono(String apiKey,
                                Object queryParams,
                                Map<String, String> pathParams,
                                Map<String, String> headers,
                                Class<T> responseType) {

        URI uri = buildFinalUri(resolveUrl(apiKey, pathParams), toQueryMap(queryParams));

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::add); // ✅ Add all custom headers
                    }
                })
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ExternalApiException("Client Error: " + body)))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ExternalApiException("Server Error: " + body)))
                )
                .bodyToMono(responseType);
    }

    public <T> Flux<T> callFlux(String apiKey,
                                Object queryParams,
                                Map<String, String> pathParams,
                                Map<String, String> headers,
                                Class<T> responseType) {

        String url = resolveUrl(apiKey, pathParams);

        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path(url);
                    toQueryMap(queryParams).forEach((key, value) ->
                            builder.queryParam(key, value == null ? "" : value));
                    URI finalUri = builder.build();
                    log.info("Calling third-party API for key: {} : {}", apiKey, finalUri);
                    return builder.build();
                })
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::add); // ✅ Add all custom headers
                    }
                })
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ExternalApiException("Client Error: " + body)))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ExternalApiException("Server Error: " + body)))
                )
                .bodyToFlux(responseType);
    }

    public static URI buildFinalUri(String baseUrl, Map<String, String> queryParams) {
        MultiValueMap<String, String> multiQueryParams = queryParams.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.of(e.getValue() == null ? "" : e.getValue()),
                        (a, b) -> b,
                        LinkedMultiValueMap::new
                ));

        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParams(multiQueryParams)
                .build(true)
                .toUri();
    }

    private String resolveUrl(String apiKey, Map<String, String> pathParams) {
        String urlTemplate = apiMappingService.resolve(apiKey);
        if (urlTemplate == null) {
            throw new ExternalApiException("No API mapping found for key: " + apiKey);
        }
        if (pathParams != null) {
            for (Map.Entry<String, String> entry : pathParams.entrySet()) {
                urlTemplate = urlTemplate.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return urlTemplate;
    }

    public static Map<String, String> toQueryMap(Object headersSource) {
        if (headersSource == null) return Map.of();

        if (headersSource instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue())
                    ));
        }

        // fallback for POJO
        Map<String, String> result = new HashMap<>();
        for (Field field : headersSource.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(headersSource);
                result.put(field.getName(), value == null ? "" : String.valueOf(value));
            } catch (IllegalAccessException ignored) {
            }
        }

        return result;
    }
}
