package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.model.NguyenVongTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class NguyenVongPanel extends JPanel {

    private static final int PAGE_SIZE = 50;
    private static final Color BG_COLOR = new Color(245, 247, 244);
    private static final Color SURFACE_COLOR = Color.WHITE;
    private static final Color LINE_COLOR = new Color(220, 229, 223);
    private static final Color PRIMARY_COLOR = new Color(12, 122, 99);
    private static final Color ACCENT_COLOR = new Color(231, 150, 45);
    private static final Color DANGER_COLOR = new Color(194, 65, 53);
    private static final Color TEXT_COLOR = new Color(29, 46, 40);
    private static final Color MUTED_COLOR = new Color(100, 118, 110);

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

        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        styleTable();

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 7, 8, 8));
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel searchLabel = new JLabel("Tìm theo CCCD:", SwingConstants.RIGHT);
        searchLabel.setForeground(MUTED_COLOR);
        panel.add(searchLabel);

        styleTextField(searchCccdField);
        panel.add(searchCccdField);

        JButton searchButton = createButton("Tìm", ButtonType.PRIMARY);
        JButton importButton = createButton("Import Excel", ButtonType.ACCENT);
        JButton refreshButton = createButton("Làm mới", ButtonType.SECONDARY);
        pageLabel.setForeground(TEXT_COLOR);

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

    private JPanel buildActionPanel() {
        JPanel btnPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        btnPanel.setBackground(SURFACE_COLOR);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JButton addButton = createButton("Thêm", ButtonType.PRIMARY);
        JButton editButton = createButton("Sửa", ButtonType.SECONDARY);
        JButton deleteButton = createButton("Xóa", ButtonType.DANGER);
        JButton deleteAllButton = createButton("Xóa tất cả", ButtonType.DANGER);
        stylePagingButton(prevButton);
        stylePagingButton(nextButton);

        btnPanel.add(addButton);
        btnPanel.add(editButton);
        btnPanel.add(deleteButton);
        btnPanel.add(deleteAllButton);
        btnPanel.add(prevButton);
        btnPanel.add(nextButton);

        addButton.addActionListener(e -> openEditDialog(null));
        editButton.addActionListener(e -> {
            NguyenVongXetTuyen selected = getSelectedRow();
            if (selected != null) {
                openEditDialog(selected);
            }
        });
        deleteButton.addActionListener(e -> deleteSelected());
        deleteAllButton.addActionListener(e -> deleteAllData());
        prevButton.addActionListener(e -> goPrevPage());
        nextButton.addActionListener(e -> goNextPage());

        updatePagingControls();
        return btnPanel;
    }

    private void styleTextField(JTextField field) {
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private JButton createButton(String text, ButtonType type) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        if (type == ButtonType.PRIMARY) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR.darker()));
        } else if (type == ButtonType.ACCENT) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(TEXT_COLOR);
            button.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR.darker()));
        } else if (type == ButtonType.DANGER) {
            button.setBackground(DANGER_COLOR);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(DANGER_COLOR.darker()));
        } else {
            button.setBackground(SURFACE_COLOR);
            button.setForeground(TEXT_COLOR);
            button.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        }

        return button;
    }

    private void stylePagingButton(JButton button) {
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(SURFACE_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
    }

    private void styleTable() {
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(236, 244, 239));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.setSelectionBackground(new Color(216, 235, 226));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(LINE_COLOR);
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
        currentQuery = "";
        currentRows = controller.loadAll();
        page = 0;
        updateTablePage();
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn xóa nguyện vọng này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                NguyenVongXetTuyen row = tableModel.getRowAt(modelRow);
                controller.delete(row.getId());
                reloadCurrentData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tất cả dữ liệu nguyện vọng? Hành động này không thể hoàn tác.",
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
        pageLabel.setText("Đang đọc Excel...");

        SwingWorker<com.example.admissions_management.application.dto.response.NguyenVongImportSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected com.example.admissions_management.application.dto.response.NguyenVongImportSummary doInBackground() throws Exception {
                return controller.importExcelFile(selectedFile);
            }

            @Override
            protected void done() {
                try {
                    com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary =
                            (com.example.admissions_management.application.dto.response.NguyenVongImportSummary) get();
                    refreshTable();
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

        worker.execute();
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
        form.add(new JLabel("Mã ngành"));
        form.add(maNganhField);
        form.add(new JLabel("Mã tổ hợp"));
        form.add(maToHopField);
        form.add(new JLabel("NV thứ tự"));
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

    private enum ButtonType {
        PRIMARY,
        SECONDARY,
        ACCENT,
        DANGER
    }
}
