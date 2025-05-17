package com.javadream.SpringbootHumanitec;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Spring boot humanitec example with java version: " + System.getProperty("java.version"));
    }

}
