package com.project.tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TicketsApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(TicketsApplication.class, args);
	}
}

//swagger link
//http://localhost:8081/swagger-ui/index.html#/
