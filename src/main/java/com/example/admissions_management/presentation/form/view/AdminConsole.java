package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.presentation.form.controller.AdminConsoleController;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import org.springframework.stereotype.Component;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

@Component
public class AdminConsole extends JFrame {

	public AdminConsole(AdminConsoleController adminConsoleController,
	                    DiemCongConsoleController diemCongConsoleController,
	                    NguyenVongConsoleController nguyenVongConsoleController) {
		setTitle("Admissions Admin Console");
		setSize(1280, 760);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Applicants", new ApplicantPanel(adminConsoleController));
		tabs.addTab("Phần 7 - Điểm Cộng", new DiemCongPanel(diemCongConsoleController));
		tabs.addTab("Phần 8 - Nguyện Vọng", new NguyenVongPanel(nguyenVongConsoleController));

		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
	}
}
