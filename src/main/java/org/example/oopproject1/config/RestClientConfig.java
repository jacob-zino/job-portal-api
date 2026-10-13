package org.example.oopproject1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for setting up HTTP clients used by the console application.
 * Provides a RestTemplate bean for making REST calls to the Job Portal API.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates and configures a RestTemplate bean.
     *
     * @return a RestTemplate instance for performing HTTP requests
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}