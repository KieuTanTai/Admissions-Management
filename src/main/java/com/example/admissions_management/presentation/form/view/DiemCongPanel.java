package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.model.DiemCongTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

public class DiemCongPanel extends JPanel {

    private static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Dimension FIELD_SIZE = new Dimension(320, 30);
    private static final Dimension AREA_SIZE = new Dimension(320, 84);
    private static final Dimension LABEL_SIZE = new Dimension(130, 24);

    private final DiemCongConsoleController controller;
    private final DiemCongTableModel tableModel;

    private final JTextField searchCccdField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField tsCccdField = new JTextField();
    private final JTextField maNganhField = new JTextField();
    private final JTextField maToHopField = new JTextField();
    private final JTextField phuongThucField = new JTextField();
    private final JTextField diemCcField = new JTextField();
    private final JTextField diemUtxtField = new JTextField();
    private final JTextField diemTongField = new JTextField();
    private final JTextArea ghiChuArea = new JTextArea(4, 30);

    private final JTable table;

    public DiemCongPanel(DiemCongConsoleController controller) {
        this.controller = controller;
        this.tableModel = new DiemCongTableModel();
        this.table = new JTable(tableModel);

        setLayout(new BorderLayout(10, 10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildEditPanel(), BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        // Row 0: label + search field (expand)
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.NONE; c.weightx = 0.0;
        panel.add(new JLabel("Tìm theo CCCD"), c);

        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        panel.add(searchCccdField, c);

        // Row 0: buttons aligned to right
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");
        JButton importButton = new JButton("Import Excel");
        JButton deleteAllButton = new JButton("Delete All");
        rightButtons.add(searchButton);
        rightButtons.add(refreshButton);
        rightButtons.add(clearButton);
        rightButtons.add(importButton);
        rightButtons.add(deleteAllButton);

        c.gridx = 2; c.gridy = 0; c.fill = GridBagConstraints.NONE; c.weightx = 0.0;
        panel.add(rightButtons, c);

        searchButton.addActionListener(e -> search());
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearFields());
        importButton.addActionListener(e -> importExcel());
        deleteAllButton.addActionListener(e -> deleteAllData());
        return panel;
    }

    private JPanel buildEditPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setPreferredSize(new Dimension(560, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Main content panel (scrollable)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, 0);

        // Section 1: ID Info
        JPanel section1 = createFieldSection("Thông Tin Chính", new Object[][] {
            { "ID", idField },
            { "CCCD", tsCccdField }
        });
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0; contentPanel.add(section1, gc);

        // Section 2: Ngành và Tổ Hợp
        JPanel section2 = createFieldSection("Ngành & Tổ Hợp", new Object[][] {
            { "Mã Ngành", maNganhField },
            { "Mã Tổ Hợp", maToHopField }
        });
        gc.gridy = 1; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section2, gc);

        // Section 3: Phương thức
        JPanel section3 = createFieldSection("Xét Tuyển", new Object[][] {
            { "Phương Thức", phuongThucField }
        });
        gc.gridy = 2; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section3, gc);

