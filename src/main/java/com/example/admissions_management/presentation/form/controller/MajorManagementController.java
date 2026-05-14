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

        if (majorId != null) {
            majorManagementService.getMajorById(majorId).ifPresent(entity -> {
                form.setIdnganh(entity.getId());
                form.setManganh(entity.getMaNganh());
                form.setTennganh(entity.getTenNganh());
                form.setnTohopgoc(entity.getToHopGoc());
                form.setnChitieu(entity.getChiTieu());
                form.setnDiemsan(entity.getDiemSan());
                form.setnDiemtrungtuyen(entity.getDiemTrungTuyen());
                form.setnTuyenthang(entity.getTuyenThang());
                form.setnDgnl(entity.getDgnl());
                form.setnThpt(entity.getThpt());
                form.setnVsat(entity.getVsat());
                form.setSlXtt(entity.getSlXtt());
                form.setSlDgnl(entity.getSlDgnl());
                form.setSlVsat(entity.getSlVsat());
                form.setSlThpt(entity.getSlThpt());

                txtMaNganh.setText(nvl(form.getManganh()));
                txtTenNganh.setText(nvl(form.getTennganh()));
                txtToHopGoc.setText(nvl(form.getnTohopgoc()));
                txtChiTieu.setText(nvl(form.getnChitieu()));
                txtDiemSan.setText(nvl(form.getnDiemsan()));
                txtDiemTrungTuyen.setText(nvl(form.getnDiemtrungtuyen()));
                txtTuyenThang.setText(nvl(form.getnTuyenthang()));
                txtDgnl.setText(nvl(form.getnDgnl()));
                txtThpt.setText(nvl(form.getnThpt()));
                txtVsat.setText(nvl(form.getnVsat()));
                txtSlXtt.setText(nvl(form.getSlXtt()));
                txtSlDgnl.setText(nvl(form.getSlDgnl()));
                txtSlVsat.setText(nvl(form.getSlVsat()));
                txtSlThpt.setText(nvl(form.getSlThpt()));
            });
        }

        panel.add(new JLabel("Mã ngành"));
        panel.add(txtMaNganh);

        panel.add(new JLabel("Tên ngành"));
        panel.add(txtTenNganh);

        panel.add(new JLabel("Tổ hợp gốc"));
        panel.add(txtToHopGoc);

        panel.add(new JLabel("Chỉ tiêu"));
        panel.add(txtChiTieu);

        panel.add(new JLabel("Điểm sàn"));
        panel.add(txtDiemSan);

        panel.add(new JLabel("Điểm trúng tuyển"));
        panel.add(txtDiemTrungTuyen);

        panel.add(new JLabel("Tuyển thẳng"));
        panel.add(txtTuyenThang);

        panel.add(new JLabel("ĐGNL"));
        panel.add(txtDgnl);

        panel.add(new JLabel("THPT"));
        panel.add(txtThpt);

        panel.add(new JLabel("V-SAT"));
        panel.add(txtVsat);

        panel.add(new JLabel("SL XTT"));
        panel.add(txtSlXtt);

        panel.add(new JLabel("SL ĐGNL"));
        panel.add(txtSlDgnl);

        panel.add(new JLabel("SL V-SAT"));
        panel.add(txtSlVsat);

        panel.add(new JLabel("SL THPT"));
        panel.add(txtSlThpt);

        JButton saveButton = new JButton("Lưu");

        saveButton.addActionListener(e -> {
            try {
                form.setIdnganh(majorId);
                form.setManganh(txtMaNganh.getText().trim());
                form.setTennganh(txtTenNganh.getText().trim());
                form.setnTohopgoc(txtToHopGoc.getText().trim());
                form.setnChitieu(toInteger(txtChiTieu.getText()));
                form.setnDiemsan(toBigDecimal(txtDiemSan.getText()));
                form.setnDiemtrungtuyen(toBigDecimal(txtDiemTrungTuyen.getText()));
                form.setnTuyenthang(txtTuyenThang.getText().trim());
                form.setnDgnl(txtDgnl.getText().trim());
                form.setnThpt(txtThpt.getText().trim());
                form.setnVsat(txtVsat.getText().trim());
                form.setSlXtt(toInteger(txtSlXtt.getText()));
                form.setSlDgnl(toInteger(txtSlDgnl.getText()));
                form.setSlVsat(toInteger(txtSlVsat.getText()));
                form.setSlThpt(txtSlThpt.getText().trim());

                majorManagementService.saveMajor(form);

                JOptionPane.showMessageDialog(frame, "Lưu ngành thành công.");
                frame.dispose();
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
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc muốn xóa ngành này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            majorManagementService.deleteMajor(majorId);
            JOptionPane.showMessageDialog(null, "Đã xóa ngành.", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteAllMajors() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc muốn xóa TẤT CẢ dữ liệu ngành tuyển sinh?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

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
            JOptionPane.showMessageDialog(
                    null,
                    "Import có lỗi:\n" + errors,
                    "Import lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Import ngành thành công\nThêm mới: " + totalImported + "\nCập nhật: " + totalUpdated,
                    "Import thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
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