package com.example.admissions_management.config;

import com.example.admissions_management.presentation.form.view.AdminConsole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.SwingUtilities;

@Configuration
public class SwingStartupConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.swing", name = "enabled", havingValue = "true")
    CommandLineRunner swingAdminConsoleLauncher(AdminConsole adminConsole) {
        return args -> {
            System.setProperty("java.awt.headless", "false");
            SwingUtilities.invokeLater(() -> adminConsole.setVisible(true));
        };
    }
}
