package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.presentation.form.controller.AdminConsoleController;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.view.combination.CombinationForm;
import com.example.admissions_management.presentation.form.model.AdminConsoleTableModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtToHopMonThiEntity;
import com.example.admissions_management.presentation.form.controller.CandidateManagementController;
import com.example.admissions_management.presentation.form.controller.MajorManagementController;
import com.example.admissions_management.presentation.form.controller.ToHopMonThiManagementController;
import com.example.admissions_management.presentation.form.controller.UserManagementController;
import org.springframework.stereotype.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.swing.admin-console", name = "enabled", havingValue = "true")
@Lazy
public class AdminConsole extends JFrame {

    private final AdminConsoleController controller;
    private final ObjectProvider<CombinationForm> combinationFormProvider;
    private final AdminConsoleTableModel tableModel;
    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField programField = new JTextField();
    private final DiemCongConsoleController diemCongConsoleController;
    private final NguyenVongConsoleController nguyenVongConsoleController;
    private final UserManagementController userController;
    private final CandidateManagementController candidateController;
    private final MajorManagementController majorController;
    private final ToHopMonThiManagementController toHopController;

    private final DefaultTableModel userTableModel;
    private final JTable userTable;
    private final JTextField userSearchField;

    private final DefaultTableModel candidateTableModel;
    private final JTable candidateTable;
    private final JTextField candidateSearchField;
    private final JLabel candidatePageLabel;
    private int candidatePage = 0;
    private String candidateQuery = "";

    private final DefaultTableModel majorTableModel;
    private final JTable majorTable;
    private final JTextField majorSearchField;
    private final JLabel majorPageLabel;
    private int majorPage = 0;
    private String majorQuery = "";

    private final DefaultTableModel toHopTableModel;
    private final JTable toHopTable;
    private final JTextField toHopSearchField;
    private final JLabel toHopPageLabel;
    private int toHopPage = 0;
    private String toHopQuery = "";

