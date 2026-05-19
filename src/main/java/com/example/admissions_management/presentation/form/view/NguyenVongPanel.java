package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.model.NguyenVongTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NguyenVongPanel extends JPanel {

<<<<<<< HEAD
    private static final int PAGE_SIZE = 50;
=======
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
    private static final Dimension FIELD_SIZE = new Dimension(320, 30);
    private static final Dimension LABEL_SIZE = new Dimension(130, 24);
>>>>>>> UI-test

    private final NguyenVongConsoleController controller;
    private final NguyenVongTableModel tableModel;
    private final JTable table;

    private final JTextField searchCccdField = new JTextField();
    private final JLabel pageLabel = new JLabel("Trang: 1 / 1");
    private final JButton prevButton = new JButton("<< Trước");
    private final JButton nextButton = new JButton("Sau >>");
    private List<NguyenVongXetTuyen> currentRows = new ArrayList<>();
    private int page = 0;
    private String currentQuery = "";

    public NguyenVongPanel(NguyenVongConsoleController controller) {
        this.controller = controller;
        this.tableModel = new NguyenVongTableModel();
        this.table = new JTable(tableModel);
        
        // Chỉ cho phép chọn một dòng để tránh lỗi xử lý dữ liệu đơn dòng
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        styleTable();
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildTopPanel() {
<<<<<<< HEAD
        // Tăng lên 7 cột để chứa khít form tìm kiếm và nút bấm, nhãn trang không bị vỡ bố cục
        JPanel panel = new JPanel(new GridLayout(1, 7, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panel.add(new JLabel("Tìm theo CCCD:", SwingConstants.RIGHT));
        panel.add(searchCccdField);

        JButton searchButton = new JButton("Tìm");
        JButton importButton = new JButton("Import Excel");
        JButton refreshButton = new JButton("Refresh");
=======
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JLabel searchLabel = new JLabel("Tìm theo CCCD");
        searchLabel.setFont(LABEL_FONT);
        searchLabel.setForeground(MUTED_COLOR);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; panel.add(searchLabel, c);
        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        styleTextField(searchCccdField);
        panel.add(searchCccdField, c);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        JButton searchButton = new JButton("Tìm");
        JButton refreshButton = new JButton("Làm mới");
        JButton clearButton = new JButton("Xóa lọc");
        JButton processMajorButton = new JButton("Xét ngành");
        JButton processAllButton = new JButton("Xét tất cả");
        JButton deleteAllButton = new JButton("Xóa tất cả");
        styleButton(searchButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(refreshButton, SURFACE_COLOR, TEXT_COLOR);
        styleButton(clearButton, SURFACE_COLOR, TEXT_COLOR);
        styleButton(processMajorButton, ACCENT_COLOR, TEXT_COLOR);
        styleButton(processAllButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(deleteAllButton, DANGER_COLOR, Color.WHITE);
        rightButtons.add(searchButton);
        rightButtons.add(refreshButton);
        rightButtons.add(clearButton);
        rightButtons.add(processMajorButton);
        rightButtons.add(processAllButton);
        rightButtons.add(deleteAllButton);
>>>>>>> UI-test

        panel.add(searchButton);
        panel.add(importButton);
        panel.add(refreshButton);
        panel.add(pageLabel);

        searchButton.addActionListener(e -> search());
        importButton.addActionListener(e -> importExcel());
        refreshButton.addActionListener(e -> {
            searchCccdField.setText("");
            refreshTable();
        });

        return panel;
    }

<<<<<<< HEAD
    private JPanel buildActionPanel() {
        // Cố định GridLayout(1, 6) ứng với chính xác 6 nút để các nút tự động co giãn đều (Auto-fit)
        JPanel btnPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton deleteAllButton = new JButton("Xóa tất cả");

        btnPanel.add(addButton);
        btnPanel.add(editButton);
=======
    private JPanel buildEditPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setPreferredSize(new Dimension(560, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outer.setBackground(BG_COLOR);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, 0);

        JPanel section1 = createFieldSection("Thông Tin Chính", new Object[][] {
            { "ID", idField },
            { "CCCD", nnCccdField }
        });
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0; contentPanel.add(section1, gc);

        JPanel section2 = createFieldSection("Ngành & Tổ Hợp", new Object[][] {
            { "Mã Ngành", maNganhField },
            { "Mã Tổ Hợp", maToHopField }
        });
        gc.gridy = 1; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section2, gc);

        JPanel section3 = createFieldSection("Nguyện Vọng", new Object[][] {
            { "NV Thứ Tự", nvThuTuField }
        });
        gc.gridy = 2; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section3, gc);

        JPanel section4 = createFieldSection("Điểm", new Object[][] {
            { "Điểm Thi", diemThxtField },
            { "Điểm UTQD", diemUtqdField },
            { "Điểm Cộng", diemCongField },
            { "Điểm Xét Tuyển", diemXetTuyenField }
        });
        gc.gridy = 3; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section4, gc);

        JPanel section5 = createFieldSection("Trạng Thái", new Object[][] {
            { "Kết Quả", ketQuaField },
            { "NV Keys", nvKeysField },
            { "Phương Thức", phuongThucField },
            { "TT THM", ttThmField }
        });
        gc.gridy = 4; gc.insets = new Insets(10, 0, 0, 0); contentPanel.add(section5, gc);

        gc.gridy = 5; gc.weighty = 1.0; contentPanel.add(new JPanel(), gc);

        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        outer.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE_COLOR);
        btnPanel.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        JButton saveButton = new JButton("Lưu");
        JButton deleteButton = new JButton("Xóa");
        JButton clearButton = new JButton("Xóa form");
        styleButton(saveButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(deleteButton, DANGER_COLOR, Color.WHITE);
        styleButton(clearButton, SURFACE_COLOR, TEXT_COLOR);

        saveButton.setPreferredSize(new Dimension(80, 32));
        deleteButton.setPreferredSize(new Dimension(80, 32));
        clearButton.setPreferredSize(new Dimension(80, 32));

        btnPanel.add(saveButton);
>>>>>>> UI-test
        btnPanel.add(deleteButton);
        btnPanel.add(deleteAllButton);
        btnPanel.add(prevButton);
        btnPanel.add(nextButton);

<<<<<<< HEAD
        addButton.addActionListener(e -> openEditDialog(null));
        editButton.addActionListener(e -> {
            NguyenVongXetTuyen selected = getSelectedRow();
            if (selected != null) {
                openEditDialog(selected);
=======
        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearFields());

        outer.add(btnPanel, BorderLayout.SOUTH);

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
                TEXT_COLOR
        ));
        panel.setBackground(SOFT_COLOR);

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
            lbl.setForeground(MUTED_COLOR);
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
                if (jcomp instanceof JTextField) {
                    JTextField tf = (JTextField) jcomp;
                    styleTextField(tf);
                    tf.setPreferredSize(FIELD_SIZE);
                    tf.setMinimumSize(new Dimension(0, FIELD_SIZE.height));
                    tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_SIZE.height));
                    panel.add(tf, gc);
                }
>>>>>>> UI-test
            }
        });
        deleteButton.addActionListener(e -> deleteSelected());
        deleteAllButton.addActionListener(e -> deleteAllData());
        prevButton.addActionListener(e -> goPrevPage());
        nextButton.addActionListener(e -> goNextPage());

        updatePagingControls();

        return btnPanel;
    }

    private void styleTable() {
        table.setRowHeight(30);
        table.setFont(FIELD_FONT);
        table.setGridColor(new Color(237, 242, 239));
        table.setSelectionBackground(new Color(218, 239, 232));
        table.setSelectionForeground(TEXT_COLOR);
        table.getTableHeader().setFont(BUTTON_FONT);
        table.getTableHeader().setForeground(new Color(64, 93, 83));
        table.getTableHeader().setBackground(new Color(242, 247, 244));
    }

    private void styleTextField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setMargin(new Insets(6, 8, 6, 8));
        field.setBackground(SURFACE_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBorder(BorderFactory.createLineBorder(LINE_COLOR, 1));
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    private void search() {
        String cccd = searchCccdField.getText().trim();
        if (cccd.isEmpty()) {
            refreshTable();
        } else {
            currentQuery = cccd;
            currentRows = controller.loadByCccd(cccd);
            page = 0;
            updateTablePage();
        }
    }

    private void refreshTable() {
<<<<<<< HEAD
        currentQuery = "";
        currentRows = controller.loadAll();
        page = 0;
        updateTablePage();
=======
        tableModel.setRows(controller.loadAll());
        table.clearSelection();
        clearDetailFields();
    }

    private void save() {
        try {
            controller.save(
                    nnCccdField.getText(),
                    maNganhField.getText(),
                    maToHopField.getText(),
                    nvThuTuField.getText(),
                    diemThxtField.getText(),
                    diemUtqdField.getText()
            );
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
        }
>>>>>>> UI-test
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
<<<<<<< HEAD
        
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn xóa nguyện vọng này?", "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                NguyenVongXetTuyen row = tableModel.getRowAt(modelRow);
                controller.delete(row.getId());
                reloadCurrentData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
=======
        try {
            NguyenVongXetTuyen row = tableModel.getRowAt(selectedRow);
            controller.delete(row.getId());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
>>>>>>> UI-test
        }
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tất cả dữ liệu nguyện vọng? Hành động này không thể hoàn tác!",
                "Xác nhận xóa tất cả",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteAll();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Xóa tất cả dữ liệu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel nguyện vọng để import");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File selectedFile = fileChooser.getSelectedFile();
        
        // Hiển thị trạng thái đang đọc luồng ngầm cho người dùng biết
        pageLabel.setText("Đang đọc Excel...");
        
        // SỬ DỤNG SWINGWORKER ĐỂ KHÔNG BỊ ĐƠ GIAO DIỆN KHI IMPORT DỮ LIỆU LỚN
        SwingWorker<com.example.admissions_management.application.dto.response.NguyenVongImportSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected com.example.admissions_management.application.dto.response.NguyenVongImportSummary doInBackground() throws Exception {
                // Đẩy tác vụ xử lý tệp Excel xuống Thread nền phụ
                return controller.importExcelFile(selectedFile);
            }

            @Override
            protected void done() {
                try {
                    com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary = (com.example.admissions_management.application.dto.response.NguyenVongImportSummary) get();
                    refreshTable(); // Cập nhật lại UI bảng hiển thị dữ liệu mới
                    JOptionPane.showMessageDialog(NguyenVongPanel.this,
                            summary.toMessage(),
                            "Import Summary",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NguyenVongPanel.this,
                            "Lỗi khi nhập dữ liệu: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            "Import Failed",
                            JOptionPane.ERROR_MESSAGE);
                    updatePagingControls();
                }
            }
        };
        
        worker.execute(); // Bắt đầu chạy tiến trình
    }

    private void openEditDialog(NguyenVongXetTuyen existing) {
        JTextField nnCccdField = new JTextField();
        JTextField maNganhField = new JTextField();
        JTextField maToHopField = new JTextField();
        JTextField nvThuTuField = new JTextField();
        JTextField diemThxtField = new JTextField();
        JTextField diemUtqdField = new JTextField();

        if (existing != null) {
            nnCccdField.setText(existing.getNnCccd());
            maNganhField.setText(existing.getNvMaNganh());
            maToHopField.setText(existing.getMaToHop());
            nvThuTuField.setText(existing.getNvThuTu() == null ? "" : existing.getNvThuTu().toString());
            diemThxtField.setText(existing.getDiemThxt() == null ? "" : existing.getDiemThxt().toPlainString());
            diemUtqdField.setText(existing.getDiemUtqd() == null ? "" : existing.getDiemUtqd().toPlainString());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("CCCD"));
        form.add(nnCccdField);
        form.add(new JLabel("Mã Ngành"));
        form.add(maNganhField);
        form.add(new JLabel("Mã Tổ Hợp"));
        form.add(maToHopField);
        form.add(new JLabel("NV Thứ Tự"));
        form.add(nvThuTuField);
        form.add(new JLabel("Điểm THXT"));
        form.add(diemThxtField);
        form.add(new JLabel("Điểm UTQD"));
        form.add(diemUtqdField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                existing == null ? "Thêm nguyện vọng" : "Sửa nguyện vọng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            if (existing == null) {
                save(nnCccdField.getText().trim(),
                        maNganhField.getText().trim(),
                        maToHopField.getText().trim(),
                        nvThuTuField.getText().trim(),
                        diemThxtField.getText().trim(),
                        diemUtqdField.getText().trim());
            } else {
                update(existing.getId(),
                        nnCccdField.getText().trim(),
                        maNganhField.getText().trim(),
                        maToHopField.getText().trim(),
                        nvThuTuField.getText().trim(),
                        diemThxtField.getText().trim(),
                        diemUtqdField.getText().trim());
            }
        }
    }

    private void save(String nnCccd, String maNganh, String maToHop, String nvThuTu, String diemThxt, String diemUtqd) {
        try {
            controller.save(nnCccd, maNganh, maToHop, nvThuTu, diemThxt, diemUtqd);
            reloadCurrentData();
            JOptionPane.showMessageDialog(this, "Lưu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update(Integer id, String nnCccd, String maNganh, String maToHop, String nvThuTu, String diemThxt, String diemUtqd) {
        try {
            controller.update(id, nnCccd, maNganh, maToHop, nvThuTu, diemThxt, diemUtqd);
            reloadCurrentData();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NguyenVongXetTuyen getSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để thực hiện.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        return tableModel.getRowAt(modelRow);
    }

    private void reloadCurrentData() {
        if (currentQuery == null || currentQuery.isEmpty()) {
            currentRows = controller.loadAll();
        } else {
            currentRows = controller.loadByCccd(currentQuery);
        }
        int maxPage = Math.max(0, (currentRows.size() - 1) / PAGE_SIZE);
        page = Math.min(page, maxPage);
        updateTablePage();
    }

    private void updateTablePage() {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, currentRows.size());
        List<NguyenVongXetTuyen> slice = start < end ? currentRows.subList(start, end) : new ArrayList<>();
        tableModel.setRows(slice);
        updatePagingControls();
        
        // Buộc giao diện cập nhật và render lại cấu trúc dòng mới
        revalidate();
        repaint();
    }

    private void updatePagingControls() {
        int totalPages = Math.max(1, (int) Math.ceil(currentRows.size() / (double) PAGE_SIZE));
        pageLabel.setText(String.format("Trang: %d / %d", (page + 1), totalPages));
        prevButton.setEnabled(page > 0);
        nextButton.setEnabled(page + 1 < totalPages);
    }

    private void goPrevPage() {
        if (page > 0) {
            page--;
            updateTablePage();
        }
    }

    private void goNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil(currentRows.size() / (double) PAGE_SIZE));
        if (page + 1 < totalPages) {
            page++;
            updateTablePage();
        }
    }
}