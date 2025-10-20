package com.simon.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador Proxy Genérico.
 * Este controlador captura cualquier petición HTTP (GET, POST, PUT, DELETE, etc.)
 * y la redirige a un microservicio registrado en Consul, cuyo nombre se toma de la URL.
 * * Estructura de la URL de entrada: /{nombreServicio}/{rutaInterna}?parametros...
 * Ejemplo: /srv-usuarios/api/v1/users?id=1 -> Enruta a http://srv-usuarios/api/v1/users?id=1
 */
@RestController
public class GenericProxyController {

    @Autowired
    private RestTemplate restTemplate;

    // Esta anotación captura CUALQUIER método HTTP y CUALQUIER ruta
    // El primer segmento de la ruta es capturado como 'serviceName'
    // El resto de la ruta es capturado por la doble estrella (/**)
    @RequestMapping(value = "/{serviceName}/**", produces = "*/*")
    public ResponseEntity<String> proxyRequest(
            // El cuerpo de la petición (necesario para POST, PUT, etc.)
            @RequestBody(required = false) String requestBody,
            // El nombre del servicio de destino (ej. 'srv-avl-backend')
            @PathVariable String serviceName,
            // Objeto de la petición HTTP actual para método, cabeceras y URI
            HttpServletRequest request) {

        // 1. OBTENER EL MÉTODO HTTP
        // Convertir el método de String a HttpMethod (ej: "GET", "POST")
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        // 2. CONSTRUIR LA URI DE DESTINO (Ruta y Query Params)
        // Obtiene la parte de la URI que debe ir al servicio (todo después de /serviceName)
        String restOfThePath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
        );
        
        // La ruta interna es la URL completa sin el prefijo del servicio
        // "/{serviceName}/" + pathInterna -> pathInterna
        String pathInternal = restOfThePath.substring(serviceName.length() + 1);

        // Añadir los parámetros de consulta originales (ej. ?path=...)
        String queryString = request.getQueryString();
        if (queryString != null) {
            pathInternal += "?" + queryString;
        }

        // 3. DEFINIR LA URL DE DESTINO (Usa el nombre del servicio de Consul)
        // Spring Cloud LoadBalancer intercepta "http://" + serviceName y lo resuelve.
        String targetUrl = "http://" + serviceName + pathInternal;
        
        // 4. PROPAGAR TODAS LAS CABECERAS
        HttpHeaders headers = new HttpHeaders();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            // Reenvía todos los valores de la cabecera
            headers.set(headerName, request.getHeader(headerName));
        });

        // 5. CREAR LA ENTIDAD HTTP (Cuerpo + Cabeceras)
        // El cuerpo solo será no nulo para métodos como POST, PUT
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 6. HACER LA LLAMADA AL BACKEND USANDO EXCHANGE
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    method, // Usar el método HTTP original
                    httpEntity,
                    String.class // Se espera una respuesta String (JSON/XML)
            );
            
            // 7. DEVOLVER LA RESPUESTA AL CLIENTE
            return response;
            
        } catch (Exception e) {
            // Manejo básico de errores de conexión o timeout
            System.err.println("Error al enrutar la petición a " + targetUrl + ": " + e.getMessage());
            // Se puede mejorar con un HttpStatus más específico (503 Service Unavailable)
            return ResponseEntity.internalServerError().body("Error interno del router: " + e.getMessage());
        }
    }
}