    public AdminConsole(AdminConsoleController controller, ObjectProvider<CombinationForm> combinationFormProvider,
            DiemCongConsoleController diemCongConsoleController,
            NguyenVongConsoleController nguyenVongConsoleController,
            UserManagementController userController,
            CandidateManagementController candidateController,
            MajorManagementController majorController,
            ToHopMonThiManagementController toHopController) {
        this.controller = controller;
        this.combinationFormProvider = combinationFormProvider;
        this.diemCongConsoleController = diemCongConsoleController;
        this.nguyenVongConsoleController = nguyenVongConsoleController;
        this.userController = userController;
        this.tableModel = new AdminConsoleTableModel();
        this.candidateController = candidateController;
        this.majorController = majorController;
        this.toHopController = toHopController;

        setTitle("Bảng Quản Trị Tuyển Sinh");
        setSize(1280, 980);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(false); // Not visible by default

        setTitle("Bảng Quản Trị Tuyển Sinh");
        setSize(1400, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userSearchField = new JTextField(20);
        candidateSearchField = new JTextField(20);
        candidatePageLabel = new JLabel("Page: 1");

        JPanel actionPanel = new JPanel(new GridLayout(1, 5, 8, 8));
        JButton saveButton = new JButton("Lưu");
        JButton refreshButton = new JButton("Làm mới");
        JButton combinationsButton = new JButton("Tổ hợp");
        JButton diemCongButton = new JButton("Điểm Cộng");
        JButton nguyenVongButton = new JButton("Nguyện Vọng");

        saveButton.addActionListener(e -> saveApplicant());
        refreshButton.addActionListener(e -> refreshTable());
        combinationsButton.addActionListener(e -> openCombinationManager());
        diemCongButton.addActionListener(e -> openDiemCongForm());
        nguyenVongButton.addActionListener(e -> openNguyenVongForm());

        actionPanel.add(saveButton);
        actionPanel.add(refreshButton);
        actionPanel.add(combinationsButton);
        actionPanel.add(diemCongButton);
        actionPanel.add(nguyenVongButton);

        majorSearchField = new JTextField(20);
        majorPageLabel = new JLabel("Page: 1");

        toHopSearchField = new JTextField(20);
        toHopPageLabel = new JLabel("Page: 1");

        userTableModel = new DefaultTableModel(
                new Object[] { "ID", "Username", "Full Name", "Role", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);

        candidateTableModel = new DefaultTableModel(
                new Object[] { "ID", "CCCD", "SBD", "Họ", "Tên", "Ngày Sinh", "Điện Thoại", "Giới Tính", "Email",
                        "Nơi Sinh", "Đối Tượng", "Khu Vực" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        candidateTable = new JTable(candidateTableModel);

        majorTableModel = new DefaultTableModel(
                new Object[] {
                        "ID",
                        "Mã ngành",
                        "Tên ngành",
                        "Tổ hợp gốc",
                        "Chỉ tiêu",
                        "Điểm sàn",
                        "Điểm trúng tuyển",
                        "Tuyển thẳng",
                        "ĐGNL",
                        "THPT",
                        "V-SAT",
                        "SL XTT",
                        "SL ĐGNL",
                        "SL V-SAT",
                        "SL THPT"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        majorTable = new JTable(majorTableModel);

        toHopTableModel = new DefaultTableModel(
                new Object[] { "ID", "Mã tổ hợp", "Môn 1", "Môn 2", "Môn 3", "Tên tổ hợp" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        toHopTable = new JTable(toHopTableModel);

        setLayout(new BorderLayout(10, 10));
        add(buildTabPane(), BorderLayout.CENTER);

        loadUsers();
        loadCandidates();
        loadMajors();
        loadToHop();
    }

    private void saveApplicant() {
        try {
            controller.registerApplicant(
                    fullNameField.getText().trim(),
                    emailField.getText().trim(),
                    programField.getText().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        tableModel.setRows(controller.loadApplicants());
    }

    private void openCombinationManager() {
        CombinationForm form = combinationFormProvider.getObject();
        form.setVisible(true);
        this.setEnabled(false);
        form.setParentFrame(this);
        form.setTrackingEnableForParentFrame();
        form.toFront();

    }

    private void openDiemCongForm() {
        DiemCongPanel diemCongPanel = new DiemCongPanel(diemCongConsoleController);
        JFrame diemCongFrame = new JFrame("Phần 7 - Điểm Cộng");
        diemCongFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        diemCongFrame.add(diemCongPanel);
        diemCongFrame.setSize(1280, 760);
        diemCongFrame.setLocationRelativeTo(this);
        diemCongFrame.setVisible(true);
    }

    private void openNguyenVongForm() {
        NguyenVongPanel nguyenVongPanel = new NguyenVongPanel(nguyenVongConsoleController);
        JFrame nguyenVongFrame = new JFrame("Phần 8 - Nguyện Vọng");
        nguyenVongFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nguyenVongFrame.add(nguyenVongPanel);
        nguyenVongFrame.setSize(1280, 760);
        nguyenVongFrame.setLocationRelativeTo(this);
        nguyenVongFrame.setVisible(true);
    }

    private JTabbedPane buildTabPane() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Quản lý người dùng", buildUserPanel());
        tabPane.addTab("Quản lý thí sinh", buildCandidatePanel());
        tabPane.addTab("Quản lý ngành tuyển sinh", buildMajorPanel());
        tabPane.addTab("Quản lý tổ hợp môn", buildToHopPanel());
        tabPane.addTab("Phần 7 - Điểm cộng", new DiemCongPanel(diemCongConsoleController));
        tabPane.addTab("Phần 8 - Nguyện vọng", new NguyenVongPanel(nguyenVongConsoleController));
        return tabPane;
    }

    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridLayout(1, 4, 8, 8));
        topPanel.add(new JLabel("Tìm kiếm User"));
        topPanel.add(userSearchField);

        JButton searchButton = new JButton("Tìm");
        JButton refreshButton = new JButton("Làm mới");

        searchButton.addActionListener(e -> loadUsers());
        refreshButton.addActionListener(e -> {
            userSearchField.setText("");
            loadUsers();
        });

        topPanel.add(searchButton);
        topPanel.add(refreshButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 5, 8, 8));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton passwordButton = new JButton("Đổi mật khẩu");
        JButton roleButton = new JButton("Đổi quyền");
        JButton enableButton = new JButton("Bật/Tắt");

        addButton.addActionListener(e -> {
            userController.openUserForm(null);
            loadUsers();
        });

        editButton.addActionListener(e -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.openUserForm(id);
                loadUsers();
            }
        });

        passwordButton.addActionListener(e -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.changePassword(id);
            }
        });

        roleButton.addActionListener(e -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.toggleUserRole(id);
                loadUsers();
            }
        });

        enableButton.addActionListener(e -> {
            Long id = getSelectedUserId();
            if (id != null) {
                boolean enabled = !isSelectedUserEnabled();
                userController.setUserEnabled(id, enabled);
                loadUsers();
            }
        });

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(passwordButton);
        actionPanel.add(roleButton);
        actionPanel.add(enableButton);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCandidatePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        topPanel.add(new JLabel("Tìm kiếm CCCD/Họ tên"));
        topPanel.add(candidateSearchField);

        JButton searchButton = new JButton("Tìm");
        JButton importButton = new JButton("Nhập CSV/Excel");
        JButton refreshButton = new JButton("Làm mới");

        topPanel.add(searchButton);
        topPanel.add(importButton);
        topPanel.add(refreshButton);
        topPanel.add(candidatePageLabel);

        searchButton.addActionListener(e -> {
            candidatePage = 0;
            candidateQuery = candidateSearchField.getText().trim();
            loadCandidates();
        });

        refreshButton.addActionListener(e -> {
            candidateSearchField.setText("");
            candidateQuery = "";
            candidatePage = 0;
            loadCandidates();
        });

        importButton.addActionListener(e -> importCandidates());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(candidateTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 8, 8));

        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton deleteAllButton = new JButton("Xóa tất cả");
        JButton prevButton = new JButton("<< Trước");
        JButton nextButton = new JButton("Sau >>");

