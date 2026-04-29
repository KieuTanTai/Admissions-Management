package com.example.admissions_management.presentation.form;

import com.example.admissions_management.config.ApplicationConfig;
import com.example.admissions_management.presentation.form.view.AdminConsole;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;

/**
 * Component để khởi động Swing Form khi Spring Boot application sẵn sàng
 * Điều này cho phép chạy cả Swing GUI lẫn Spring Web MVC server cùng lúc
 *
 * Swing UI chạy trên riêng Event Dispatch Thread (EDT)
 * Spring Web MVC server chạy trên servlet container
 */
@Component
public class ApplicationStartup {

    private static final Logger logger = Logger.getLogger(ApplicationStartup.class.getName());

    private final AdminConsole adminConsole;
    private final ApplicationConfig applicationConfig;

    public ApplicationStartup(AdminConsole adminConsole, ApplicationConfig applicationConfig) {
        this.adminConsole = adminConsole;
        this.applicationConfig = applicationConfig;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("================================");
        logger.info("Spring Boot Application đã khởi động thành công!");
        logger.info("Web Server: http://localhost:8080/admissions");
        logger.info("================================");

        // Nếu Swing được bật, khởi động UI trên EDT
        if (applicationConfig.getSwing().isEnabled()) {
            logger.info("Đang khởi động Swing Admin Console...");

            SwingUtilities.invokeLater(() -> {
                adminConsole.setVisible(true);
                logger.info("✓ Admin Console Swing Form đã được hiển thị");
            });
        } else {
            logger.info("Swing UI bị vô hiệu hóa. Chỉ chạy Web Server.");
            logger.info("Để bật Swing UI, thiết lập: app.swing.enabled=true");
        }
    }
}



