package com.example.admissions_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AdmissionsManagementApplication {

	public static void main(String[] args) {
		// SpringApplication.run(AdmissionsManagementApplication.class, args);
		new SpringApplicationBuilder(AdmissionsManagementApplication.class).headless(false).run(args);
	}

}
