package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiConsoleController;
import com.example.admissions_management.presentation.form.model.BangQuyDoiTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

public class BangQuyDoiManagementPanel extends JPanel {

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

    private final BangQuyDoiConsoleController controller;
    private final BangQuyDoiTableModel tableModel;
    private final JTable table;

    private final JTextField searchMaQDField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField phuongThucField = new JTextField();
    private final JTextField toHopField = new JTextField();
    private final JTextField monField = new JTextField();
    private final JTextField diemAField = new JTextField();
    private final JTextField diemBField = new JTextField();
    private final JTextField diemCField = new JTextField();
    private final JTextField diemDField = new JTextField();
    private final JTextField maQuyDoiField = new JTextField();
    private final JTextField phanViField = new JTextField();

    public BangQuyDoiManagementPanel(BangQuyDoiConsoleController controller) {
        this.controller = controller;
        this.tableModel = new BangQuyDoiTableModel();
        this.table = new JTable(tableModel);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildEditPanel(), BorderLayout.EAST);

        styleTable();

        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        JLabel searchLabel = new JLabel("Tìm theo mã quy đổi");
        searchLabel.setForeground(MUTED_COLOR);
        searchLabel.setFont(LABEL_FONT);
        panel.add(searchLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        styleTextField(searchMaQDField);
        panel.add(searchMaQDField, c);

        JPanel rightButtons = new JPanel(new GridLayout(1, 4, 10, 0));
        rightButtons.setOpaque(false);
        JButton searchButton = createButton("Tìm", ButtonType.PRIMARY);
        JButton refreshButton = createButton("Làm mới", ButtonType.SECONDARY);
        JButton importButton = createButton("Import Excel", ButtonType.ACCENT);
        JButton deleteAllButton = createButton("Xóa tất cả", ButtonType.DANGER);
        rightButtons.add(searchButton);
        rightButtons.add(refreshButton);
        rightButtons.add(importButton);
        rightButtons.add(deleteAllButton);

        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        panel.add(rightButtons, c);

        searchButton.addActionListener(e -> search());
        refreshButton.addActionListener(e -> refreshTable());
        importButton.addActionListener(e -> importExcel());
        deleteAllButton.addActionListener(e -> deleteAllData());

        return panel;
    }

    private JPanel buildEditPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setPreferredSize(new Dimension(560, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outer.setBackground(BG_COLOR);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        JPanel section = createFieldSection("Thông tin quy đổi", new Object[][] {
                { "ID", idField },
                { "Phương thức", phuongThucField },
                { "Tổ hợp", toHopField },
                { "Môn", monField },
                { "Điểm A", diemAField },
                { "Điểm B", diemBField },
                { "Điểm C", diemCField },
                { "Điểm D", diemDField },
                { "Mã quy đổi", maQuyDoiField },
                { "Phân vị", phanViField }
        });

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        content.add(section, gc);

        gc.gridy = 1;
        gc.weighty = 1.0;
        content.add(new JPanel(), gc);

        JScrollPane sp = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        outer.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE_COLOR);
        btnPanel.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        JButton saveButton = createButton("Lưu", ButtonType.PRIMARY);
        JButton deleteButton = createButton("Xóa", ButtonType.DANGER);
        JButton clearButton = createButton("Xóa form", ButtonType.SECONDARY);
        saveButton.setPreferredSize(new Dimension(140, 42));
        deleteButton.setPreferredSize(new Dimension(140, 42));
        clearButton.setPreferredSize(new Dimension(140, 42));
        btnPanel.add(saveButton);
        btnPanel.add(deleteButton);
        btnPanel.add(clearButton);
        outer.add(btnPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearFields());

        idField.setEditable(false);
        return outer;
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

        for (int i = 0; i < fields.length; i++) {
            String label = (String) fields[i][0];
            JComponent comp = (JComponent) fields[i][1];

            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.25;
            JLabel lbl = new JLabel(label);
            lbl.setFont(LABEL_FONT);
            lbl.setForeground(MUTED_COLOR);
            panel.add(lbl, gc);

            gc.gridx = 1;
            gc.weightx = 0.75;
            if (comp instanceof JTextField textField) {
                styleTextField(textField);
                textField.setPreferredSize(new Dimension(220, 30));
                panel.add(textField, gc);
            } else {
                panel.add(comp, gc);
            }
        }

        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(FIELD_FONT);
        textField.setForeground(TEXT_COLOR);
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }

    private JButton createButton(String text, ButtonType type) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(150, 42));
        button.setMinimumSize(new Dimension(150, 42));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

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
    }

    private void search() {
        String q = searchMaQDField.getText().trim();
        if (q.isEmpty()) {
            refreshTable();
        } else {
            tableModel.setRows(controller.loadByMaQuyDoi(q));
        }
    }

    private void refreshTable() {
        tableModel.setRows(controller.loadAll());
    }

    private void save() {
        try {
            controller.save(
                    phuongThucField.getText(),
                    toHopField.getText(),
                    monField.getText(),
                    diemAField.getText(),
                    diemBField.getText(),
                    diemCField.getText(),
                    diemDField.getText(),
                    maQuyDoiField.getText(),
                    phanViField.getText());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Xóa dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BangQuyDoi row = tableModel.getRowAt(r);
            controller.delete(row.getId());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected(ListSelectionEvent ev) {
        if (ev.getValueIsAdjusting()) {
            return;
        }
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }
        BangQuyDoi row = tableModel.getRowAt(r);
        idField.setText(row.getId() == null ? "" : String.valueOf(row.getId()));
        phuongThucField.setText(row.getPhuongThuc());
        toHopField.setText(row.getToHop());
        monField.setText(row.getMon());
        diemAField.setText(row.getDiemA() == null ? "" : row.getDiemA().toPlainString());
        diemBField.setText(row.getDiemB() == null ? "" : row.getDiemB().toPlainString());
        diemCField.setText(row.getDiemC() == null ? "" : row.getDiemC().toPlainString());
        diemDField.setText(row.getDiemD() == null ? "" : row.getDiemD().toPlainString());
        maQuyDoiField.setText(row.getMaQuyDoi());
        phanViField.setText(row.getPhanVi());
    }

    private void clearFields() {
        searchMaQDField.setText("");
        idField.setText("");
        phuongThucField.setText("");
        toHopField.setText("");
        monField.setText("");
        diemAField.setText("");
        diemBField.setText("");
        diemCField.setText("");
        diemDField.setText("");
        maQuyDoiField.setText("");
        phanViField.setText("");
        table.clearSelection();
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa tất cả dữ liệu quy đổi?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteAll();
            refreshTable();
            clearFields();
        }
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel để import");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File selectedFile = fileChooser.getSelectedFile();
        try {
            int imported = controller.importExcelFile(selectedFile);
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this,
                    "Nhập thành công. Số dòng: " + imported,
                    "Import thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi nhập: " + ex.getMessage(),
                    "Import thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private enum ButtonType {
        PRIMARY,
        SECONDARY,
        ACCENT,
        DANGER
    }
}
