package com.example.admissions_management;

import com.example.admissions_management.presentation.form.view.AdminConsole;
import org.hibernate.query.criteria.JpaWindowFrame;
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
