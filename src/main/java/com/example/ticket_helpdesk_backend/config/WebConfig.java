package com.example.ticket_helpdesk_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapping /images/** -> thư mục public/images/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:public/images/");
    }
}
