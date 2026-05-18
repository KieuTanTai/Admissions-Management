package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.model.DiemCongTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DiemCongPanel extends JPanel {

    private static final int PAGE_SIZE = 50;

    private final DiemCongConsoleController controller;
    private final DiemCongTableModel tableModel;

    private final JTextField searchCccdField = new JTextField();
    private final JTable table;
    private final JLabel pageLabel = new JLabel("Page: 1");
    private final JButton prevButton = new JButton("<< Trước");
    private final JButton nextButton = new JButton("Sau >>");
    private List<DiemCongXetTuyen> currentRows = new ArrayList<>();
    private int page = 0;
    private String currentQuery = "";

    public DiemCongPanel(DiemCongConsoleController controller) {
        this.controller = controller;
        this.tableModel = new DiemCongTableModel();
        this.table = new JTable(tableModel);

        setLayout(new BorderLayout(10, 10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panel.add(new JLabel("Tìm theo CCCD"));
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
        JPanel btnPanel = new JPanel(new GridLayout(1, 6, 8, 8));
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
        editButton.addActionListener(e -> openEditDialog(getSelectedRow()));
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

    private void save(Long id,
                      String tsCccd,
                      String maNganh,
                      String maToHop,
                      String phuongThuc,
                      String diemCc,
                      String diemUtxt,
                      String diemTong,
                      String ghiChu) {
        try {
            controller.save(id, tsCccd, maNganh, maToHop, phuongThuc, diemCc, diemUtxt, diemTong, ghiChu);
            reloadCurrentData();
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
            reloadCurrentData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
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
                reloadCurrentData();
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
            reloadCurrentData();
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

    private void openEditDialog(DiemCongXetTuyen existing) {
        JTextField tsCccdField = new JTextField();
        JTextField maNganhField = new JTextField();
        JTextField maToHopField = new JTextField();
        JTextField phuongThucField = new JTextField();
        JTextField diemCcField = new JTextField();
        JTextField diemUtxtField = new JTextField();
        JTextField diemTongField = new JTextField();
        JTextArea ghiChuArea = new JTextArea(4, 22);
        JScrollPane ghiChuScroll = new JScrollPane(ghiChuArea);

        if (existing != null) {
            tsCccdField.setText(existing.getTsCccd());
            maNganhField.setText(existing.getMaNganh());
            maToHopField.setText(existing.getMaToHop());
            phuongThucField.setText(existing.getPhuongThuc());
            diemCcField.setText(existing.getDiemCc() == null ? "" : existing.getDiemCc().toPlainString());
            diemUtxtField.setText(existing.getDiemUtxt() == null ? "" : existing.getDiemUtxt().toPlainString());
            diemTongField.setText(existing.getDiemTong() == null ? "" : existing.getDiemTong().toPlainString());
            ghiChuArea.setText(existing.getGhiChu());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("CCCD"));
        form.add(tsCccdField);
        form.add(new JLabel("Mã Ngành"));
        form.add(maNganhField);
        form.add(new JLabel("Mã Tổ Hợp"));
        form.add(maToHopField);
        form.add(new JLabel("Phương Thức"));
        form.add(phuongThucField);
        form.add(new JLabel("Điểm CC"));
        form.add(diemCcField);
        form.add(new JLabel("Điểm UT"));
        form.add(diemUtxtField);
        form.add(new JLabel("Tổng Điểm"));
        form.add(diemTongField);
        form.add(new JLabel("Ghi Chú"));
        form.add(ghiChuScroll);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                existing == null ? "Thêm điểm cộng" : "Sửa điểm cộng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            save(existing == null ? null : existing.getId(),
                    tsCccdField.getText(),
                    maNganhField.getText(),
                    maToHopField.getText(),
                    phuongThucField.getText(),
                    diemCcField.getText(),
                    diemUtxtField.getText(),
                    diemTongField.getText(),
                    ghiChuArea.getText());
        }
    }

    private DiemCongXetTuyen getSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để sửa.", "Sửa", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tableModel.getRowAt(selectedRow);
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
        List<DiemCongXetTuyen> slice = start < end ? currentRows.subList(start, end) : new ArrayList<>();
        tableModel.setRows(slice);
        updatePagingControls();
    }

    private void updatePagingControls() {
        int totalPages = Math.max(1, (int) Math.ceil(currentRows.size() / (double) PAGE_SIZE));
        pageLabel.setText("Page: " + (page + 1));
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