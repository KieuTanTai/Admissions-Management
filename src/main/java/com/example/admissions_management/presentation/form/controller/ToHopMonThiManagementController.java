package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.ToHopMonThiManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtToHopMonThiEntity;
import com.example.admissions_management.presentation.form.model.ToHopMonThiForm;
import com.example.admissions_management.presentation.form.model.ToHopMonThiImportResult;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class ToHopMonThiManagementController {

    private final ToHopMonThiManagementService service;

    public ToHopMonThiManagementController(ToHopMonThiManagementService service) {
        this.service = service;
    }

    public List<XtToHopMonThiEntity> searchToHop(String query, int page) {
        return service.searchToHop(query, page);
    }

    public Optional<XtToHopMonThiEntity> getById(Integer id) {
        return service.getById(id);
    }

    public void openToHopForm(Integer id) {
        JFrame frame = new JFrame(id == null ? "Tạo mới tổ hợp môn" : "Chỉnh sửa tổ hợp môn");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField txtMaToHop = new JTextField(20);
        JTextField txtMon1 = new JTextField(20);
        JTextField txtMon2 = new JTextField(20);
        JTextField txtMon3 = new JTextField(20);
        JTextField txtTenToHop = new JTextField(20);

        ToHopMonThiForm form = new ToHopMonThiForm();

        if (id != null) {
            service.getById(id).ifPresent(entity -> {
                form.setId(entity.getId());
                form.setMaToHop(entity.getMaToHop());
                form.setMon1(entity.getMon1());
                form.setMon2(entity.getMon2());
                form.setMon3(entity.getMon3());
                form.setTenToHop(entity.getTenToHop());

                txtMaToHop.setText(nvl(form.getMaToHop()));
                txtMon1.setText(nvl(form.getMon1()));
                txtMon2.setText(nvl(form.getMon2()));
                txtMon3.setText(nvl(form.getMon3()));
                txtTenToHop.setText(nvl(form.getTenToHop()));
            });
        }

        panel.add(new JLabel("Mã tổ hợp"));
        panel.add(txtMaToHop);

        panel.add(new JLabel("Môn 1"));
        panel.add(txtMon1);

        panel.add(new JLabel("Môn 2"));
        panel.add(txtMon2);

        panel.add(new JLabel("Môn 3"));
        panel.add(txtMon3);

        panel.add(new JLabel("Tên tổ hợp"));
        panel.add(txtTenToHop);

        JButton saveButton = new JButton("Lưu");

        saveButton.addActionListener(e -> {
            try {
                form.setId(id);
                form.setMaToHop(txtMaToHop.getText().trim());
                form.setMon1(txtMon1.getText().trim());
                form.setMon2(txtMon2.getText().trim());
                form.setMon3(txtMon3.getText().trim());
                form.setTenToHop(txtTenToHop.getText().trim());

                service.saveToHop(form);

                JOptionPane.showMessageDialog(frame, "Lưu tổ hợp thành công.");
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

    public void deleteToHop(Integer id) {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc muốn xóa tổ hợp này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteToHop(id);
            JOptionPane.showMessageDialog(null, "Đã xóa tổ hợp.", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteAllToHop() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc muốn xóa TẤT CẢ tổ hợp môn?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteAllToHop();
            JOptionPane.showMessageDialog(null, "Đã xóa tất cả tổ hợp.", "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void importToHop(List<String> files) {
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

            ToHopMonThiImportResult result = service.importExcelFile(path);

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
                    "Import tổ hợp thành công\nThêm mới: " + totalImported + "\nCập nhật: " + totalUpdated,
                    "Import thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private static String nvl(Object value) {
        return value == null ? "" : value.toString();
    }
}