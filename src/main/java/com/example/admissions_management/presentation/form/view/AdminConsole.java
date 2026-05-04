package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.presentation.form.controller.AdminConsoleController;
import com.example.admissions_management.presentation.form.view.combination.CombinationForm;
import com.example.admissions_management.presentation.form.model.AdminConsoleTableModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "app.swing", name = "enabled", havingValue = "true")
public class AdminConsole extends JFrame {

	private final AdminConsoleController controller;
	private final ObjectProvider<CombinationForm> combinationFormProvider;
	private final AdminConsoleTableModel tableModel;

	private final JTextField fullNameField = new JTextField();
	private final JTextField emailField = new JTextField();
	private final JTextField programField = new JTextField();

	public AdminConsole(AdminConsoleController controller, ObjectProvider<CombinationForm> combinationFormProvider) {
		this.controller = controller;
		this.combinationFormProvider = combinationFormProvider;
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
		JButton combinationsButton = new JButton("Combinations");

		saveButton.addActionListener(e -> saveApplicant());
		refreshButton.addActionListener(e -> refreshTable());
		combinationsButton.addActionListener(e -> openCombinationManager());

		actionPanel.add(saveButton);
		actionPanel.add(refreshButton);
		actionPanel.add(combinationsButton);
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

	private void openCombinationManager() {
		CombinationForm form = combinationFormProvider.getObject();
		form.setVisible(true);
		form.toFront();
	}
}