        editButton.addActionListener(e -> {
            Integer id = getSelectedCandidateId();
            if (id != null) {
                candidateController.openCandidateForm(id);
                loadCandidates();
            }
        });

        deleteButton.addActionListener(e -> {
            Integer id = getSelectedCandidateId();
            if (id != null) {
                candidateController.deleteCandidate(id);
                loadCandidates();
            }
        });

        deleteAllButton.addActionListener(e -> {
            candidateController.deleteAllCandidates();
            loadCandidates();
        });

        prevButton.addActionListener(e -> {
            if (candidatePage > 0) {
                candidatePage--;
                loadCandidates();
            }
        });

        nextButton.addActionListener(e -> {
            candidatePage++;
            loadCandidates();
        });

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMajorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        topPanel.add(new JLabel("Tìm kiếm mã ngành/tên ngành"));
        topPanel.add(majorSearchField);

        JButton searchButton = new JButton("Tìm");
        JButton importButton = new JButton("Import Excel");
        JButton refreshButton = new JButton("Refresh");

        topPanel.add(searchButton);
        topPanel.add(importButton);
        topPanel.add(refreshButton);
        topPanel.add(majorPageLabel);

        searchButton.addActionListener(e -> {
            majorPage = 0;
            majorQuery = majorSearchField.getText().trim();
            loadMajors();
        });

        refreshButton.addActionListener(e -> {
            majorSearchField.setText("");
            majorQuery = "";
            majorPage = 0;
            loadMajors();
        });

