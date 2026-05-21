package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtToHopMonThiEntity;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiConsoleController;
import com.example.admissions_management.presentation.form.controller.CandidateManagementController;
import com.example.admissions_management.presentation.form.controller.CombinationFormController;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.controller.MajorManagementController;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.controller.ScoreManagementConsoleController;
import com.example.admissions_management.presentation.form.controller.ToHopMonThiManagementController;
import com.example.admissions_management.presentation.form.controller.UserManagementController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.swing.admin-console", name = "enabled", havingValue = "true")
@Lazy
public class AdminConsole extends JFrame {

    private static final Color BG_COLOR = new Color(240, 243, 238);
    private static final Color SURFACE_COLOR = new Color(252, 253, 251);
    private static final Color SOFT_COLOR = new Color(236, 244, 239);
    private static final Color PRIMARY_COLOR = new Color(17, 110, 88);
    private static final Color ACCENT_COLOR = new Color(220, 155, 63);
    private static final Color DANGER_COLOR = new Color(190, 76, 64);
    private static final Color TEXT_COLOR = new Color(23, 45, 38);
    private static final Color MUTED_COLOR = new Color(102, 121, 113);
    private static final Color BORDER_COLOR = new Color(214, 224, 218);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private final DiemCongConsoleController diemCongConsoleController;
    private final NguyenVongConsoleController nguyenVongConsoleController;
    private final ScoreManagementConsoleController scoreManagementConsoleController;
    private final BangQuyDoiConsoleController bangQuyDoiConsoleController;
    private final UserManagementController userController;
    private final CandidateManagementController candidateController;
    private final MajorManagementController majorController;
    private final ToHopMonThiManagementController toHopController;
    private final CombinationFormController combinationFormController;

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

    public AdminConsole(
            DiemCongConsoleController diemCongConsoleController,
            NguyenVongConsoleController nguyenVongConsoleController,
            ScoreManagementConsoleController scoreManagementConsoleController,
            BangQuyDoiConsoleController bangQuyDoiConsoleController,
            UserManagementController userController,
            CandidateManagementController candidateController,
            MajorManagementController majorController,
            ToHopMonThiManagementController toHopController,
            CombinationFormController combinationFormController) {
        this.diemCongConsoleController = diemCongConsoleController;
        this.nguyenVongConsoleController = nguyenVongConsoleController;
        this.scoreManagementConsoleController = scoreManagementConsoleController;
        this.bangQuyDoiConsoleController = bangQuyDoiConsoleController;
        this.userController = userController;
        this.candidateController = candidateController;
        this.majorController = majorController;
        this.toHopController = toHopController;
        this.combinationFormController = combinationFormController;

        setTitle("Hệ thống quản trị tuyển sinh");
        setSize(1480, 860);
        setMinimumSize(new Dimension(1320, 780));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        userSearchField = new JTextField(20);
        candidateSearchField = new JTextField(20);
        candidatePageLabel = createPageLabel();
        majorSearchField = new JTextField(20);
        majorPageLabel = createPageLabel();
        toHopSearchField = new JTextField(20);
        toHopPageLabel = createPageLabel();

        userTableModel = new DefaultTableModel(
                new Object[] { "ID", "Tên đăng nhập", "Họ và tên", "Vai trò", "Trạng thái" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);

        candidateTableModel = new DefaultTableModel(
                new Object[] { "ID", "CCCD", "SBD", "Họ", "Tên", "Ngày sinh", "Điện thoại", "Giới tính", "Email",
                        "Nơi sinh", "Đối tượng", "Khu vực" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        candidateTable = new JTable(candidateTableModel);

        majorTableModel = new DefaultTableModel(
                new Object[] { "ID", "Mã ngành", "Tên ngành", "Tổ hợp gốc", "Chỉ tiêu", "Điểm sàn",
                        "Điểm trúng tuyển", "Tuyển thẳng", "ĐGNL", "THPT", "V-SAT", "SL XTT", "SL ĐGNL",
                        "SL V-SAT", "SL THPT" }, 0) {
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

        styleTable(userTable);
        styleTable(candidateTable);
        styleTable(majorTable);
        styleTable(toHopTable);

        setLayout(new BorderLayout(10, 10));
        add(buildDashboardHeader(), BorderLayout.NORTH);
        add(buildTabPane(), BorderLayout.CENTER);

        loadUsers();
        loadCandidates();
        loadMajors();
        loadToHop();
    }

    private JPanel buildDashboardHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 18, 6, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE_COLOR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 22, 20, 22)));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("ADMISSIONS CONSOLE");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(154, 100, 28));

