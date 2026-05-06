package com.example.admissions_management.presentation.form.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiAppController;

@Component
public class AdminPanel extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    // 1. Khai báo Service
    private final BangQuyDoiAppController bangQuyDoiAppController;

    // 2. Inject Service thông qua Constructor
    public AdminPanel(BangQuyDoiAppController bangQuyDoiAppController) {
        this.bangQuyDoiAppController = bangQuyDoiAppController;

        setTitle("Hệ thống Quản lý Tuyển sinh - Admin");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createHomePanel(), "TRANG_CHU");
        mainContentPanel.add(createBangQuyDoiPanel(), "BANG_QUY_DOI");

        add(mainContentPanel, BorderLayout.CENTER);
        add(createLeftNavBar(), BorderLayout.WEST);

        // 3. Tải dữ liệu lên bảng ngay khi mở ứng dụng
        loadDataToTable();
    }

    // Hàm đổ dữ liệu từ Service vào JTable
    private void loadDataToTable() {
        List<BangQuyDoi> danhSach = bangQuyDoiAppController.getAll();
        fillTable(danhSach);
    }

    // Tìm hàm fillTable trong AdminConsole.java và sửa lại như sau:
    private void fillTable(List<BangQuyDoi> danhSach) {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        for (BangQuyDoi qd : danhSach) {
            tableModel.addRow(new Object[]{
                qd.getId(),
                qd.getPhuongThuc(),
                qd.getToHop(),
                qd.getMon(),
                qd.getDiemA(), // d_diema
                qd.getDiemB(), // d_diemb
                qd.getDiemC(), // d_diemc
                qd.getDiemD(), // d_diemd
                qd.getMaQuyDoi(), // d_maquydoi
                qd.getPhanVi() // d_phanvi
            });
        }
    }

