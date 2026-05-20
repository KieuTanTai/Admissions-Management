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

    private final CardLayout cardLayout;
    private final JPanel mainContentPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private final BangQuyDoiAppController bangQuyDoiAppController;

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

        loadDataToTable();
    }

    private void loadDataToTable() {
        fillTable(bangQuyDoiAppController.getAll());
    }

    private void fillTable(List<BangQuyDoi> danhSach) {
        tableModel.setRowCount(0);
        for (BangQuyDoi qd : danhSach) {
            tableModel.addRow(new Object[] {
                    qd.getId(),
                    qd.getPhuongThuc(),
                    qd.getToHop(),
                    qd.getMon(),
                    qd.getDiemA(),
                    qd.getDiemB(),
                    qd.getDiemC(),
                    qd.getDiemD(),
                    qd.getMaQuyDoi(),
                    qd.getPhanVi()
            });
        }
    }

    private JPanel createBangQuyDoiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(244, 247, 243));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Bảng Quy đổi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(20, 49, 41));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        toolbar.add(txtSearch);

        String[] columnNames = { "ID", "Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D",
                "Mã quy đổi", "Phân vị" };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(232, 239, 235));
        table.getTableHeader().setForeground(new Color(20, 49, 41));
        table.setGridColor(new Color(220, 229, 223));
        table.setSelectionBackground(new Color(220, 237, 231));
        table.setSelectionForeground(new Color(20, 49, 41));

        JButton btnSearch = createActionButton("Tìm kiếm", new Color(12, 122, 99), Color.WHITE, false);
        btnSearch.addActionListener(e -> {
            String searchText = txtSearch.getText().trim();
            if (searchText.isEmpty()) {
                loadDataToTable();
                return;
            }

            try {
                int id = Integer.parseInt(searchText);
                Optional<BangQuyDoi> ketQua = bangQuyDoiAppController.findById(id);
                if (ketQua.isPresent()) {
                    fillTable(java.util.Collections.singletonList(ketQua.get()));
                } else {
                    fillTable(java.util.Collections.emptyList());
                    JOptionPane.showMessageDialog(this,
                            "Không tìm thấy quy tắc quy đổi nào với ID = " + id,
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                List<BangQuyDoi> ketQua = bangQuyDoiAppController.findByMaQuyDoi(searchText);
                if (ketQua != null) {
                    fillTable(ketQua);
                } else {
                    fillTable(java.util.Collections.emptyList());
                }
            }
        });

        JButton btnAdd = createActionButton("Thêm mới", new Color(216, 154, 62), new Color(43, 26, 4), false);
        btnAdd.addActionListener(e -> {
            BangQuyDoiDialog dialog = new BangQuyDoiDialog(this, null);
            dialog.setVisible(true);
            loadDataToTable();
        });

        JButton btnUpdate = createActionButton("Sửa", Color.WHITE, new Color(20, 49, 41), true);
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
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnDelete = createActionButton("Xóa", new Color(189, 76, 60), Color.WHITE, false);
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                bangQuyDoiAppController.delete(Integer.parseInt(table.getValueAt(selectedRow, 0).toString()));
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnImportExcel = createActionButton("Import Excel", new Color(12, 122, 99), Color.WHITE, false);
        btnImportExcel.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn file Excel để import");
            fileChooser
                    .setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    List<BangQuyDoi> dataImport = ExcelHelper.docFileExcel(selectedFile);
                    bangQuyDoiAppController.addList(dataImport);
                    loadDataToTable();
                    JOptionPane.showMessageDialog(this,
                            "Import thành công " + dataImport.size() + " dòng!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi đọc file Excel. Hãy kiểm tra lại định dạng các cột.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnTaiFileMau = createActionButton("Tải file mẫu", Color.WHITE, new Color(20, 49, 41), true);
        btnTaiFileMau.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu file mẫu Excel (.xlsx)");

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".xlsx");
                }

                try {
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Data");
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(0);

                    String[] headers = { "Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D",
                            "Mã QĐ", "Phân vị" };
                    for (int i = 0; i < headers.length; i++) {
                        row.createCell(i).setCellValue(headers[i]);
                        sheet.autoSizeColumn(i);
                    }

                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                        workbook.write(out);
                    }

                    workbook.close();
                    JOptionPane.showMessageDialog(this, "Đã tạo file mẫu (.xlsx) thành công!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(244, 247, 243));

        JLabel lblWelcome = new JLabel("Hệ thống Quản lý Tuyển sinh - Admin", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblWelcome.setForeground(new Color(20, 49, 41));
        panel.add(lblWelcome, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeftNavBar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(220, 0));
        navPanel.setBackground(new Color(16, 40, 33));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));

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
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(24, 58, 47));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return btn;
    }

    private JButton createActionButton(String text, Color background, Color foreground, boolean outlined) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setBorder(outlined
                ? BorderFactory.createLineBorder(new Color(204, 216, 210))
                : BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public BangQuyDoiAppController getBangQuyDoiAppController() {
        return this.bangQuyDoiAppController;
    }
}
