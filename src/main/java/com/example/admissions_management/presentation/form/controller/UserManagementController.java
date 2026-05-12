package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.dto.request.UserAccountRequest;
import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.application.service.UserAccountService;
import com.example.admissions_management.domain.model.UserRole;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

@Component
public class UserManagementController {

    private final UserAccountService userAccountService;

    public UserManagementController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public List<UserAccountResponse> getAllUsers() {
        return userAccountService.getAllUsers();
    }

    public Optional<UserAccountResponse> getUserById(Long id) {
        return userAccountService.getUserById(id);
    }

    public void showUserList() {
        List<UserAccountResponse> users = getAllUsers();
        String[][] data = new String[users.size()][5];
        for (int i = 0; i < users.size(); i++) {
            UserAccountResponse user = users.get(i);
            data[i][0] = user.getId().toString();
            data[i][1] = user.getUsername();
            data[i][2] = user.getFullName();
            data[i][3] = user.getRole().toString();
            data[i][4] = user.getEnabled() ? "Enabled" : "Disabled";
        }
        String[] columnNames = {"ID", "Username", "Full Name", "Role", "Status"};
        JTable table = new JTable(data, columnNames);
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Danh sách người dùng", JOptionPane.INFORMATION_MESSAGE);
    }

    public void openUserForm(Long id) {
        JFrame frame = new JFrame(id == null ? "Tạo mới người dùng" : "Chỉnh sửa người dùng");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField txtUsername = new JTextField(20);
        JTextField txtFullName = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JComboBox<UserRole> roleComboBox = new JComboBox<>(UserRole.values());
        JCheckBox chkEnabled = new JCheckBox("Enabled");

        if (id != null) {
            userAccountService.getUserById(id).ifPresent(user -> {
                txtUsername.setText(user.getUsername());
                txtFullName.setText(user.getFullName());
                roleComboBox.setSelectedItem(user.getRole());
                chkEnabled.setSelected(user.getEnabled());
            });
        }

        panel.add(new JLabel("Username"));
        panel.add(txtUsername);
        panel.add(new JLabel("Full Name"));
        panel.add(txtFullName);
        panel.add(new JLabel("Password"));
        panel.add(txtPassword);
        panel.add(new JLabel("Role"));
        panel.add(roleComboBox);
        panel.add(chkEnabled);

        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> {
            try {
                UserAccountRequest request = new UserAccountRequest();
                request.setId(id);
                request.setUsername(txtUsername.getText().trim());
                request.setFullName(txtFullName.getText().trim());
                request.setPassword(new String(txtPassword.getPassword()).trim());
                request.setRole((UserRole) roleComboBox.getSelectedItem());
                request.setEnabled(chkEnabled.isSelected());

                userAccountService.save(request);
                JOptionPane.showMessageDialog(frame, "Lưu người dùng thành công.");
                frame.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(saveButton);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void changePassword(Long id) {
        JFrame frame = new JFrame("Thay đổi mật khẩu");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPasswordField txtPassword = new JPasswordField(20);
        panel.add(new JLabel("New Password"));
        panel.add(txtPassword);

        JButton changeButton = new JButton("Đổi mật khẩu");
        changeButton.addActionListener(e -> {
            try {
                String newPassword = new String(txtPassword.getPassword()).trim();
                userAccountService.changePassword(id, newPassword);
                JOptionPane.showMessageDialog(frame, "Đổi mật khẩu thành công.");
                frame.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(changeButton);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void toggleUserRole(Long id) {
        userAccountService.getUserById(id).ifPresentOrElse(user -> {
            UserRole newRole = user.getRole() == UserRole.ADMIN ? UserRole.USER : UserRole.ADMIN;
            userAccountService.setRole(id, newRole);
            JOptionPane.showMessageDialog(null, "Đổi quyền thành công.");
        }, () -> JOptionPane.showMessageDialog(null, "Người dùng không tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE));
    }

    public void setUserEnabled(Long id, boolean enabled) {
        userAccountService.setEnabled(id, enabled);
        JOptionPane.showMessageDialog(null, "Cập nhật trạng thái thành công.");
    }
}