// Trong hàm createBangQuyDoiPanel, cập nhật tiêu đề cột:
    private JPanel createBangQuyDoiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Quản lý Bảng Quy Đổi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        toolbar.add(txtSearch);

        // Cấu hình Bảng
        String[] columnNames = {"ID", "Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã quy đổi", "Phân vị"};
        tableModel = new DefaultTableModel(columnNames, 0); // Khởi tạo model rỗng
        table = new JTable(tableModel);

        JButton btnSearch = new JButton("Tìm kiếm");
        // Bắt sự kiện nút Tìm kiếm
        btnSearch.addActionListener(e -> {
            // Lấy chữ từ ô tìm kiếm và xóa khoảng trắng thừa ở 2 đầu
            String searchText = txtSearch.getText().trim();

            // Nếu người dùng để trống rồi bấm tìm -> Tải lại toàn bộ dữ liệu lên bảng
            if (searchText.isEmpty()) {
                loadDataToTable(); // Gọi lại hàm load toàn bộ danh sách bạn đã có
                return; // Dừng lại không làm tiếp
            }

            // Dùng try-catch để an toàn khi parse số nguyên
            try {
                int id = Integer.parseInt(searchText);

                Optional<BangQuyDoi> ketQua = bangQuyDoiAppController.findById(id);

                // Xử lý Optional thành List để đưa vào bảng
                if (ketQua.isPresent()) {
                    // Nếu tìm thấy: Biến 1 phần tử này thành 1 List (danh sách) chỉ có 1 phần tử
                    fillTable(java.util.Collections.singletonList(ketQua.get()));
                } else {
                    // Nếu không tìm thấy: Truyền vào 1 danh sách rỗng để làm sạch bảng
                    fillTable(java.util.Collections.emptyList());

                    // Hiển thị Popup thông báo cho người dùng
                    JOptionPane.showMessageDialog(this,
                            "Không tìm thấy quy tắc quy đổi nào với ID = " + id,
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                List<BangQuyDoi> ketQua= bangQuyDoiAppController.findByMaQuyDoi(searchText);
                 if (ketQua != null) {
                    fillTable(ketQua);
                } else {
                    // Nếu không tìm thấy: Truyền vào 1 danh sách rỗng để làm sạch bảng
                    fillTable(java.util.Collections.emptyList());
                }
            }
        });

        JButton btnAdd = new JButton("Thêm mới");

        btnAdd.addActionListener(e -> {
            // Mở cửa sổ nhập liệu (Dialog)
            // Truyền 'this' để làm cửa sổ cha, và 'true' để khóa màn hình chính khi đang nhập
            BangQuyDoiDialog dialog = new BangQuyDoiDialog(this, null);
            dialog.setVisible(true);

            // Sau khi đóng cửa sổ nhập, tải lại bảng để hiện dữ liệu mới
            loadDataToTable();
        });

        JButton btnUpdate = new JButton("Sửa");

        btnUpdate.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                BangQuyDoi bangQuyDoi = new BangQuyDoi();
                bangQuyDoi.setId(Integer.parseInt(table.getValueAt(selectedRow, 0).toString()));
                bangQuyDoi.setPhuongThuc(table.getValueAt(selectedRow, 1).toString());
                bangQuyDoi.setToHop(table.getValueAt(selectedRow, 2).toString());
                bangQuyDoi.setMon(table.getValueAt(selectedRow, 3).toString());
                bangQuyDoi.setDiemA(new BigDecimal(table.getValueAt(selectedRow, 4).toString()));
                bangQuyDoi.setDiemB(new BigDecimal(table.getValueAt(selectedRow, 5).toString()));
                bangQuyDoi.setDiemC(new BigDecimal(table.getValueAt(selectedRow, 6).toString()));
                bangQuyDoi.setDiemD(new BigDecimal(table.getValueAt(selectedRow, 7).toString()));
                bangQuyDoi.setMaQuyDoi(table.getValueAt(selectedRow, 8).toString());
                bangQuyDoi.setPhanVi(table.getValueAt(selectedRow, 9).toString());

                BangQuyDoiDialog dialog = new BangQuyDoiDialog(this, bangQuyDoi);
                dialog.setVisible(true);

                // Sau khi đóng cửa sổ nhập, tải lại bảng để hiện dữ liệu mới
                loadDataToTable();
            }
            else{
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        });

        JButton btnDelete = new JButton("Xóa");

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1)
            {
                bangQuyDoiAppController.delete(Integer.parseInt(table.getValueAt(selectedRow, 0).toString()));
            }
            else{
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnImportExcel = new JButton("Import Excel");

        btnImportExcel.addActionListener(e -> {
            // Mở cửa sổ chọn file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn file Excel để Import");
            // Chỉ cho phép chọn file .xlsx
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // Đọc file Excel thành List
                    List<BangQuyDoi> dataImport = ExcelHelper.docFileExcel(selectedFile);

                    // Gọi Service để lưu toàn bộ vào DB
                    bangQuyDoiAppController.addList(dataImport);

                    // Load lại bảng
                    loadDataToTable();

                    JOptionPane.showMessageDialog(this, "Import thành công " + dataImport.size() + " dòng!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi đọc file Excel! Hãy kiểm tra lại định dạng các cột.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Tạo nút Tải file mẫu
        JButton btnTaiFileMau = new JButton("Tải file mẫu");

        btnTaiFileMau.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu file mẫu Excel (.xlsx)");

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".xlsx");
                }

                try {
                    // ĐỂ KHỞI TẠO BÌNH THƯỜNG Ở ĐÂY (Lách lỗi AutoCloseable của IDE)
                    XSSFWorkbook workbook = new XSSFWorkbook();

                    org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Data");
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(0);

                    String[] headers = {"Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã QĐ", "Phân vị"};
                    for (int i = 0; i < headers.length; i++) {
                        row.createCell(i).setCellValue(headers[i]);
                    }
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    // Chỉ FileOutputStream mới cần để trong try(...)
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                        workbook.write(out);
                    }

                    workbook.close(); // Đóng file thủ công

                    JOptionPane.showMessageDialog(this, "Đã tạo file mẫu (.xlsx) thành công!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Thêm nút vào thanh công cụ (toolbar) cạnh nút Import
        toolbar.add(btnSearch);        
        toolbar.add(btnAdd);
        toolbar.add(btnUpdate);
        toolbar.add(btnDelete);
        toolbar.add(btnTaiFileMau);
        toolbar.add(btnImportExcel);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        panel.add(topPanel, BorderLayout.NORTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ... (Các hàm createHomePanel, createLeftNavBar giữ nguyên như cũ) ...
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel lblWelcome = new JLabel("Hệ thống Quản lý Tuyển sinh - Admin!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 26));
        panel.add(lblWelcome, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeftNavBar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(220, 0));
        navPanel.setBackground(new Color(44, 62, 80));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnHome = createNavButton("Trang chủ");
        btnHome.addActionListener(e -> cardLayout.show(mainContentPanel, "TRANG_CHU"));

        JButton btnQuyDoi = createNavButton("Quản lý Bảng quy đổi");
        btnQuyDoi.addActionListener(e -> cardLayout.show(mainContentPanel, "BANG_QUY_DOI"));

        navPanel.add(btnHome);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btnQuyDoi);
        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        return btn;
    }

    public BangQuyDoiAppController getBangQuyDoiAppController() {
        return this.bangQuyDoiAppController;
    }
}
