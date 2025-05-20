package com.javadream.SpringbootHumanitec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadream.SpringbootHumanitec.exception.CustomApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ApiService {
    @Autowired
    private WebClientHelper webClientHelper;

    public Mono<Object> makeGenericGetCall() {
        String url = "http://localhost:8081/user";
        Map<String, String> headers = Map.of(
                "Authorization", "Bearer YOUR_JWT",
                "Custom-Header", "CustomValue"
        );
        Mono<Object> json = webClientHelper.get(url, headers, String.class).map(jsonStr -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(jsonStr, Object.class); // or use Map.class
            } catch (Exception e) {
                throw new CustomApiException(500,"Error While Parsing Json","Method makeGenericGetCall");
            }
        });
        return json;
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
