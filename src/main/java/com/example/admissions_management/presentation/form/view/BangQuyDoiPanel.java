package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiConsoleController;
import com.example.admissions_management.presentation.form.model.BangQuyDoiTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

public class BangQuyDoiPanel extends JPanel {

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

    public BangQuyDoiPanel(BangQuyDoiConsoleController controller) {
        this.controller = controller;
        this.tableModel = new BangQuyDoiTableModel();
        this.table = new JTable(tableModel);

        setLayout(new BorderLayout(10,10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildEditPanel(), BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; panel.add(new JLabel("Tìm theo Mã Quy Đổi"), c);
        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; panel.add(searchMaQDField, c);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton importButton = new JButton("Import Excel");
        JButton deleteAllButton = new JButton("Delete All");
        rightButtons.add(searchButton); rightButtons.add(refreshButton); rightButtons.add(importButton); rightButtons.add(deleteAllButton);

        c.gridx = 2; c.gridy = 0; c.fill = GridBagConstraints.NONE; c.weightx = 0.0; panel.add(rightButtons, c);

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

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, 0);

        JPanel section = createFieldSection("Thông Tin Quy Đổi", new Object[][]{
                {"ID", idField},
                {"Phương Thức", phuongThucField},
                {"Tổ Hợp", toHopField},
                {"Môn", monField},
                {"Điểm A", diemAField},
                {"Điểm B", diemBField},
                {"Điểm C", diemCField},
                {"Điểm D", diemDField},
                {"Mã Quy Đổi", maQuyDoiField},
                {"Phân Vị", phanViField}
        });

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0; content.add(section, gc);

            gc.gridy = 1; gc.weighty = 1.0; content.add(new JPanel(), gc);

        JScrollPane sp = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        outer.add(sp, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            btnPanel.setBackground(new Color(240, 240, 240));
            JButton saveButton = new JButton("Save");
            JButton deleteButton = new JButton("Delete");
            JButton clearButton = new JButton("Clear");
            saveButton.setPreferredSize(new Dimension(80, 32));
            deleteButton.setPreferredSize(new Dimension(80, 32));
            clearButton.setPreferredSize(new Dimension(80, 32));
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
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 11),
                new Color(60, 60, 60)
        ));
        panel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 10, 8, 10);

        for (int i = 0; i < fields.length; i++) {
            String label = (String) fields[i][0];
            JComponent comp = (JComponent) fields[i][1];
            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.25;
            gc.anchor = GridBagConstraints.NORTHWEST;
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Arial", Font.PLAIN, 10));
            panel.add(lbl, gc);

            gc.gridx = 1;
            gc.weightx = 0.75;
            gc.anchor = GridBagConstraints.WEST;
            if (comp instanceof JTextField) {
                JTextField tf = (JTextField) comp;
                tf.setFont(new Font("Courier New", Font.PLAIN, 10));
                tf.setPreferredSize(new Dimension(220, 28));
                panel.add(tf, gc);
            } else {
                panel.add(comp, gc);
            }
        }

        return panel;
    }

    private void search() {
        String q = searchMaQDField.getText().trim();
        if (q.isEmpty()) refreshTable(); else tableModel.setRows(controller.loadByMaQuyDoi(q));
    }

    private void refreshTable() { tableModel.setRows(controller.loadAll()); }

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
                    phanViField.getText()
            );
            refreshTable(); clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Delete", JOptionPane.WARNING_MESSAGE); return; }
        try { BangQuyDoi row = tableModel.getRowAt(r); controller.delete(row.getId()); refreshTable(); clearFields(); } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE); }
    }

    private void onRowSelected(ListSelectionEvent ev) {
        if (ev.getValueIsAdjusting()) return; int r = table.getSelectedRow(); if (r < 0) return; BangQuyDoi row = tableModel.getRowAt(r);
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
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa tất cả?","Xác nhận",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) { controller.deleteAll(); refreshTable(); clearFields(); }
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn file Excel để import"); fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        int result = fileChooser.showOpenDialog(this); if (result != JFileChooser.APPROVE_OPTION) return; java.io.File selectedFile = fileChooser.getSelectedFile();
        try {
            int imported = controller.importExcelFile(selectedFile);
            refreshTable(); clearFields(); JOptionPane.showMessageDialog(this, "Nhập thành công. Số dòng: " + imported, "Import", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi nhập: " + ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
