package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.application.service.ScoreManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.presentation.form.controller.ScoreManagementConsoleController;
import com.example.admissions_management.presentation.form.model.ScoreManagementTableModel;
import com.example.admissions_management.presentation.web.model.ScoreManagementForm;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;

public class ScoreManagementPanel extends JPanel {

    private static final Color BG_COLOR = new Color(245, 247, 244);
    private static final Color SURFACE_COLOR = Color.WHITE;
    private static final Color SOFT_COLOR = new Color(248, 251, 248);
    private static final Color LINE_COLOR = new Color(220, 229, 223);
    private static final Color PRIMARY_COLOR = new Color(12, 122, 99);
    private static final Color ACCENT_COLOR = new Color(231, 150, 45);
    private static final Color DANGER_COLOR = new Color(194, 65, 53);
    private static final Color TEXT_COLOR = new Color(29, 46, 40);
    private static final Color MUTED_COLOR = new Color(100, 118, 110);
    private static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Dimension FIELD_SIZE = new Dimension(320, 32);
    private static final Dimension LABEL_SIZE = new Dimension(136, 24);

    private final ScoreManagementConsoleController controller;
    private final ScoreManagementTableModel tableModel;

    private final JTextField idField = new JTextField();
    private final JTextField cccdField = new JTextField();
    private final JTextField soBaoDanhField = new JTextField();
    private final JTextField phuongThucField = new JTextField("THPT_A00");
    private final JTextField toField = new JTextField();
    private final JTextField liField = new JTextField();
    private final JTextField hoField = new JTextField();
    private final JTextField siField = new JTextField();
    private final JTextField suField = new JTextField();
    private final JTextField diField = new JTextField();
    private final JTextField vaField = new JTextField();
    private final JTextField n1ThiField = new JTextField();
    private final JTextField n1CcField = new JTextField();
    private final JTextField cncnField = new JTextField();
    private final JTextField cnnnField = new JTextField();
    private final JTextField tiField = new JTextField();
    private final JTextField ktplField = new JTextField();
    private final JTextField nl1Field = new JTextField();
    private final JTextField nk1Field = new JTextField();
    private final JTextField nk2Field = new JTextField();

    private final JComboBox<String> filterType = new JComboBox<>(new String[] { "ALL", "THPT", "VSAT", "DGNL" });
    private final JTable table;

    public ScoreManagementPanel(ScoreManagementConsoleController controller) {
        this.controller = controller;
        this.tableModel = new ScoreManagementTableModel();
        this.table = new JTable(tableModel);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        styleTable();

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(createTableScrollPane(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateFromSelectedRow();
            }
        });

