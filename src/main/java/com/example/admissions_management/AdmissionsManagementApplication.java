package com.example.admissions_management;

import javax.swing.SwingUtilities;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext; // Nhớ sửa đúng tên package của bạn

import com.example.admissions_management.presentation.form.view.AdminConsole;

@SpringBootApplication
public class AdmissionsManagementApplication {
    public static void main(String[] args) {
        // 1. Khởi chạy Spring và lưu vào biến context
        ConfigurableApplicationContext context = new SpringApplicationBuilder(AdmissionsManagementApplication.class)
                .headless(false)
                .run(args);

        // 2. Lệnh quan trọng để mở giao diện
        SwingUtilities.invokeLater(() -> {
            // Lấy giao diện từ Spring ra (không dùng lệnh new)
            AdminConsole frame = context.getBean(AdminConsole.class); 
            System.out.println(frame);
            // Ép kích thước to ra để không bị co rúm
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true); // Hiện hình!
        });
    }
}