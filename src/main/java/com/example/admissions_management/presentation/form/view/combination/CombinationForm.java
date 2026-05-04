package com.example.admissions_management.presentation.form.view.combination;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.presentation.form.controller.CombinationFormController;
import com.example.admissions_management.presentation.form.model.CombinationTableModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "app.swing", name = "enabled", havingValue = "true")
@Lazy
public class CombinationForm extends JFrame {

	private final CombinationFormController controller;
	private final CombinationTableModel tableModel = new CombinationTableModel();
	private final JTable table = new JTable(tableModel);

	public CombinationForm(CombinationFormController controller) {
		this.controller = controller;

		setTitle("Combination Manager (Mock)");
		setSize(1400, 650);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout(12, 12));
		add(buildActionPanel(), BorderLayout.NORTH);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		add(new JScrollPane(table), BorderLayout.CENTER);

		refreshAll();
	}

	private JPanel buildActionPanel() {
		JPanel panel = new JPanel();

		JButton insertButton = new JButton("Insert");
		JButton updateButton = new JButton("Update");
		JButton deleteButton = new JButton("Delete");
		JButton findButton = new JButton("Find");
		JButton refreshButton = new JButton("Refresh");

		insertButton.addActionListener(e -> onInsert());
		updateButton.addActionListener(e -> onUpdateSelected());
		deleteButton.addActionListener(e -> onDeleteSelected());
		findButton.addActionListener(e -> onFind());
		refreshButton.addActionListener(e -> refreshAll());

		panel.add(insertButton);
		panel.add(updateButton);
		panel.add(deleteButton);
		panel.add(findButton);
		panel.add(refreshButton);
		return panel;
	}

	private void refreshAll() {
		tableModel.setRows(controller.loadAll());
	}

	private void onFind() {
		String query = JOptionPane.showInputDialog(this, "Find by id / manganh / matohop / tb_keys:", "Find", JOptionPane.QUESTION_MESSAGE);
		if (query == null) {
			return;
		}
		tableModel.setRows(controller.find(query));
	}

	private void onInsert() {
		try {
			CombinationResponse input = showEditorDialog("Insert Combination", null);
			if (input == null) {
				return;
			}
			controller.insert(input);
			refreshAll();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Insert Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onUpdateSelected() {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a row to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = table.convertRowIndexToModel(viewRow);
		CombinationResponse selected = tableModel.getRowAt(modelRow);
		if (selected == null || selected.getId() == null) {
			JOptionPane.showMessageDialog(this, "Invalid selected row.", "Update Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			CombinationResponse input = showEditorDialog("Update Combination (id=" + selected.getId() + ")", selected);
			if (input == null) {
				return;
			}
			controller.update(selected.getId(), input);
			refreshAll();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onDeleteSelected() {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = table.convertRowIndexToModel(viewRow);
		CombinationResponse selected = tableModel.getRowAt(modelRow);
		if (selected == null || selected.getId() == null) {
			JOptionPane.showMessageDialog(this, "Invalid selected row.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Delete combination id=" + selected.getId() + " ?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
		);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			controller.delete(selected.getId());
			refreshAll();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private CombinationResponse showEditorDialog(String title, CombinationResponse initial) {
		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 8, 4, 8);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JTextField maNganhField = new JTextField();
		JTextField maToHopField = new JTextField();
		JTextField thMon1Field = new JTextField();
		JTextField hsMon1Field = new JTextField();
		JTextField thMon2Field = new JTextField();
		JTextField hsMon2Field = new JTextField();
		JTextField thMon3Field = new JTextField();
		JTextField hsMon3Field = new JTextField();
		JTextField tbKeysField = new JTextField();
		JTextField doLechField = new JTextField();

		JCheckBox n1Box = new JCheckBox();
		JCheckBox toBox = new JCheckBox();
		JCheckBox liBox = new JCheckBox();
		JCheckBox hoBox = new JCheckBox();
		JCheckBox siBox = new JCheckBox();
		JCheckBox vaBox = new JCheckBox();
		JCheckBox suBox = new JCheckBox();
		JCheckBox diBox = new JCheckBox();
		JCheckBox tiBox = new JCheckBox();
		JCheckBox khacBox = new JCheckBox();
		JCheckBox ktplBox = new JCheckBox();

		if (initial != null) {
			maNganhField.setText(nullToEmpty(initial.getMaNganh()));
			maToHopField.setText(nullToEmpty(initial.getMaToHop()));
			thMon1Field.setText(nullToEmpty(initial.getThMon1()));
			hsMon1Field.setText(initial.getHsMon1() == null ? "" : String.valueOf(initial.getHsMon1()));
			thMon2Field.setText(nullToEmpty(initial.getThMon2()));
			hsMon2Field.setText(initial.getHsMon2() == null ? "" : String.valueOf(initial.getHsMon2()));
			thMon3Field.setText(nullToEmpty(initial.getThMon3()));
			hsMon3Field.setText(initial.getHsMon3() == null ? "" : String.valueOf(initial.getHsMon3()));
			tbKeysField.setText(nullToEmpty(initial.getTbKeys()));
			doLechField.setText(initial.getDoLech() == null ? "" : initial.getDoLech().toPlainString());

			n1Box.setSelected(Boolean.TRUE.equals(initial.getN1()));
			toBox.setSelected(Boolean.TRUE.equals(initial.getTo()));
			liBox.setSelected(Boolean.TRUE.equals(initial.getLi()));
			hoBox.setSelected(Boolean.TRUE.equals(initial.getHo()));
			siBox.setSelected(Boolean.TRUE.equals(initial.getSi()));
			vaBox.setSelected(Boolean.TRUE.equals(initial.getVa()));
			suBox.setSelected(Boolean.TRUE.equals(initial.getSu()));
			diBox.setSelected(Boolean.TRUE.equals(initial.getDi()));
			tiBox.setSelected(Boolean.TRUE.equals(initial.getTi()));
			khacBox.setSelected(Boolean.TRUE.equals(initial.getKhac()));
			ktplBox.setSelected(Boolean.TRUE.equals(initial.getKtpl()));
		}

		int row = 0;
		row = addRow(form, gbc, row, "manganh", maNganhField);
		row = addRow(form, gbc, row, "matohop", maToHopField);
		row = addRow(form, gbc, row, "th_mon1", thMon1Field);
		row = addRow(form, gbc, row, "hsmon1", hsMon1Field);
		row = addRow(form, gbc, row, "th_mon2", thMon2Field);
		row = addRow(form, gbc, row, "hsmon2", hsMon2Field);
		row = addRow(form, gbc, row, "th_mon3", thMon3Field);
		row = addRow(form, gbc, row, "hsmon3", hsMon3Field);
		row = addRow(form, gbc, row, "tb_keys", tbKeysField);
		row = addRow(form, gbc, row, "dolech", doLechField);
		row = addRow(form, gbc, row, "N1", n1Box);
		row = addRow(form, gbc, row, "TO", toBox);
		row = addRow(form, gbc, row, "LI", liBox);
		row = addRow(form, gbc, row, "HO", hoBox);
		row = addRow(form, gbc, row, "SI", siBox);
		row = addRow(form, gbc, row, "VA", vaBox);
		row = addRow(form, gbc, row, "SU", suBox);
		row = addRow(form, gbc, row, "DI", diBox);
		row = addRow(form, gbc, row, "TI", tiBox);
		row = addRow(form, gbc, row, "KHAC", khacBox);
		row = addRow(form, gbc, row, "KTPL", ktplBox);

		JScrollPane scrollPane = new JScrollPane(form);
		scrollPane.setPreferredSize(new Dimension(520, 520));

		int result = JOptionPane.showConfirmDialog(this, scrollPane, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}

		CombinationResponse out = new CombinationResponse();
		out.setMaNganh(maNganhField.getText().trim());
		out.setMaToHop(maToHopField.getText().trim());
		out.setThMon1(blankToNull(thMon1Field.getText()));
		out.setHsMon1(parseByteOrNull(hsMon1Field.getText()));
		out.setThMon2(blankToNull(thMon2Field.getText()));
		out.setHsMon2(parseByteOrNull(hsMon2Field.getText()));
		out.setThMon3(blankToNull(thMon3Field.getText()));
		out.setHsMon3(parseByteOrNull(hsMon3Field.getText()));
		out.setTbKeys(blankToNull(tbKeysField.getText()));
		out.setDoLech(parseBigDecimalOrNull(doLechField.getText()));

		out.setN1(n1Box.isSelected());
		out.setTo(toBox.isSelected());
		out.setLi(liBox.isSelected());
		out.setHo(hoBox.isSelected());
		out.setSi(siBox.isSelected());
		out.setVa(vaBox.isSelected());
		out.setSu(suBox.isSelected());
		out.setDi(diBox.isSelected());
		out.setTi(tiBox.isSelected());
		out.setKhac(khacBox.isSelected());
		out.setKtpl(ktplBox.isSelected());

		// keep id for update semantics (service will override to selected id anyway)
		if (initial != null) {
			out.setId(initial.getId());
		}
		return out;
	}

	private static int addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component input) {
		gbc.gridy = row;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		form.add(new JLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		form.add(input, gbc);
		return row + 1;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static Byte parseByteOrNull(String value) {
		String v = blankToNull(value);
		if (v == null) {
			return null;
		}
		try {
			return Byte.valueOf(v);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid byte value: " + value);
		}
	}

	private static BigDecimal parseBigDecimalOrNull(String value) {
		String v = blankToNull(value);
		if (v == null) {
			return null;
		}
		try {
			return new BigDecimal(v);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid decimal value: " + value);
		}
	}
}