        JLabel title = new JLabel("Trung tâm điều hành tuyển sinh");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        JLabel subtitle = new JLabel("Theo dõi dữ liệu, kiểm soát nhập liệu và thao tác trực tiếp trên từng phân hệ.");
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(MUTED_COLOR);

        titlePanel.add(badge);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.CENTER);
        wrapper.add(header, BorderLayout.CENTER);
        return wrapper;
    }

        private JTabbedPane buildTabPane() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(UI_FONT_BOLD);
        tabPane.setForeground(TEXT_COLOR);
        tabPane.setBackground(BG_COLOR);
        tabPane.setBorder(new EmptyBorder(10, 18, 18, 18));

        tabPane.addTab("1. Quản lý người dùng", buildUserPanel());
        tabPane.addTab("2. Quản lý thí sinh", buildCandidatePanel());
        tabPane.addTab("3. Quản lý ngành tuyển sinh", buildMajorPanel());
        tabPane.addTab("4. Quản lý tổ hợp môn xét tuyển", buildToHopPanel());
        tabPane.addTab("5. Quản lý danh sách ngành - tổ hợp", new CombinationManagementPanel(combinationFormController));
        tabPane.addTab("6. Quản lý điểm thí sinh", new ScoreManagementPanel(scoreManagementConsoleController));
        tabPane.addTab("7. Quản lý điểm cộng", new DiemCongPanel(diemCongConsoleController));
        tabPane.addTab("8. Quản lý nguyện vọng và xét tuyển", new NguyenVongPanel(nguyenVongConsoleController));
        tabPane.addTab("9. Quản lý bảng quy đổi", new BangQuyDoiManagementPanel(bangQuyDoiConsoleController));
        return tabPane;
    }

    private JPanel buildUserPanel() {
        JPanel panel = createSectionPanel();

        JPanel topPanel = createToolbarPanel();
        topPanel.setLayout(new GridLayout(1, 4, 10, 10));
        topPanel.add(createToolbarLabel("Tìm kiếm người dùng"));
        styleTextField(userSearchField);
        topPanel.add(userSearchField);
        topPanel.add(createButton("Tìm", ButtonStyle.PRIMARY, this::loadUsers));
        topPanel.add(createButton("Làm mới", ButtonStyle.SECONDARY, () -> {
            userSearchField.setText("");
            loadUsers();
        }));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTableScrollPane(userTable), BorderLayout.CENTER);

        JPanel actionPanel = createToolbarPanel();
        actionPanel.setLayout(new GridLayout(1, 5, 10, 10));
        actionPanel.add(createButton("Thêm", ButtonStyle.PRIMARY, () -> {
            userController.openUserForm(null);
            loadUsers();
        }));
        actionPanel.add(createButton("Sửa", ButtonStyle.SECONDARY, () -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.openUserForm(id);
                loadUsers();
            }
        }));
        actionPanel.add(createButton("Đổi mật khẩu", ButtonStyle.SECONDARY, () -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.changePassword(id);
            }
        }));
        actionPanel.add(createButton("Đổi quyền", ButtonStyle.SECONDARY, () -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.toggleUserRole(id);
                loadUsers();
            }
        }));
        actionPanel.add(createButton("Bật/Tắt", ButtonStyle.DANGER, () -> {
            Long id = getSelectedUserId();
            if (id != null) {
                userController.setUserEnabled(id, !isSelectedUserEnabled());
                loadUsers();
            }
        }));

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCandidatePanel() {
        JPanel panel = createSectionPanel();

        JPanel topPanel = createToolbarPanel();
        topPanel.setLayout(new GridLayout(1, 6, 10, 10));
        topPanel.add(createToolbarLabel("Tìm kiếm CCCD / họ tên"));
        styleTextField(candidateSearchField);
        topPanel.add(candidateSearchField);
        topPanel.add(createButton("Tìm", ButtonStyle.PRIMARY, () -> {
            candidatePage = 0;
            candidateQuery = candidateSearchField.getText().trim();
            loadCandidates();
        }));
        topPanel.add(createButton("Nhập Excel", ButtonStyle.ACCENT, this::importCandidates));
        topPanel.add(createButton("Làm mới", ButtonStyle.SECONDARY, () -> {
            candidateSearchField.setText("");
            candidateQuery = "";
            candidatePage = 0;
            loadCandidates();
        }));
        topPanel.add(candidatePageLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTableScrollPane(candidateTable), BorderLayout.CENTER);

        JPanel buttonPanel = createToolbarPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 10));
        buttonPanel.add(createButton("Thêm", ButtonStyle.PRIMARY, () -> {
            candidateController.openCandidateForm(null);
            loadCandidates();
        }));
        buttonPanel.add(createButton("Sửa", ButtonStyle.SECONDARY, () -> {
            Integer id = getSelectedCandidateId();
            if (id != null) {
                candidateController.openCandidateForm(id);
                loadCandidates();
            }
        }));
        buttonPanel.add(createButton("Xóa", ButtonStyle.DANGER, () -> {
            Integer id = getSelectedCandidateId();
            if (id != null) {
                candidateController.deleteCandidate(id);
                loadCandidates();
            }
        }));
        buttonPanel.add(createButton("Xóa tất cả", ButtonStyle.DANGER, () -> {
            candidateController.deleteAllCandidates();
            loadCandidates();
        }));
        buttonPanel.add(createButton("<< Trước", ButtonStyle.SECONDARY, () -> {
            if (candidatePage > 0) {
                candidatePage--;
                loadCandidates();
            }
        }));
        buttonPanel.add(createButton("Sau >>", ButtonStyle.SECONDARY, () -> {
            candidatePage++;
            loadCandidates();
        }));

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMajorPanel() {
        JPanel panel = createSectionPanel();

        JPanel topPanel = createToolbarPanel();
        topPanel.setLayout(new GridLayout(1, 6, 10, 10));
        topPanel.add(createToolbarLabel("Tìm kiếm mã ngành / tên ngành"));
        styleTextField(majorSearchField);
        topPanel.add(majorSearchField);
        topPanel.add(createButton("Tìm", ButtonStyle.PRIMARY, () -> {
            majorPage = 0;
            majorQuery = majorSearchField.getText().trim();
            loadMajors();
        }));
        topPanel.add(createButton("Nhập Excel", ButtonStyle.ACCENT, this::importMajors));
        topPanel.add(createButton("Làm mới", ButtonStyle.SECONDARY, () -> {
            majorSearchField.setText("");
            majorQuery = "";
            majorPage = 0;
            loadMajors();
        }));
        topPanel.add(majorPageLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTableScrollPane(majorTable), BorderLayout.CENTER);

        JPanel buttonPanel = createToolbarPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 10));
        buttonPanel.add(createButton("Thêm", ButtonStyle.PRIMARY, () -> {
            majorController.openMajorForm(null);
            loadMajors();
        }));
        buttonPanel.add(createButton("Sửa", ButtonStyle.SECONDARY, () -> {
            Integer id = getSelectedMajorId();
            if (id != null) {
                majorController.openMajorForm(id);
                loadMajors();
            }
        }));
        buttonPanel.add(createButton("Xóa", ButtonStyle.DANGER, () -> {
            Integer id = getSelectedMajorId();
            if (id != null) {
                majorController.deleteMajor(id);
                loadMajors();
            }
        }));
        buttonPanel.add(createButton("Xóa tất cả", ButtonStyle.DANGER, () -> {
            majorController.deleteAllMajors();
            loadMajors();
        }));
        buttonPanel.add(createButton("<< Trước", ButtonStyle.SECONDARY, () -> {
            if (majorPage > 0) {
                majorPage--;
                loadMajors();
            }
        }));
        buttonPanel.add(createButton("Sau >>", ButtonStyle.SECONDARY, () -> {
            majorPage++;
            loadMajors();
        }));

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildToHopPanel() {
        JPanel panel = createSectionPanel();

        JPanel topPanel = createToolbarPanel();
        topPanel.setLayout(new GridLayout(1, 6, 10, 10));
        topPanel.add(createToolbarLabel("Tìm kiếm mã / tên tổ hợp"));
        styleTextField(toHopSearchField);
        topPanel.add(toHopSearchField);
        topPanel.add(createButton("Tìm", ButtonStyle.PRIMARY, () -> {
            toHopPage = 0;
            toHopQuery = toHopSearchField.getText().trim();
            loadToHop();
        }));
        topPanel.add(createButton("Nhập Excel", ButtonStyle.ACCENT, this::importToHop));
        topPanel.add(createButton("Làm mới", ButtonStyle.SECONDARY, () -> {
            toHopSearchField.setText("");
            toHopQuery = "";
            toHopPage = 0;
            loadToHop();
        }));
        topPanel.add(toHopPageLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createTableScrollPane(toHopTable), BorderLayout.CENTER);

        JPanel buttonPanel = createToolbarPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 10));
        buttonPanel.add(createButton("Thêm", ButtonStyle.PRIMARY, () -> {
            toHopController.openToHopForm(null);
            loadToHop();
        }));
        buttonPanel.add(createButton("Sửa", ButtonStyle.SECONDARY, () -> {
            Integer id = getSelectedToHopId();
            if (id != null) {
                toHopController.openToHopForm(id);
                loadToHop();
            }
        }));
        buttonPanel.add(createButton("Xóa", ButtonStyle.DANGER, () -> {
            Integer id = getSelectedToHopId();
            if (id != null) {
                toHopController.deleteToHop(id);
                loadToHop();
            }
        }));
        buttonPanel.add(createButton("Xóa tất cả", ButtonStyle.DANGER, () -> {
            toHopController.deleteAllToHop();
            loadToHop();
        }));
        buttonPanel.add(createButton("<< Trước", ButtonStyle.SECONDARY, () -> {
            if (toHopPage > 0) {
                toHopPage--;
                loadToHop();
            }
        }));
        buttonPanel.add(createButton("Sau >>", ButtonStyle.SECONDARY, () -> {
            toHopPage++;
            loadToHop();
        }));

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
                        Boolean.TRUE.equals(user.getEnabled()) ? "Đang hoạt động" : "Đã khóa"
                });
            }
        }
    }

    private void loadCandidates() {
        candidateTableModel.setRowCount(0);
        List<XtThiSinhXetTuyen25Entity> candidates = candidateController.searchCandidates(candidateQuery, candidatePage);

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

        candidatePageLabel.setText("Trang " + (candidatePage + 1));
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

        majorPageLabel.setText("Trang " + (majorPage + 1));
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

        toHopPageLabel.setText("Trang " + (toHopPage + 1));
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
        return "Đang hoạt động".equals(status);
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
        chooser.setDialogTitle("Chọn file CSV hoặc Excel để nhập thí sinh");
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
        chooser.setDialogTitle("Chọn file Excel để nhập ngành tuyển sinh");
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
        chooser.setDialogTitle("Chọn file Excel để nhập tổ hợp môn");
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

    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        return panel;
    }

    private JLabel createToolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT_BOLD);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createPageLabel() {
        JLabel label = new JLabel("Trang 1", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(SOFT_COLOR);
        label.setForeground(TEXT_COLOR);
        label.setFont(UI_FONT_BOLD);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(9, 12, 9, 12)));
        return label;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder()));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JButton createButton(String text, ButtonStyle style, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 18, 12, 18)));
        button.setPreferredSize(new Dimension(156, 46));
        button.setMinimumSize(new Dimension(156, 46));
        button.setOpaque(true);

        if (style == ButtonStyle.PRIMARY) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else if (style == ButtonStyle.ACCENT) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(TEXT_COLOR);
        } else if (style == ButtonStyle.DANGER) {
            button.setBackground(DANGER_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(248, 250, 247));
            button.setForeground(TEXT_COLOR);
        }

        button.addActionListener(e -> action.run());
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(UI_FONT);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(9, 11, 9, 11)));
    }

    private void styleTable(JTable table) {
        table.setFont(UI_FONT);
        table.setRowHeight(32);
        table.setGridColor(new Color(233, 239, 235));
        table.setSelectionBackground(new Color(221, 237, 231));
        table.setSelectionForeground(TEXT_COLOR);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(UI_FONT_BOLD);
        table.getTableHeader().setBackground(new Color(242, 246, 243));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        ACCENT,
        DANGER
    }
}

