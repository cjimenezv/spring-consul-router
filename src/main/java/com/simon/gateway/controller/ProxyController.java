package com.simon.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/items")
    public String getItems(@RequestParam(required = false) String path) {
        // El nombre del servicio debe coincidir con el que se registra en Consul
        String serviceName = "srv-avl-backend"; 
        String url = "http://" + serviceName + "/items";

        // Si quieres agregar path dinámico desde query param
        if (path != null && !path.isEmpty()) {
            url += "?path=" + path;
        }

        return restTemplate.getForObject(url, String.class);
    }
}
