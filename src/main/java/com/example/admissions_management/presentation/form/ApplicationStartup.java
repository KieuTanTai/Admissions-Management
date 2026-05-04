package com.example.admissions_management.presentation.form;

import com.example.admissions_management.config.ApplicationConfig;
import com.example.admissions_management.presentation.form.view.AdminConsole;
import com.example.admissions_management.presentation.form.view.combination.CombinationForm;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<AdminConsole> adminConsoleProvider;
    private final ApplicationConfig applicationConfig;
    private final ObjectProvider<CombinationForm> combinationFormProvider;

//    public ApplicationStartup(ObjectProvider<AdminConsole> adminConsoleProvider, ApplicationConfig applicationConfig) {
//        this.adminConsoleProvider = adminConsoleProvider;
//        this.applicationConfig = applicationConfig;
//    }

    public ApplicationStartup(ObjectProvider<AdminConsole> adminConsoleProvider, ApplicationConfig applicationConfig, ObjectProvider<CombinationForm> combinationFormProvider) {
        this.adminConsoleProvider = adminConsoleProvider;
        this.applicationConfig = applicationConfig;
        this.combinationFormProvider = combinationFormProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("================================");
        logger.info("Spring Boot Application đã khởi động thành công!");
        logger.info("Web Server: http://localhost:8080/admissions");
        logger.info("================================");

        if (applicationConfig.getSwing().isEnabled()) {
            CombinationForm combinationForm = combinationFormProvider.getIfAvailable();
            if (combinationForm == null) {
                logger.warning("Swing UI được bật nhưng không tìm thấy bean CombinationForm (có thể chạy ở chế độ headless).");
                return;
            }
            logger.info("Đang khởi động Swing Combination Form...");
            SwingUtilities.invokeLater(() -> {
                combinationForm.setVisible(true);
                logger.info("✓ Combination Form đã được hiển thị");
            });

        } else {
            logger.info("Swing UI bị vô hiệu hóa. Chỉ chạy Web Server.");
            logger.info("Để bật Swing UI, thiết lập: app.swing.enabled=true");
        }
//        // Nếu Swing được bật, khởi động UI trên EDT
//        if (applicationConfig.getSwing().isEnabled()) {
//            AdminConsole adminConsole = adminConsoleProvider.getIfAvailable();
//            if (adminConsole == null) {
//                logger.warning("Swing UI được bật nhưng không tìm thấy bean AdminConsole (có thể chạy ở chế độ headless).");
//                return;
//            }
//
//            logger.info("Đang khởi động Swing Admin Console...");
//
//            SwingUtilities.invokeLater(() -> {
//                adminConsole.setVisible(true);
//                logger.info("✓ Admin Console Swing Form đã được hiển thị");
//            });
//        } else {
//            logger.info("Swing UI bị vô hiệu hóa. Chỉ chạy Web Server.");
//            logger.info("Để bật Swing UI, thiết lập: app.swing.enabled=true");
//        }
    }
}



