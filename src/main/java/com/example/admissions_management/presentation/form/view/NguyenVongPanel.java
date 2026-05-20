package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.model.NguyenVongTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NguyenVongPanel extends JPanel {

    private static final int PAGE_SIZE = 50;

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

        setLayout(new BorderLayout(10, 10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        // Tăng lên 7 cột để chứa khít form tìm kiếm và nút bấm, nhãn trang không bị vỡ bố cục
        JPanel panel = new JPanel(new GridLayout(1, 7, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panel.add(new JLabel("Tìm theo CCCD:", SwingConstants.RIGHT));
        panel.add(searchCccdField);

        JButton searchButton = new JButton("Tìm");
        JButton importButton = new JButton("Import Excel");
        JButton refreshButton = new JButton("Refresh");

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
        // Cố định GridLayout(1, 6) ứng với chính xác 6 nút để các nút tự động co giãn đều (Auto-fit)
        JPanel btnPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton deleteAllButton = new JButton("Xóa tất cả");

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