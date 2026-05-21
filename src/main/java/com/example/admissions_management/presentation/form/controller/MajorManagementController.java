package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.MajorManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.presentation.form.model.MajorForm;
import com.example.admissions_management.presentation.form.model.MajorImportResult;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class MajorManagementController {

    private final MajorManagementService majorManagementService;

    public MajorManagementController(MajorManagementService majorManagementService) {
        this.majorManagementService = majorManagementService;
    }

    public List<XtNganhEntity> searchMajors(String query, int page) {
        return majorManagementService.searchMajors(query, page);
    }

    public Optional<XtNganhEntity> getMajorById(Integer id) {
        return majorManagementService.getMajorById(id);
    }

    public void openMajorForm(Integer majorId) {
        JFrame frame = new JFrame(majorId == null ? "Tạo mới ngành tuyển sinh" : "Chỉnh sửa ngành tuyển sinh");
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        // Tạo các textfield
        JTextField txtMaNganh = new JTextField(20);
        JTextField txtTenNganh = new JTextField(20);
        JTextField txtToHopGoc = new JTextField(20);
        JTextField txtChiTieu = new JTextField(20);
        JTextField txtDiemSan = new JTextField(20);
        JTextField txtDiemTrungTuyen = new JTextField(20);
        JTextField txtTuyenThang = new JTextField(20);
        JTextField txtDgnl = new JTextField(20);
        JTextField txtThpt = new JTextField(20);
        JTextField txtVsat = new JTextField(20);
        JTextField txtSlXtt = new JTextField(20);
        JTextField txtSlDgnl = new JTextField(20);
        JTextField txtSlVsat = new JTextField(20);
        JTextField txtSlThpt = new JTextField(20);

        MajorForm form = new MajorForm();

        // Nếu chỉnh sửa, load dữ liệu từ DB
        if (majorId != null) {
            majorManagementService.getMajorById(majorId).ifPresent(entity -> {
                form.setIdnganh(entity.getId());
                txtMaNganh.setText(nvl(entity.getMaNganh()));
                txtTenNganh.setText(nvl(entity.getTenNganh()));
                txtToHopGoc.setText(nvl(entity.getToHopGoc()));
                txtChiTieu.setText(nvl(entity.getChiTieu()));
                txtDiemSan.setText(nvl(entity.getDiemSan()));
                txtDiemTrungTuyen.setText(nvl(entity.getDiemTrungTuyen()));
                txtTuyenThang.setText(nvl(entity.getTuyenThang()));
                txtDgnl.setText(nvl(entity.getDgnl()));
                txtThpt.setText(nvl(entity.getThpt()));
                txtVsat.setText(nvl(entity.getVsat()));
                txtSlXtt.setText(nvl(entity.getSlXtt()));
                txtSlDgnl.setText(nvl(entity.getSlDgnl()));
                txtSlVsat.setText(nvl(entity.getSlVsat()));
                txtSlThpt.setText(nvl(entity.getSlThpt()));
            });
        }

        // Thêm label + textfield vào panel
        panel.add(new JLabel("Mã ngành")); panel.add(txtMaNganh);
        panel.add(new JLabel("Tên ngành")); panel.add(txtTenNganh);
        panel.add(new JLabel("Tổ hợp gốc")); panel.add(txtToHopGoc);
        panel.add(new JLabel("Chỉ tiêu")); panel.add(txtChiTieu);
        panel.add(new JLabel("Điểm sàn")); panel.add(txtDiemSan);
        panel.add(new JLabel("Điểm trúng tuyển")); panel.add(txtDiemTrungTuyen);
        panel.add(new JLabel("Tuyển thẳng")); panel.add(txtTuyenThang);
        panel.add(new JLabel("ĐGNL")); panel.add(txtDgnl);
        panel.add(new JLabel("THPT")); panel.add(txtThpt);
        panel.add(new JLabel("V-SAT")); panel.add(txtVsat);
        panel.add(new JLabel("SL XTT")); panel.add(txtSlXtt);
        panel.add(new JLabel("SL ĐGNL")); panel.add(txtSlDgnl);
        panel.add(new JLabel("SL V-SAT")); panel.add(txtSlVsat);
        panel.add(new JLabel("SL THPT")); panel.add(txtSlThpt);

        // Nút Lưu
        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> {
            try {
                if (majorId == null) {
                    // ADD: tạo entity mới
                    XtNganhEntity entity = new XtNganhEntity();
                    entity.setMaNganh(txtMaNganh.getText().trim());
                    entity.setTenNganh(txtTenNganh.getText().trim());
                    entity.setToHopGoc(txtToHopGoc.getText().trim());
                    entity.setChiTieu(toInteger(txtChiTieu.getText()));
                    entity.setDiemSan(toBigDecimal(txtDiemSan.getText()));
                    entity.setDiemTrungTuyen(toBigDecimal(txtDiemTrungTuyen.getText()));
                    entity.setTuyenThang(txtTuyenThang.getText().trim());
                    entity.setDgnl(txtDgnl.getText().trim());
                    entity.setThpt(txtThpt.getText().trim());
                    entity.setVsat(txtVsat.getText().trim());
                    entity.setSlXtt(toInteger(txtSlXtt.getText()));
                    entity.setSlDgnl(toInteger(txtSlDgnl.getText()));
                    entity.setSlVsat(toInteger(txtSlVsat.getText()));
                    entity.setSlThpt(toInteger(txtSlThpt.getText()));

                    majorManagementService.saveMajor(entity);
                    JOptionPane.showMessageDialog(frame, "Thêm ngành thành công.");
                    frame.dispose();
                } else {
                    // EDIT: lấy entity từ DB
                    majorManagementService.getMajorById(majorId).ifPresent(entity -> {
                        entity.setMaNganh(txtMaNganh.getText().trim());
                        entity.setTenNganh(txtTenNganh.getText().trim());
                        entity.setToHopGoc(txtToHopGoc.getText().trim());
                        entity.setChiTieu(toInteger(txtChiTieu.getText()));
                        entity.setDiemSan(toBigDecimal(txtDiemSan.getText()));
                        entity.setDiemTrungTuyen(toBigDecimal(txtDiemTrungTuyen.getText()));
                        entity.setTuyenThang(txtTuyenThang.getText().trim());
                        entity.setDgnl(txtDgnl.getText().trim());
                        entity.setThpt(txtThpt.getText().trim());
                        entity.setVsat(txtVsat.getText().trim());
                        entity.setSlXtt(toInteger(txtSlXtt.getText()));
                        entity.setSlDgnl(toInteger(txtSlDgnl.getText()));
                        entity.setSlVsat(toInteger(txtSlVsat.getText()));
                        entity.setSlThpt(toInteger(txtSlThpt.getText()));

                        majorManagementService.saveMajor(entity);
                        JOptionPane.showMessageDialog(frame, "Sửa ngành thành công.");
                        frame.dispose();
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(panel);
        root.add(saveButton);

        frame.add(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void deleteMajor(Integer majorId) {
        int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn xóa ngành này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            majorManagementService.deleteMajor(majorId);
            JOptionPane.showMessageDialog(null, "Đã xóa ngành.", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteAllMajors() {
        int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn xóa TẤT CẢ dữ liệu ngành tuyển sinh?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            majorManagementService.deleteAllMajors();
            JOptionPane.showMessageDialog(null, "Đã xóa tất cả dữ liệu ngành.", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void importMajors(List<String> files) {
        int totalImported = 0;
        int totalUpdated = 0;
        StringBuilder errors = new StringBuilder();

        for (String file : files) {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                errors.append("File không tồn tại: ").append(file).append("\n");
                continue;
            }
            String lowerName = file.toLowerCase();
            if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
                errors.append("Định dạng không hỗ trợ: ").append(file).append("\n");
                continue;
            }
            MajorImportResult result = majorManagementService.importExcelFile(path);
            totalImported += result.getImportedCount();
            totalUpdated += result.getUpdatedCount();
            if (result.hasErrors()) {
                for (String error : result.getErrors()) {
                    errors.append(error).append("\n");
                }
            }
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(null, "Import có lỗi:\n" + errors, "Import lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Import ngành thành công\nThêm mới: " + totalImported + "\nCập nhật: " + totalUpdated, "Import thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Integer toInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            return new BigDecimal(value.trim().replace(",", "")).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }

    private static String nvl(Object value) {
        return value == null ? "" : value.toString();
    }
}