        importButton.addActionListener(e -> importMajors());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(majorTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 8, 8));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton deleteAllButton = new JButton("Xóa tất cả");
        JButton prevButton = new JButton("<< Trước");
        JButton nextButton = new JButton("Sau >>");

        addButton.addActionListener(e -> {
            majorController.openMajorForm(null);
            loadMajors();
        });

        editButton.addActionListener(e -> {
            Integer id = getSelectedMajorId();
            if (id != null) {
                majorController.openMajorForm(id);
                loadMajors();
            }
        });

        deleteButton.addActionListener(e -> {
            Integer id = getSelectedMajorId();
            if (id != null) {
                majorController.deleteMajor(id);
                loadMajors();
            }
        });

        deleteAllButton.addActionListener(e -> {
            majorController.deleteAllMajors();
            loadMajors();
        });

        prevButton.addActionListener(e -> {
            if (majorPage > 0) {
                majorPage--;
                loadMajors();
            }
        });

        nextButton.addActionListener(e -> {
            majorPage++;
            loadMajors();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildToHopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        topPanel.add(new JLabel("Tìm kiếm mã/tên tổ hợp"));
        topPanel.add(toHopSearchField);

        JButton searchButton = new JButton("Tìm");
        JButton importButton = new JButton("Import Excel");
        JButton refreshButton = new JButton("Refresh");

        topPanel.add(searchButton);
        topPanel.add(importButton);
        topPanel.add(refreshButton);
        topPanel.add(toHopPageLabel);

        searchButton.addActionListener(e -> {
            toHopPage = 0;
            toHopQuery = toHopSearchField.getText().trim();
            loadToHop();
        });

        refreshButton.addActionListener(e -> {
            toHopSearchField.setText("");
            toHopQuery = "";
            toHopPage = 0;
            loadToHop();
        });

        importButton.addActionListener(e -> importToHop());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(toHopTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 8, 8));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton deleteAllButton = new JButton("Xóa tất cả");
        JButton prevButton = new JButton("<< Trước");
        JButton nextButton = new JButton("Sau >>");

        addButton.addActionListener(e -> {
            toHopController.openToHopForm(null);
            loadToHop();
        });

        editButton.addActionListener(e -> {
            Integer id = getSelectedToHopId();
            if (id != null) {
                toHopController.openToHopForm(id);
                loadToHop();
            }
        });

        deleteButton.addActionListener(e -> {
            Integer id = getSelectedToHopId();
            if (id != null) {
                toHopController.deleteToHop(id);
                loadToHop();
            }
        });

        deleteAllButton.addActionListener(e -> {
            toHopController.deleteAllToHop();
            loadToHop();
        });

        prevButton.addActionListener(e -> {
            if (toHopPage > 0) {
                toHopPage--;
                loadToHop();
            }
        });

        nextButton.addActionListener(e -> {
            toHopPage++;
            loadToHop();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadUsers() {
        userTableModel.setRowCount(0);

        List<UserAccountResponse> users = userController.getAllUsers();
        String query = userSearchField.getText().trim().toLowerCase();

        for (UserAccountResponse user : users) {
            if (query.isEmpty()
                    || user.getUsername().toLowerCase().contains(query)
                    || (user.getFullName() != null && user.getFullName().toLowerCase().contains(query))) {
                userTableModel.addRow(new Object[] {
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole(),
                        user.getEnabled() ? "Enabled" : "Disabled"
                });
            }
        }
    }

    private void loadCandidates() {
        candidateTableModel.setRowCount(0);

        List<XtThiSinhXetTuyen25Entity> candidates = candidateController.searchCandidates(candidateQuery,
                candidatePage);

        for (XtThiSinhXetTuyen25Entity entity : candidates) {
            candidateTableModel.addRow(new Object[] {
                    entity.getId(),
                    entity.getCccd(),
                    entity.getSoBaoDanh(),
                    entity.getHo(),
                    entity.getTen(),
                    entity.getNgaySinh(),
                    entity.getDienThoai(),
                    entity.getGioiTinh(),
                    entity.getEmail(),
                    entity.getNoiSinh(),
                    entity.getDoiTuong(),
                    entity.getKhuVuc()
            });
        }

        candidatePageLabel.setText("Page: " + (candidatePage + 1));
    }

    private void loadMajors() {
        majorTableModel.setRowCount(0);

        List<XtNganhEntity> majors = majorController.searchMajors(majorQuery, majorPage);

        for (XtNganhEntity entity : majors) {
            majorTableModel.addRow(new Object[] {
                    entity.getId(),
                    entity.getMaNganh(),
                    entity.getTenNganh(),
                    entity.getToHopGoc(),
                    entity.getChiTieu(),
                    entity.getDiemSan(),
                    entity.getDiemTrungTuyen(),
                    entity.getTuyenThang(),
                    entity.getDgnl(),
                    entity.getThpt(),
                    entity.getVsat(),
                    entity.getSlXtt(),
                    entity.getSlDgnl(),
                    entity.getSlVsat(),
                    entity.getSlThpt()
            });
        }

        majorPageLabel.setText("Page: " + (majorPage + 1));
    }

    private void loadToHop() {
        toHopTableModel.setRowCount(0);

        List<XtToHopMonThiEntity> list = toHopController.searchToHop(toHopQuery, toHopPage);

        for (XtToHopMonThiEntity entity : list) {
            toHopTableModel.addRow(new Object[] {
                    entity.getId(),
                    entity.getMaToHop(),
                    entity.getMon1(),
                    entity.getMon2(),
                    entity.getMon3(),
                    entity.getTenToHop()
            });
        }

        toHopPageLabel.setText("Page: " + (toHopPage + 1));
    }

    private Long getSelectedUserId() {
        int row = userTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Object idValue = userTable.getValueAt(row, 0);
        return idValue instanceof Number ? ((Number) idValue).longValue() : Long.parseLong(idValue.toString());
    }

    private boolean isSelectedUserEnabled() {
        int row = userTable.getSelectedRow();

        if (row < 0) {
            return true;
        }

        Object status = userTable.getValueAt(row, 4);
        return "Enabled".equals(status);
    }

    private Integer getSelectedCandidateId() {
        int row = candidateTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Object idValue = candidateTable.getValueAt(row, 0);
        return idValue instanceof Number ? ((Number) idValue).intValue() : Integer.parseInt(idValue.toString());
    }

    private Integer getSelectedMajorId() {
        int row = majorTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Object idValue = majorTable.getValueAt(row, 0);
        return idValue instanceof Number ? ((Number) idValue).intValue() : Integer.parseInt(idValue.toString());
    }

    private Integer getSelectedToHopId() {
        int row = toHopTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tổ hợp môn.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Object idValue = toHopTable.getValueAt(row, 0);
        return idValue instanceof Number ? ((Number) idValue).intValue() : Integer.parseInt(idValue.toString());
    }

    private void importCandidates() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Chọn file CSV hoặc Excel import thí sinh");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("CSV, XLS, XLSX", "csv", "xls", "xlsx"));

        int choice = chooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            List<String> files = Arrays.stream(chooser.getSelectedFiles())
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            candidateController.importCandidates(files);
            loadCandidates();
        }
    }

    private void importMajors() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Chọn file Excel import ngành tuyển sinh");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("XLS, XLSX", "xls", "xlsx"));

        int choice = chooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            List<String> files = Arrays.stream(chooser.getSelectedFiles())
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            majorController.importMajors(files);
            loadMajors();
        }
    }

    private void importToHop() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Chọn file Excel import tổ hợp môn");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("XLS, XLSX", "xls", "xlsx"));

        int choice = chooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            List<String> files = Arrays.stream(chooser.getSelectedFiles())
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            toHopController.importToHop(files);
            loadToHop();
        }
    }
}
