package com.example.admissions_management.presentation.form.view;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiAppController;

public class BangQuyDoiDialog extends JDialog {
    
    // Khai báo đầy đủ các text field theo Entity
    private JTextField txtPhuongThuc, txtToHop, txtMon;
    private JTextField txtDiemA, txtDiemB, txtDiemC, txtDiemD;
    private JTextField txtMaQuyDoi, txtPhanVi;
    private JButton btnLuu, btnHuy;
    
    private BangQuyDoi currentData; 
    private BangQuyDoiAppController service;

    public BangQuyDoiDialog(JFrame parent, BangQuyDoi data) {
        super(parent, data == null ? "Thêm mới Quy tắc" : "Sửa Quy tắc", true);
        this.currentData = data;
        
        // Lấy service từ AdminConsole
        this.service = ((AdminPanel)parent).getBangQuyDoiAppController();

        // Tăng GridLayout lên 10 dòng (9 trường nhập + 1 dòng nút bấm)
        setLayout(new GridLayout(10, 2, 10, 10));
        setSize(450, 450); // Mở rộng chiều cao form
        setLocationRelativeTo(parent);

        // Khởi tạo các ô nhập liệu
        txtPhuongThuc = new JTextField(); addRow("Phương thức:", txtPhuongThuc);
        txtToHop      = new JTextField(); addRow("Tổ hợp:", txtToHop);
        txtMon        = new JTextField(); addRow("Môn:", txtMon);
        txtDiemA      = new JTextField(); addRow("Điểm A:", txtDiemA);
        txtDiemB      = new JTextField(); addRow("Điểm B:", txtDiemB);
        txtDiemC      = new JTextField(); addRow("Điểm C:", txtDiemC);
        txtDiemD      = new JTextField(); addRow("Điểm D:", txtDiemD);
        txtMaQuyDoi   = new JTextField(); addRow("Mã quy đổi (Unique):", txtMaQuyDoi);
        txtPhanVi     = new JTextField(); addRow("Phân vị:", txtPhanVi);

        // Nút bấm
        btnLuu = new JButton("Lưu dữ liệu");
        btnHuy = new JButton("Hủy bỏ");
        
        // Tạo panel chứa 2 nút cho đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnHuy);
        buttonPanel.add(btnLuu);
        add(new JLabel()); // Ô trống để đẩy nút sang cột bên phải
        add(buttonPanel);

        // ----------------------------------------------------
        // NẾU LÀ SỬA (currentData != null) -> ĐỔ DỮ LIỆU LÊN FORM
        // ----------------------------------------------------
        if (currentData != null) {
            txtPhuongThuc.setText(currentData.getPhuongThuc());
            txtToHop.setText(currentData.getToHop());
            txtMon.setText(currentData.getMon());
            txtDiemA.setText(currentData.getDiemA() != null ? currentData.getDiemA().toString() : "");
            txtDiemB.setText(currentData.getDiemB() != null ? currentData.getDiemB().toString() : "");
            txtDiemC.setText(currentData.getDiemC() != null ? currentData.getDiemC().toString() : "");
            txtDiemD.setText(currentData.getDiemD() != null ? currentData.getDiemD().toString() : "");
            txtMaQuyDoi.setText(currentData.getMaQuyDoi());
            txtPhanVi.setText(currentData.getPhanVi());
        }

        // ----------------------------------------------------
        // SỰ KIỆN NÚT LƯU
        // ----------------------------------------------------
        btnLuu.addActionListener(e -> {
            try {
                // Nếu đang thêm mới thì tạo object mới, nếu sửa thì lấy object cũ để giữ nguyên ID
                BangQuyDoi qd = (currentData == null) ? new BangQuyDoi() : currentData;
                
                qd.setPhuongThuc(txtPhuongThuc.getText());
                qd.setToHop(txtToHop.getText());
                qd.setMon(txtMon.getText());
                
                // Dùng hàm an toàn để chuyển String sang BigDecimal
                qd.setDiemA(parseBigDecimal(txtDiemA.getText()));
                qd.setDiemB(parseBigDecimal(txtDiemB.getText()));
                qd.setDiemC(parseBigDecimal(txtDiemC.getText()));
                qd.setDiemD(parseBigDecimal(txtDiemD.getText()));
                
                qd.setMaQuyDoi(txtMaQuyDoi.getText());
                qd.setPhanVi(txtPhanVi.getText());

                // Lưu xuống Database
                service.add(qd); 
                
                JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Đóng cửa sổ sau khi lưu xong
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu! Vui lòng kiểm tra lại định dạng số (Điểm).", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // In lỗi ra console để dev dễ fix
            }
        });

        // Sự kiện nút Hủy
        btnHuy.addActionListener(e -> dispose());
    }

    // Hàm tiện ích: Thêm nhãn và ô nhập liệu vào Form
    private void addRow(String labelText, JTextField textField) {
        JLabel label = new JLabel(" " + labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        add(label);
        add(textField);
    }

    // Hàm tiện ích: Chuyển chuỗi sang BigDecimal an toàn (hỗ trợ trường hợp bỏ trống)
    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null; // Hoặc trả về BigDecimal.ZERO tùy nghiệp vụ của bạn
        }
        return new BigDecimal(text.trim());
    }
}