package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.presentation.form.controller.AdminConsoleController;
import com.example.admissions_management.presentation.form.model.AdminConsoleTableModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

@Component
public class AdminConsole extends JFrame {

	private final AdminConsoleController controller;
	private final ObjectProvider<ScoreManagementConsole> scoreManagementConsoleProvider;
	private final AdminConsoleTableModel tableModel;

	private final JTextField fullNameField = new JTextField();
	private final JTextField emailField = new JTextField();
	private final JTextField programField = new JTextField();

	public AdminConsole(AdminConsoleController controller,
						ObjectProvider<ScoreManagementConsole> scoreManagementConsoleProvider) {
		this.controller = controller;
		this.scoreManagementConsoleProvider = scoreManagementConsoleProvider;
		this.tableModel = new AdminConsoleTableModel();

		setTitle("Admissions Admin Console");
		setSize(860, 520);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

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

		JPanel actionPanel = new JPanel(new GridLayout(1, 3, 8, 8));
		JButton saveButton = new JButton("Save");
		JButton refreshButton = new JButton("Refresh");
		JButton scoreManagementButton = new JButton("Open Scores Form");

		saveButton.addActionListener(e -> saveApplicant());
		refreshButton.addActionListener(e -> refreshTable());
		scoreManagementButton.addActionListener(e -> openScoreManagementForm());

		actionPanel.add(saveButton);
		actionPanel.add(refreshButton);
		actionPanel.add(scoreManagementButton);
		panel.add(actionPanel);

		return panel;
	}

	private void saveApplicant() {
		try {
			controller.registerApplicant(
					fullNameField.getText().trim(),
					emailField.getText().trim(),
					programField.getText().trim()
			);

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

	private void openScoreManagementForm() {
		try {
			ScoreManagementConsole scoreConsole = scoreManagementConsoleProvider.getObject();
			scoreConsole.setVisible(true);
			setVisible(false);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"Khong mo duoc form quan ly diem: " + ex.getMessage(),
					"Open Form Failed",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
