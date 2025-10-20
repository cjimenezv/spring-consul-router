package com.simon.gateway;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SpringConsulRouterApplication {

	public static void main(String[] args) {
		System.out.println("version 20-Oct-2025");
		SpringApplication.run(SpringConsulRouterApplication.class, args);
	}
	
	@Bean
    @LoadBalanced  // Esto hace que RestTemplate use Spring Cloud LoadBalancer
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
