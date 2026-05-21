package com.example.admissions_management.presentation.form;

import com.example.admissions_management.config.ApplicationConfig;
import com.example.admissions_management.presentation.form.view.AdminConsole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;

@Component
public class ApplicationStartup {

    private static final Logger logger = Logger.getLogger(ApplicationStartup.class.getName());

    private final ObjectProvider<AdminConsole> adminConsoleProvider;
    private final ApplicationConfig applicationConfig;
    private final Environment environment;

    public ApplicationStartup(ObjectProvider<AdminConsole> adminConsoleProvider,
            ApplicationConfig applicationConfig,
            Environment environment) {
        this.adminConsoleProvider = adminConsoleProvider;
        this.applicationConfig = applicationConfig;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("================================");
        logger.info("Spring Boot application da khoi dong thanh cong!");
        String port = environment.getProperty("local.server.port");
        if (port == null || port.isBlank()) {
            port = environment.getProperty("server.port", "8081");
        }
        logger.info("Web Server: http://localhost:" + port + "/admissions");
        logger.info("================================");

        if (!applicationConfig.getSwing().isEnabled()) {
            logger.info("Swing UI bi vo hieu hoa. Chi chay Web Server.");
            logger.info("De bat Swing UI, thiet lap: app.swing.enabled=true");
            return;
        }

        AdminConsole adminConsole = adminConsoleProvider.getIfAvailable();
        if (adminConsole == null) {
            logger.warning("Swing UI duoc bat nhung khong tim thay bean AdminConsole.");
            return;
        }

        logger.info("Dang khoi dong Swing admin console...");
        SwingUtilities.invokeLater(() -> {
            adminConsole.setVisible(true);
            adminConsole.toFront();
            logger.info("Admin console da duoc hien thi.");
        });
    }
}
