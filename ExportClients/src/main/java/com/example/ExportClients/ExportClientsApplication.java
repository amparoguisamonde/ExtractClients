package com.example.ExportClients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExportClientsApplication {

	public static void main(String[] args) {
		  for(String arg:args) {
	            System.out.println(arg);
	        }
		SpringApplication.run(ExportClientsApplication.class, args);
		

	}
}
