package com.javadream.common.mapping;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "api.mappings.bene")
@Data
public class ApiMappingService {

    private Map<String, String> endpoints;

    @PostConstruct
    public void init() {
        System.out.println("Loaded API mappings: " + endpoints);
    }

    public String resolve(String key) {
        return endpoints.get(key);
    }
}
