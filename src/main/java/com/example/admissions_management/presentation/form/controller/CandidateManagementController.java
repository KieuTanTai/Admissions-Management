package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.CandidateManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.presentation.form.model.CandidateForm;
import com.example.admissions_management.presentation.form.model.CandidateImportResult;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CandidateManagementController {

    private final CandidateManagementService candidateManagementService;

    public CandidateManagementController(CandidateManagementService candidateManagementService) {
        this.candidateManagementService = candidateManagementService;
    }

    public List<XtThiSinhXetTuyen25Entity> searchCandidates(String query, int page) {
        return candidateManagementService.searchCandidates(query, page);
    }

    public Optional<XtThiSinhXetTuyen25Entity> getCandidateById(Integer id) {
        return candidateManagementService.getCandidateById(id);
    }

    public void openCandidateForm(Integer candidateId) {
        JFrame frame = new JFrame(candidateId == null ? "Tạo mới thí sinh" : "Chỉnh sửa thí sinh");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField txtCccd = new JTextField(20);
        JTextField txtSoBaoDanh = new JTextField(20);
        JTextField txtHo = new JTextField(20);
        JTextField txtTen = new JTextField(20);
        JTextField txtNgaySinh = new JTextField(20);
        JTextField txtDienThoai = new JTextField(20);
        JTextField txtGioiTinh = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtNoiSinh = new JTextField(20);
        JTextField txtDoiTuong = new JTextField(20);
        JTextField txtKhuVuc = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);

        CandidateForm form = new CandidateForm();
        if (candidateId != null) {
            candidateManagementService.getCandidateById(candidateId).ifPresent(entity -> {
                form.setId(entity.getId());
                form.setCccd(entity.getCccd());
                form.setSoBaoDanh(entity.getSoBaoDanh());
                form.setHo(entity.getHo());
                form.setTen(entity.getTen());
                form.setNgaySinh(entity.getNgaySinh());
                form.setDienThoai(entity.getDienThoai());
                form.setGioiTinh(entity.getGioiTinh());
                form.setEmail(entity.getEmail());
                form.setNoiSinh(entity.getNoiSinh());
                form.setDoiTuong(entity.getDoiTuong());
                form.setKhuVuc(entity.getKhuVuc());
                form.setPassword(entity.getPassword());

                txtCccd.setText(form.getCccd());
                txtSoBaoDanh.setText(form.getSoBaoDanh());
                txtHo.setText(form.getHo());
                txtTen.setText(form.getTen());
                txtNgaySinh.setText(form.getNgaySinh());
                txtDienThoai.setText(form.getDienThoai());
                txtGioiTinh.setText(form.getGioiTinh());
                txtEmail.setText(form.getEmail());
                txtNoiSinh.setText(form.getNoiSinh());
                txtDoiTuong.setText(form.getDoiTuong());
                txtKhuVuc.setText(form.getKhuVuc());
                txtPassword.setText(form.getPassword());
            });
        }

        panel.add(new JLabel("CCCD"));
        panel.add(txtCccd);
        panel.add(new JLabel("Số Báo Danh"));
        panel.add(txtSoBaoDanh);
        panel.add(new JLabel("Họ"));
        panel.add(txtHo);
        panel.add(new JLabel("Tên"));
        panel.add(txtTen);
        panel.add(new JLabel("Ngày Sinh"));
        panel.add(txtNgaySinh);
        panel.add(new JLabel("Điện Thoại"));
        panel.add(txtDienThoai);
        panel.add(new JLabel("Giới Tính"));
        panel.add(txtGioiTinh);
        panel.add(new JLabel("Email"));
        panel.add(txtEmail);
        panel.add(new JLabel("Nơi Sinh"));
        panel.add(txtNoiSinh);
        panel.add(new JLabel("Đối Tượng"));
        panel.add(txtDoiTuong);
        panel.add(new JLabel("Khu Vực"));
        panel.add(txtKhuVuc);
        panel.add(new JLabel("Password"));
        panel.add(txtPassword);

        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> {
            try {
                form.setId(candidateId);
                form.setCccd(txtCccd.getText().trim());
                form.setSoBaoDanh(txtSoBaoDanh.getText().trim());
                form.setHo(txtHo.getText().trim());
                form.setTen(txtTen.getText().trim());
                form.setNgaySinh(txtNgaySinh.getText().trim());
                form.setDienThoai(txtDienThoai.getText().trim());
                form.setGioiTinh(txtGioiTinh.getText().trim());
                form.setEmail(txtEmail.getText().trim());
                form.setNoiSinh(txtNoiSinh.getText().trim());
                form.setDoiTuong(txtDoiTuong.getText().trim());
                form.setKhuVuc(txtKhuVuc.getText().trim());
                form.setPassword(new String(txtPassword.getPassword()).trim());

                candidateManagementService.saveCandidate(form);
                JOptionPane.showMessageDialog(frame, "Lưu thí sinh thành công.");
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(saveButton);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void deleteCandidate(Integer candidateId) {
        candidateManagementService.deleteCandidate(candidateId);
    }

    public void deleteAllCandidates() {
        int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn xóa TẤT CẢ dữ liệu thí sinh?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            candidateManagementService.deleteAllCandidates();
            JOptionPane.showMessageDialog(null, "Đã xóa tất cả dữ liệu thí sinh", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void importCandidates(List<String> files) {
        int totalImported = 0;
        List<String> errors = new ArrayList<>();

        for (String file : files) {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                errors.add("File không tồn tại: " + file);
                continue;
            }
            String lowerName = file.toLowerCase();
            try {
                if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                    // Sử dụng method tối ưu cho Excel
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        List<XtThiSinhXetTuyen25Entity> candidates = candidateManagementService.importExcelCandidates(inputStream);
                        // Batch save
                        final int BATCH_SIZE = 1000;
                        for (int i = 0; i < candidates.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, candidates.size());
                            List<XtThiSinhXetTuyen25Entity> batch = candidates.subList(i, end);
                            candidateManagementService.saveBatch(batch);
                        }
                        totalImported += candidates.size();
                    }
                } else if (lowerName.endsWith(".csv")) {
                    // Giữ nguyên cho CSV
                    CandidateImportResult result = candidateManagementService.importCsvFile(path);
                    totalImported += result.getImportedCount();
                    errors.addAll(result.getErrors());
                } else {
                    errors.add("Định dạng file không hỗ trợ: " + file);
                }
            } catch (IOException ex) {
                errors.add("Lỗi đọc file: " + file + " - " + ex.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Lỗi khi import:\n" + String.join("\n", errors), "Import Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Import thành công\nĐã import: " + totalImported + " thí sinh", "Import Thành Công", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
