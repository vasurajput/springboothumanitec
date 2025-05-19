package com.javadream.SpringbootHumanitec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ApiService {
    @Autowired
    private WebClientHelper webClientHelper;

    public Mono<String> makeGenericGetCall() {
        String url = "http://localhost:8081/user";
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer YOUR_JWT",
                "Custom-Header", "CustomValue"
        );
        return webClientHelper.get(url, headers, String.class);
    }

    public Mono<String> makeGenericPostCall() {
        String url = "https://api.example.com/submit";
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer YOUR_JWT"
        );

        // Can be a POJO or Map<String, Object>
        Map<String, Object> payload = Map.of(
                "name", "Harry",
                "age", 30
        );

        return webClientHelper.post(url, payload, headers, String.class);
    }
}
