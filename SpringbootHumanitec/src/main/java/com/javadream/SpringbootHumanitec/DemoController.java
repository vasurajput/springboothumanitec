package com.javadream.SpringbootHumanitec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    @Autowired
    private ApiService service;

    @GetMapping("/test")
    public Mono<Object> test() {
        Mono<Object> stringMono = service.makeGenericGetCall();
        return stringMono;
    }

}
