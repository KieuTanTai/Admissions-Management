package com.example.admissions_management.presentation.form.view.combination;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.presentation.form.controller.CombinationFormController;
import com.example.admissions_management.presentation.form.model.CombinationTableModel;
import org.jspecify.annotations.NonNull;
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
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.swing", name = "enabled", havingValue = "true")
@Lazy
public class CombinationForm extends JFrame {

	private final CombinationFormController controller;
	private final CombinationTableModel tableModel = new CombinationTableModel();
	private final JTable table = new JTable(tableModel);
	private final JTextField searchField = new JTextField(20);
	private JFrame motherFrame = null;
	private int currentPage = 0;
	private int totalPages = 0;
	private String currentSearchQuery = "";
	private JLabel pageInfoLabel;
	private JButton prevButton;
	private JButton nextButton;


	public CombinationForm(CombinationFormController controller) {
		this.controller = controller;

		setTitle("Xét tuyển ngành - tổ hợp");
		setSize(1400, 650);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout(0, 0));

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
		topPanel.add(new JSeparator(), BorderLayout.SOUTH);
		add(topPanel, BorderLayout.NORTH);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		add(buildFooterPanel(), BorderLayout.SOUTH);

		refreshAll();
	}

	private JPanel buildFooterPanel() {
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
		footerPanel.add(new JSeparator());

		prevButton = new JButton("<- Trước");
		prevButton.setFocusPainted(false);
		prevButton.addActionListener(_ -> previousPage());

		pageInfoLabel = new JLabel("Trang 1 / 1");
		pageInfoLabel.setPreferredSize(new Dimension(200, 20));

		nextButton = new JButton("Tiếp ->");
		nextButton.setFocusPainted(false);
		nextButton.addActionListener(_ -> nextPage());

		footerPanel.add(prevButton);
		footerPanel.add(pageInfoLabel);
		footerPanel.add(nextButton);

		return footerPanel;
	}

	private void previousPage() {
		if (currentPage > 0) {
			currentPage--;
			loadPage();
		}
	}

	private void nextPage() {
		if (currentPage < totalPages - 1) {
			currentPage++;
			loadPage();
		}
	}

	private void loadPage() {
		if (currentSearchQuery.isEmpty()) {
			loadPageData(controller.loadAllPaged(currentPage));
		} else {
			loadPageData(controller.findPaged(currentSearchQuery, currentPage));
		}
	}

	private void loadPageData(Map<String, Object> pageData) {
		java.util.List<CombinationResponse> content = (List<CombinationResponse>) pageData.get("content");
		currentPage = (Integer) pageData.get("page");
		totalPages = (Integer) pageData.get("totalPages");

		tableModel.setRows(content);
		pageInfoLabel.setText(String.format("Trang %d / %d (Tổng: %d)",
			currentPage + 1, totalPages, (Long) pageData.get("totalElements")));

		prevButton.setEnabled(currentPage > 0);
		nextButton.setEnabled(currentPage < totalPages - 1);
	}

	public void setTrackingEnableForMotherFrame() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				if (motherFrame != null) {
					motherFrame.setEnabled(true);
					motherFrame.toFront();
				}
				super.windowClosed(event);
			}
		});
	}
	public void setMotherFrame(JFrame motherFrame) {
		this.motherFrame = motherFrame;
	}

	private JPanel buildHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 8));

		// Left panel: Search controls only
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

		JButton searchButton = new JButton("Tìm kiếm");
		searchButton.setFocusPainted(false);
		searchButton.addActionListener(_ -> onSearch());

		leftPanel.add(new JLabel("Tìm kiếm bằng mã ngành:"));
		leftPanel.add(searchField);
		leftPanel.add(searchButton);

		// Right panel: Action buttons (Insert, Update, Refresh, Back)
		JPanel rightPanel = getJPanel();


		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	private @NonNull JPanel getJPanel() {
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));

		JButton insertButton = new JButton("Thêm");
		insertButton.setFocusPainted(false);

		JButton updateButton = new JButton("Sửa");
		updateButton.setFocusPainted(false);

		JButton refreshButton = new JButton("Làm mới");
		refreshButton.setFocusPainted(false);

		insertButton.addActionListener(_ -> onInsert());
		updateButton.addActionListener(_ -> onUpdateSelected());
		refreshButton.addActionListener(_ -> refreshAll());

		rightPanel.add(insertButton);
		rightPanel.add(updateButton);
		rightPanel.add(refreshButton);
		return rightPanel;
	}

	private void refreshAll() {
		searchField.setText("");
		currentPage = 0;
		currentSearchQuery = "";
		loadPageData(controller.loadAllPaged(0));
	}

	private void onSearch() {
		String query = searchField.getText().trim();
		currentSearchQuery = query;
		currentPage = 0;
		if (query.isEmpty()) {
			loadPageData(controller.loadAllPaged(0));
		} else {
			loadPageData(controller.findPaged(query, 0));
		}
	}

	private void onInsert() {
		try {
			CombinationResponse input = showEditorDialog("Thêm ngành - tổ hợp", null);
			if (input == null) {
				return;
			}
			controller.insert(input);
			refreshAll();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Thêm thất bại!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onUpdateSelected() {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn ngành - tổ hợp muốn sửa.", "Không có ngành - tổ hợp được chọn", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = table.convertRowIndexToModel(viewRow);
		CombinationResponse selected = tableModel.getRowAt(modelRow);
		if (selected == null || selected.getId() == null) {
			JOptionPane.showMessageDialog(this, "Số dòng lựa chọn không hợp lệ.", "Cập nhật thất bại", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			CombinationResponse input = showEditorDialog("Cập nhật ngành - tổ hợp (id=" + selected.getId() + ")", selected);
			if (input == null) {
				return;
			}
			controller.update(selected.getId(), input);
			refreshAll();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
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
		addRow(form, gbc, row, "KTPL", ktplBox);

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
