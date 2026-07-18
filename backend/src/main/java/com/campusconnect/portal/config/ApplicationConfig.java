package com.campusconnect.portal.config;

import com.campusconnect.portal.config.props.AiProperties;
import com.campusconnect.portal.config.props.CacheProperties;
import com.campusconnect.portal.config.props.CorsProperties;
import com.campusconnect.portal.config.props.EmailProperties;
import com.campusconnect.portal.config.props.JwtProperties;
import com.campusconnect.portal.config.props.OAuthProperties;
import com.campusconnect.portal.config.props.SecurityProperties;
import com.campusconnect.portal.config.props.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Registers typed configuration properties and shared infrastructure beans. */
@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class,
        CorsProperties.class,
        EmailProperties.class,
        StorageProperties.class,
        AiProperties.class,
        OAuthProperties.class,
        CacheProperties.class
})
public class ApplicationConfig {

    /** Shared {@link RestClient.Builder} for outbound integrations (Brevo, Supabase, AI). */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
