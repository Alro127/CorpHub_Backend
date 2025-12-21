package com.example.ticket_helpdesk_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "company.location")
@Getter
@Setter
public class CompanyLocationProperties {

    private double lat;
    private double lng;
    private double radiusMeter;
}