        refreshTable();
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = createCardPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton refreshButton = createButton("Làm mới", ButtonType.SECONDARY);
        JButton clearButton = createButton("Xóa biểu mẫu", ButtonType.SECONDARY);
        JButton deleteButton = createButton("Xóa dòng chọn", ButtonType.DANGER);
        JButton importButton = createButton("Import Excel", ButtonType.ACCENT);

        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearForm());
        deleteButton.addActionListener(e -> deleteSelected());
        importButton.addActionListener(e -> importExcel());

        left.add(refreshButton);
        left.add(clearButton);
        left.add(deleteButton);
        left.add(importButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel filterLabel = new JLabel("Lọc theo loại điểm");
        filterLabel.setForeground(MUTED_COLOR);
        filterLabel.setFont(LABEL_FONT);
        styleComboBox(filterType);
        filterType.setPreferredSize(new Dimension(170, 34));
        JButton filterButton = createButton("Áp dụng", ButtonType.PRIMARY);
        filterButton.addActionListener(e -> refreshTable());

        right.add(filterLabel);
        right.add(filterType);
        right.add(filterButton);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setPreferredSize(new Dimension(600, 0));
        outer.setBackground(BG_COLOR);

        idField.setEditable(false);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 10, 0);

        gc.gridy = 0;
        contentPanel.add(createFieldSection("Thông tin chính", new Object[][] {
                { "ID", idField },
                { "CCCD", cccdField },
                { "Số báo danh", soBaoDanhField },
                { "Phương thức", phuongThucField }
        }), gc);

        gc.gridy = 1;
        contentPanel.add(createFieldSection("Khối điểm THPT / V-SAT", new Object[][] {
                { "Toán", toField },
                { "Vật lý", liField },
                { "Hóa học", hoField },
                { "Sinh học", siField },
                { "Lịch sử", suField },
                { "Địa lý", diField },
                { "Ngữ văn", vaField },
                { "N1 thi", n1ThiField },
                { "N1 chứng chỉ", n1CcField }
        }), gc);

        gc.gridy = 2;
        contentPanel.add(createFieldSection("Môn bổ sung", new Object[][] {
                { "CNCN", cncnField },
                { "CNNN", cnnnField },
                { "Tin học", tiField },
                { "KTPL", ktplField }
        }), gc);

        gc.gridy = 3;
        contentPanel.add(createFieldSection("Điểm đánh giá năng lực", new Object[][] {
                { "ĐGNL", nl1Field },
                { "NK1", nk1Field },
                { "NK2", nk2Field }
        }), gc);

        gc.gridy = 4;
        gc.weighty = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        contentPanel.add(filler, gc);

        JScrollPane scrollPane = new JScrollPane(
                contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel actions = createCardPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveButton = createButton("Lưu / Cập nhật", ButtonType.PRIMARY);
        JButton clearButton = createButton("Xóa biểu mẫu", ButtonType.SECONDARY);
        JButton refreshButton = createButton("Làm mới", ButtonType.SECONDARY);
        JButton deleteButton = createButton("Xóa dòng chọn", ButtonType.DANGER);
        saveButton.addActionListener(e -> saveOrUpdate());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> refreshTable());
        deleteButton.addActionListener(e -> deleteSelected());
        actions.add(saveButton);
        actions.add(clearButton);
        actions.add(refreshButton);
        actions.add(deleteButton);

        outer.add(scrollPane, BorderLayout.CENTER);
        outer.add(actions, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel createCardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    private JPanel createFieldSection(String title, Object[][] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE_COLOR, 1),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                SECTION_TITLE_FONT,
                TEXT_COLOR));
        panel.setBackground(SOFT_COLOR);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 10, 8, 10);
        gc.gridy = 0;

        for (Object[] row : fields) {
            JLabel label = new JLabel((String) row[0]);
            label.setFont(LABEL_FONT);
            label.setForeground(MUTED_COLOR);
            label.setPreferredSize(LABEL_SIZE);
            label.setMinimumSize(LABEL_SIZE);
            label.setMaximumSize(LABEL_SIZE);

            gc.gridx = 0;
            gc.weightx = 0.0;
            gc.gridwidth = 1;
            panel.add(label, gc);

            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.gridwidth = GridBagConstraints.REMAINDER;

            JComponent component = (JComponent) row[1];
            if (component instanceof JTextField field) {
                styleTextField(field);
                field.setPreferredSize(FIELD_SIZE);
                field.setMinimumSize(new Dimension(0, FIELD_SIZE.height));
                field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_SIZE.height));
            }
            panel.add(component, gc);
            gc.gridy++;
        }

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FIELD_FONT);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
    }

    private JButton createButton(String text, ButtonType type) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(150, 36));
        button.setMinimumSize(new Dimension(150, 36));

        if (type == ButtonType.PRIMARY) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else if (type == ButtonType.ACCENT) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(TEXT_COLOR);
        } else if (type == ButtonType.DANGER) {
            button.setBackground(DANGER_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(SURFACE_COLOR);
            button.setForeground(TEXT_COLOR);
        }
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        return button;
    }

    private void styleTable() {
        table.setRowHeight(30);
        table.setFont(FIELD_FONT);
        table.getTableHeader().setFont(BUTTON_FONT);
        table.getTableHeader().setBackground(new Color(236, 244, 239));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.setSelectionBackground(new Color(216, 235, 226));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(LINE_COLOR);
        table.setFillsViewportHeight(true);
    }

    private JScrollPane createTableScrollPane() {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void refreshTable() {
        String selectedType = filterType.getSelectedItem() == null ? "ALL" : filterType.getSelectedItem().toString();
        tableModel.setRows(controller.loadByType(selectedType));
    }

    private void populateFromSelectedRow() {
        int selectedRow = table.getSelectedRow();
        XtDiemThiXetTuyenEntity row = tableModel.getRowAt(selectedRow);
        if (row == null) {
            return;
        }

        idField.setText(toText(row.getId()));
        cccdField.setText(toText(row.getCccd()));
        soBaoDanhField.setText(toText(row.getSoBaoDanh()));
        phuongThucField.setText(toText(row.getdPhuongThuc()));
        toField.setText(toText(row.getTo()));
        liField.setText(toText(row.getLi()));
        hoField.setText(toText(row.getHo()));
        siField.setText(toText(row.getSi()));
        suField.setText(toText(row.getSu()));
        diField.setText(toText(row.getDi()));
        vaField.setText(toText(row.getVa()));
        n1ThiField.setText(toText(row.getN1Thi()));
        n1CcField.setText(toText(row.getN1Cc()));
        cncnField.setText(toText(row.getCncn()));
        cnnnField.setText(toText(row.getCnnn()));
        tiField.setText(toText(row.getTi()));
        ktplField.setText(toText(row.getKtpl()));
        nl1Field.setText(toText(row.getNl1()));
        nk1Field.setText(toText(row.getNk1()));
        nk2Field.setText(toText(row.getNk2()));
    }

    private void saveOrUpdate() {
        try {
            ScoreManagementForm form = new ScoreManagementForm();
            if (!idField.getText().trim().isEmpty()) {
                form.setId(Integer.parseInt(idField.getText().trim()));
            }

            form.setCccd(cccdField.getText().trim());
            form.setSoBaoDanh(soBaoDanhField.getText().trim());
            form.setdPhuongThuc(phuongThucField.getText().trim());
            form.setTo(parseDecimal(toField.getText()));
            form.setLi(parseDecimal(liField.getText()));
            form.setHo(parseDecimal(hoField.getText()));
            form.setSi(parseDecimal(siField.getText()));
            form.setSu(parseDecimal(suField.getText()));
            form.setDi(parseDecimal(diField.getText()));
            form.setVa(parseDecimal(vaField.getText()));
            form.setN1Thi(parseDecimal(n1ThiField.getText()));
            form.setN1Cc(parseDecimal(n1CcField.getText()));
            form.setCncn(parseDecimal(cncnField.getText()));
            form.setCnnn(parseDecimal(cnnnField.getText()));
            form.setTi(parseDecimal(tiField.getText()));
            form.setKtpl(parseDecimal(ktplField.getText()));
            form.setNl1(parseDecimal(nl1Field.getText()));
            form.setNk1(parseDecimal(nk1Field.getText()));
            form.setNk2(parseDecimal(nk2Field.getText()));

            if (form.getCccd() == null || form.getCccd().isBlank()) {
                throw new IllegalArgumentException("CCCD là bắt buộc.");
            }

            XtDiemThiXetTuyenEntity saved = controller.save(form);
            JOptionPane.showMessageDialog(this,
                    "Đã lưu bản ghi điểm: ID=" + saved.getId() + ", CCCD=" + saved.getCccd(),
                    "Lưu thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "Lưu thất bại: " + exception.getMessage(),
                    "Lưu thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        XtDiemThiXetTuyenEntity row = tableModel.getRowAt(selectedRow);
        if (row == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một dòng dữ liệu để xóa.",
                    "Chưa chọn dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xóa ID=" + row.getId() + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteById(row.getId());
            clearForm();
            refreshTable();
        }
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx, *.xls)", "xlsx", "xls"));
        fileChooser.setDialogTitle("Chọn file Excel để import");

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        try {
            ScoreManagementService.ImportResult importResult = controller.importExcel(selectedFile);
            JOptionPane.showMessageDialog(this,
                    importResult.message(),
                    "Kết quả import",
                    importResult.message().toLowerCase().contains("thất bại")
                            || importResult.message().toLowerCase().contains("không")
                                    ? JOptionPane.WARNING_MESSAGE
                                    : JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (Exception exception) {
            String msg = exception.getMessage() == null ? exception.toString() : exception.getMessage();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi import: " + msg,
                    "Import thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        idField.setText("");
        cccdField.setText("");
        soBaoDanhField.setText("");
        phuongThucField.setText("THPT_A00");
        toField.setText("");
        liField.setText("");
        hoField.setText("");
        siField.setText("");
        suField.setText("");
        diField.setText("");
        vaField.setText("");
        n1ThiField.setText("");
        n1CcField.setText("");
        cncnField.setText("");
        cnnnField.setText("");
        tiField.setText("");
        ktplField.setText("");
        nl1Field.setText("");
        nk1Field.setText("");
        nk2Field.setText("");
        table.clearSelection();
    }

    private BigDecimal parseDecimal(String value) {
        return controller.parseDecimal(value);
    }

    private String toText(Object value) {
        return value == null ? "" : value.toString();
    }

    private enum ButtonType {
        PRIMARY,
        SECONDARY,
        ACCENT,
        DANGER
    }
}
