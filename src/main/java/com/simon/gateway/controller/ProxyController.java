package com.simon.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest; // 💡 ¡Necesitas esta importación!

@RestController
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    // Inyecta la petición actual para acceder a las cabeceras
    @GetMapping("/items")
    public ResponseEntity<String> getItems(HttpServletRequest request, 
                                           @RequestParam(required = false) String path) {

        // 1. CAPTURAR Y PROPAGAR TODAS LAS CABECERAS
        HttpHeaders headers = new HttpHeaders();
        // Itera sobre las cabeceras de la petición entrante (de KrakenD)
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            // Reenvía todos los valores de la cabecera
            headers.set(headerName, request.getHeader(headerName));
        });

        // 2. CREAR LA ENTIDAD HTTP CON LAS CABECERAS
        // Crea un HttpEntity que contendrá las cabeceras que se enviarán al backend
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        // 3. DEFINIR LA URL DE DESTINO (Consul)
        String serviceName = "srv-avl-backend"; 
        String url = "http://" + serviceName + "/items";

        // Reconstruir los parámetros de consulta si es necesario (ej. 'path')
        if (path != null && !path.isEmpty()) {
            url += "?path=" + path;
        }

        // 4. HACER LA LLAMADA AL BACKEND USANDO EXCHANGE
        // Usamos exchange() en lugar de getForObject() para enviar cabeceras
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET, // O el método HTTP real (en este caso, GET)
            httpEntity,
            String.class
        );

        // 5. DEVOLVER LA RESPUESTA AL CLIENTE (KRAKEND)
        return response;
    }
}