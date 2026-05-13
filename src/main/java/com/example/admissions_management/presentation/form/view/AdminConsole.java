package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.presentation.form.controller.AdminConsoleController;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.view.combination.CombinationForm;
import com.example.admissions_management.presentation.form.view.DiemCongPanel;
import com.example.admissions_management.presentation.form.view.NguyenVongPanel;
import com.example.admissions_management.presentation.form.model.AdminConsoleTableModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

@Component
@ConditionalOnProperty(prefix = "app.swing.admin-console", name = "enabled", havingValue = "true")
@Lazy
public class AdminConsole extends JFrame {

	private final AdminConsoleController controller;
	private final ObjectProvider<CombinationForm> combinationFormProvider;
	private final AdminConsoleTableModel tableModel;
	private final JTextField fullNameField = new JTextField();
	private final JTextField emailField = new JTextField();
	private final JTextField programField = new JTextField();
	private final DiemCongConsoleController diemCongConsoleController;
	private final NguyenVongConsoleController nguyenVongConsoleController;

	public AdminConsole(AdminConsoleController controller, ObjectProvider<CombinationForm> combinationFormProvider,
			DiemCongConsoleController diemCongConsoleController,
			NguyenVongConsoleController nguyenVongConsoleController) {
		this.controller = controller;
		this.combinationFormProvider = combinationFormProvider;
		this.diemCongConsoleController = diemCongConsoleController;
		this.nguyenVongConsoleController = nguyenVongConsoleController;
		this.tableModel = new AdminConsoleTableModel();

		setTitle("Admissions Admin Console");
		setSize(1280, 980);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(false); // Not visible by default

		setLayout(new BorderLayout(12, 12));
		add(buildFormPanel(), BorderLayout.NORTH);
		add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

		refreshTable();
	}

	private JPanel buildFormPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 4, 8, 8));
		panel.add(new JLabel("Full Name"));
		panel.add(new JLabel("Email"));
		panel.add(new JLabel("Program"));
		panel.add(new JLabel("Actions"));

		panel.add(fullNameField);
		panel.add(emailField);
		panel.add(programField);

		JPanel actionPanel = new JPanel(new GridLayout(1, 5, 8, 8));
		JButton saveButton = new JButton("Save");
		JButton refreshButton = new JButton("Refresh");
		JButton combinationsButton = new JButton("Combinations");
		JButton diemCongButton = new JButton("Điểm Cộng");
		JButton nguyenVongButton = new JButton("Nguyện Vọng");

		saveButton.addActionListener(_ -> saveApplicant());
		refreshButton.addActionListener(_ -> refreshTable());
		combinationsButton.addActionListener(_ -> openCombinationManager());
		diemCongButton.addActionListener(_ -> openDiemCongForm());
		nguyenVongButton.addActionListener(_ -> openNguyenVongForm());

		actionPanel.add(saveButton);
		actionPanel.add(refreshButton);
		actionPanel.add(combinationsButton);
		actionPanel.add(diemCongButton);
		actionPanel.add(nguyenVongButton);
		panel.add(actionPanel);

		return panel;
	}

	private void saveApplicant() {
		try {
			controller.registerApplicant(
					fullNameField.getText().trim(),
					emailField.getText().trim(),
					programField.getText().trim());

			fullNameField.setText("");
			emailField.setText("");
			programField.setText("");
			refreshTable();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void refreshTable() {
		tableModel.setRows(controller.loadApplicants());
	}

	private void openCombinationManager() {
		CombinationForm form = combinationFormProvider.getObject();
		form.setVisible(true);
		this.setEnabled(false);
		form.setParentFrame(this);
		form.setTrackingEnableForParentFrame();
		form.toFront();

	}

	private void openDiemCongForm() {
		DiemCongPanel diemCongPanel = new DiemCongPanel(diemCongConsoleController);
		JFrame diemCongFrame = new JFrame("Phần 7 - Điểm Cộng");
		diemCongFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		diemCongFrame.add(diemCongPanel);
		diemCongFrame.setSize(1280, 760);
		diemCongFrame.setLocationRelativeTo(this);
		diemCongFrame.setVisible(true);
	}

	private void openNguyenVongForm() {
		NguyenVongPanel nguyenVongPanel = new NguyenVongPanel(nguyenVongConsoleController);
		JFrame nguyenVongFrame = new JFrame("Phần 8 - Nguyện Vọng");
		nguyenVongFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		nguyenVongFrame.add(nguyenVongPanel);
		nguyenVongFrame.setSize(1280, 760);
		nguyenVongFrame.setLocationRelativeTo(this);
		nguyenVongFrame.setVisible(true);
	}
}