        // Section 4: Điểm
        JPanel section4 = createFieldSection("Điểm", new Object[][] {
            { "Điểm CC", diemCcField },
            { "Điểm UT", diemUtxtField },
            { "Tổng Điểm", diemTongField }
        });
        gc.gridy = 3; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section4, gc);

        // Section 5: Ghi Chú
        JPanel section5 = createFieldSection("Ghi Chú", new Object[][] {
            { "Nội Dung", ghiChuArea }
        });
        gc.gridy = 4; gc.insets = new Insets(10, 0, 0, 0); gc.weighty = 1.0; contentPanel.add(section5, gc);

        // Filler
        gc.gridy = 5; gc.weighty = 1.0; contentPanel.add(new JPanel(), gc);

        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        outer.add(scrollPane, BorderLayout.CENTER);

        // Button panel
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

        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearFields());

        outer.add(btnPanel, BorderLayout.SOUTH);

        idField.setEditable(false);

        return outer;
    }

    private JPanel createFieldSection(String title, Object[][] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                SECTION_TITLE_FONT,
                new Color(45, 45, 45)
        ));
        panel.setBackground(new Color(250, 250, 250));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 10, 8, 10);

        for (int i = 0; i < fields.length; i++) {
            String label = (String) fields[i][0];
            Object comp = fields[i][1];

            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.0;
            gc.gridwidth = 1;
            gc.anchor = GridBagConstraints.NORTHWEST;
            JLabel lbl = new JLabel(label);
            lbl.setFont(LABEL_FONT);
            lbl.setForeground(new Color(50, 50, 50));
            lbl.setPreferredSize(LABEL_SIZE);
            lbl.setMinimumSize(LABEL_SIZE);
            lbl.setMaximumSize(LABEL_SIZE);
            panel.add(lbl, gc);

            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.anchor = GridBagConstraints.WEST;
            
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                if (jcomp instanceof JTextArea) {
                    JTextArea area = (JTextArea) jcomp;
                    area.setFont(FIELD_FONT);
                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);
                    area.setMargin(new Insets(6, 8, 6, 8));
                    area.setBackground(new Color(255, 255, 255));
                    area.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                    
                    JScrollPane scrollPane = new JScrollPane(area);
                    scrollPane.setPreferredSize(AREA_SIZE);
                    scrollPane.setMinimumSize(AREA_SIZE);
                    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, AREA_SIZE.height));
                    scrollPane.setBorder(null);
                    panel.add(scrollPane, gc);
                } else if (jcomp instanceof JTextField) {
                    JTextField tf = (JTextField) jcomp;
                    tf.setFont(FIELD_FONT);
                    tf.setMargin(new Insets(6, 8, 6, 8));
                    tf.setBackground(new Color(255, 255, 255));
                    tf.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                    tf.setPreferredSize(FIELD_SIZE);
                    tf.setMinimumSize(new Dimension(0, FIELD_SIZE.height));
                    tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_SIZE.height));
                    panel.add(tf, gc);
                }
            }
        }

        return panel;
    }

    private void search() {
        String cccd = searchCccdField.getText().trim();
        if (cccd.isEmpty()) {
            refreshTable();
        } else {
            tableModel.setRows(controller.loadByCccd(cccd));
        }
    }

    private void refreshTable() {
        tableModel.setRows(controller.loadAll());
    }

    private void save() {
        try {
            controller.save(
                    tsCccdField.getText(),
                    maNganhField.getText(),
                    maToHopField.getText(),
                    phuongThucField.getText(),
                    diemCcField.getText(),
                    diemUtxtField.getText(),
                    diemTongField.getText(),
                    ghiChuArea.getText()
            );
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            DiemCongXetTuyen row = tableModel.getRowAt(selectedRow);
            controller.delete(row.getId());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        DiemCongXetTuyen row = tableModel.getRowAt(selectedRow);
        idField.setText(row.getId() == null ? "" : String.valueOf(row.getId()));
        tsCccdField.setText(row.getTsCccd());
        maNganhField.setText(row.getMaNganh());
        maToHopField.setText(row.getMaToHop());
        phuongThucField.setText(row.getPhuongThuc());
        diemCcField.setText(row.getDiemCc() == null ? "" : row.getDiemCc().toPlainString());
        diemUtxtField.setText(row.getDiemUtxt() == null ? "" : row.getDiemUtxt().toPlainString());
        diemTongField.setText(row.getDiemTong() == null ? "" : row.getDiemTong().toPlainString());
        ghiChuArea.setText(row.getGhiChu());
    }

    private void clearFields() {
        searchCccdField.setText("");
        idField.setText("");
        tsCccdField.setText("");
        maNganhField.setText("");
        maToHopField.setText("");
        phuongThucField.setText("");
        diemCcField.setText("");
        diemUtxtField.setText("");
        diemTongField.setText("");
        ghiChuArea.setText("");
        table.clearSelection();
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tất cả dữ liệu? Hành động này không thể hoàn tác!",
                "Xác nhận xóa tất cả",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteAll();
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Xóa tất cả dữ liệu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
            }
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
            DiemCongImportSummary summary = controller.importExcelFile(selectedFile);
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this,
                summary.toMessage(),
                "Import Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Lỗi khi nhập dữ liệu: " + ex.getMessage(), 
                    "Import Failed", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}