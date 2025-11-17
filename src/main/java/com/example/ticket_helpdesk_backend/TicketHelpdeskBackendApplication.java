package com.example.ticket_helpdesk_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketHelpdeskBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketHelpdeskBackendApplication.class, args);
	}

}
