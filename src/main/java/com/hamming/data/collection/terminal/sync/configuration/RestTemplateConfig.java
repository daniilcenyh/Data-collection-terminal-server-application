package com.hamming.data.collection.terminal.sync.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate externalApiRestTemplate(
            @Value("${external.api.username}") String username,
            @Value("${external.api.password}") String password
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                Collections.singletonList(new BasicAuthInterceptor(username, password))
        );
        return restTemplate;
    }

    /**
     * Interceptor для добавления Basic Auth заголовка ко всем запросам
     */
    private static class BasicAuthInterceptor implements ClientHttpRequestInterceptor {

        private final String authHeader;

        public BasicAuthInterceptor(String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            this.authHeader = "Basic " + new String(encodedAuth);
        }

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().set("Authorization", authHeader);
            return execution.execute(request, body);
        }
    }
}
