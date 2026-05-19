package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import com.example.admissions_management.presentation.form.model.NguyenVongTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.Map;

public class NguyenVongPanel extends JPanel {

    private static final Color BG_COLOR = new Color(245, 247, 244);
    private static final Color SURFACE_COLOR = Color.WHITE;
    private static final Color SOFT_COLOR = new Color(248, 251, 248);
    private static final Color LINE_COLOR = new Color(220, 229, 223);
    private static final Color PRIMARY_COLOR = new Color(12, 122, 99);
    private static final Color ACCENT_COLOR = new Color(231, 150, 45);
    private static final Color DANGER_COLOR = new Color(194, 65, 53);
    private static final Color TEXT_COLOR = new Color(29, 46, 40);
    private static final Color MUTED_COLOR = new Color(100, 118, 110);
    private static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Dimension FIELD_SIZE = new Dimension(320, 30);
    private static final Dimension LABEL_SIZE = new Dimension(130, 24);

    private final NguyenVongConsoleController controller;
    private final NguyenVongTableModel tableModel;
    private final JTable table;

    private final JTextField searchCccdField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField nnCccdField = new JTextField();
    private final JTextField maNganhField = new JTextField();
    private final JTextField maToHopField = new JTextField();
    private final JTextField nvThuTuField = new JTextField();
    private final JTextField diemThxtField = new JTextField();
    private final JTextField diemUtqdField = new JTextField();
    private final JTextField diemCongField = new JTextField();
    private final JTextField diemXetTuyenField = new JTextField();
    private final JTextField ketQuaField = new JTextField();
    private final JTextField nvKeysField = new JTextField();
    private final JTextField phuongThucField = new JTextField();
    private final JTextField ttThmField = new JTextField();

    public NguyenVongPanel(NguyenVongConsoleController controller) {
        this.controller = controller;
        this.tableModel = new NguyenVongTableModel();
        this.table = new JTable(tableModel);
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table.setFillsViewportHeight(true);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        styleTable();
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildEditPanel(), BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
        setReadOnlyResultFields();
        refreshTable();
    }

    private void setReadOnlyResultFields() {
        idField.setEditable(false);
        diemCongField.setEditable(false);
        diemXetTuyenField.setEditable(false);
        ketQuaField.setEditable(false);
        nvKeysField.setEditable(false);
        phuongThucField.setEditable(false);
        ttThmField.setEditable(false);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JLabel searchLabel = new JLabel("Tìm theo CCCD");
        searchLabel.setFont(LABEL_FONT);
        searchLabel.setForeground(MUTED_COLOR);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(searchLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        styleTextField(searchCccdField);
        panel.add(searchCccdField, c);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        JButton searchButton = new JButton("Tìm");
        JButton refreshButton = new JButton("Làm mới");
        JButton clearButton = new JButton("Xóa lọc");
        JButton processMajorButton = new JButton("Xét ngành");
        JButton processAllButton = new JButton("Xét tất cả");
        JButton deleteAllButton = new JButton("Xóa tất cả");
        styleButton(searchButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(refreshButton, SURFACE_COLOR, TEXT_COLOR);
        styleButton(clearButton, SURFACE_COLOR, TEXT_COLOR);
        styleButton(processMajorButton, ACCENT_COLOR, TEXT_COLOR);
        styleButton(processAllButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(deleteAllButton, DANGER_COLOR, Color.WHITE);
        rightButtons.add(searchButton);
        rightButtons.add(refreshButton);
        rightButtons.add(clearButton);
        rightButtons.add(processMajorButton);
        rightButtons.add(processAllButton);
        rightButtons.add(deleteAllButton);

        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        panel.add(rightButtons, c);

        searchButton.addActionListener(e -> search());
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearFields());
        processMajorButton.addActionListener(e -> processMajor());
        processAllButton.addActionListener(e -> processAll());
        deleteAllButton.addActionListener(e -> deleteAllData());
        return panel;
    }

    private JPanel buildEditPanel() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setPreferredSize(new Dimension(560, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outer.setBackground(BG_COLOR);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, 0);

        JPanel section1 = createFieldSection("Thông Tin Chính", new Object[][] {
                { "ID", idField },
                { "CCCD", nnCccdField }
        });
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        contentPanel.add(section1, gc);

        JPanel section2 = createFieldSection("Ngành & Tổ Hợp", new Object[][] {
                { "Mã Ngành", maNganhField },
                { "Mã Tổ Hợp", maToHopField }
        });
        gc.gridy = 1;
        gc.insets = new Insets(10, 0, 0, 0);
        contentPanel.add(section2, gc);

        JPanel section3 = createFieldSection("Nguyện Vọng", new Object[][] {
                { "NV Thứ Tự", nvThuTuField }
        });
        gc.gridy = 2;
        gc.insets = new Insets(10, 0, 0, 0);
        contentPanel.add(section3, gc);

        JPanel section4 = createFieldSection("Điểm", new Object[][] {
                { "Điểm Thi", diemThxtField },
                { "Điểm UTQD", diemUtqdField },
                { "Điểm Cộng", diemCongField },
                { "Điểm Xét Tuyển", diemXetTuyenField }
        });
        gc.gridy = 3;
        gc.insets = new Insets(10, 0, 0, 0);
        contentPanel.add(section4, gc);

        JPanel section5 = createFieldSection("Trạng Thái", new Object[][] {
                { "Kết Quả", ketQuaField },
                { "NV Keys", nvKeysField },
                { "Phương Thức", phuongThucField },
                { "TT THM", ttThmField }
        });
        gc.gridy = 4;
        gc.insets = new Insets(10, 0, 0, 0);
        contentPanel.add(section5, gc);

        gc.gridy = 5;
        gc.weighty = 1.0;
        contentPanel.add(new JPanel(), gc);

        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        outer.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE_COLOR);
        btnPanel.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        JButton saveButton = new JButton("Lưu");
        JButton deleteButton = new JButton("Xóa");
        JButton clearButton = new JButton("Xóa form");
        styleButton(saveButton, PRIMARY_COLOR, Color.WHITE);
        styleButton(deleteButton, DANGER_COLOR, Color.WHITE);
        styleButton(clearButton, SURFACE_COLOR, TEXT_COLOR);

        saveButton.setPreferredSize(new Dimension(80, 32));
        deleteButton.setPreferredSize(new Dimension(80, 32));
        clearButton.setPreferredSize(new Dimension(80, 32));

        btnPanel.add(saveButton);
        btnPanel.add(deleteButton);
        btnPanel.add(clearButton);

        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearFields());

        outer.add(btnPanel, BorderLayout.SOUTH);

        return outer;
    }

    private JPanel createFieldSection(String title, Object[][] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE_COLOR, 1),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                SECTION_TITLE_FONT,
                TEXT_COLOR));
        panel.setBackground(SOFT_COLOR);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 10, 8, 10);

        for (int i = 0; i < fields.length; i++) {
            String label = (String) fields[i][0];
            Object comp = fields[i][1];

            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.0;
            gc.gridwidth = 1;
            gc.anchor = GridBagConstraints.NORTHWEST;
            JLabel lbl = new JLabel(label);
            lbl.setFont(LABEL_FONT);
            lbl.setForeground(MUTED_COLOR);
            lbl.setPreferredSize(LABEL_SIZE);
            lbl.setMinimumSize(LABEL_SIZE);
            lbl.setMaximumSize(LABEL_SIZE);
            panel.add(lbl, gc);

            gc.gridx = 1;
            gc.weightx = 1.0;
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.anchor = GridBagConstraints.WEST;
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                if (jcomp instanceof JTextField) {
                    JTextField tf = (JTextField) jcomp;
                    styleTextField(tf);
                    tf.setPreferredSize(FIELD_SIZE);
                    tf.setMinimumSize(new Dimension(0, FIELD_SIZE.height));
                    tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_SIZE.height));
                    panel.add(tf, gc);
                }
            }
        }

        return panel;
    }

    private void styleTable() {
        table.setRowHeight(30);
        table.setFont(FIELD_FONT);
        table.setGridColor(new Color(237, 242, 239));
        table.setSelectionBackground(new Color(218, 239, 232));
        table.setSelectionForeground(TEXT_COLOR);
        table.getTableHeader().setFont(BUTTON_FONT);
        table.getTableHeader().setForeground(new Color(64, 93, 83));
        table.getTableHeader().setBackground(new Color(242, 247, 244));
    }

    private void styleTextField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setMargin(new Insets(6, 8, 6, 8));
        field.setBackground(SURFACE_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBorder(BorderFactory.createLineBorder(LINE_COLOR, 1));
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private void search() {
        String cccd = searchCccdField.getText().trim();
        if (cccd.isEmpty()) {
            refreshTable();
        } else {
            tableModel.setRows(controller.loadByCccd(cccd));
            table.clearSelection();
            clearDetailFields();
        }
    }

    private void refreshTable() {
        tableModel.setRows(controller.loadAll());
        table.clearSelection();
        clearDetailFields();
    }

    private void save() {
        try {
            controller.save(
                    nnCccdField.getText(),
                    maNganhField.getText(),
                    maToHopField.getText(),
                    nvThuTuField.getText(),
                    diemThxtField.getText(),
                    diemUtqdField.getText());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.", "Xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            NguyenVongXetTuyen row = tableModel.getRowAt(selectedRow);
            controller.delete(row.getId());
            refreshTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processMajor() {
        String maNganh = maNganhField.getText().trim();
        if (maNganh.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã ngành để xét tuyển.", "Xét tuyển",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Map<String, Object> result = controller.processMajor(maNganh);
            JOptionPane.showMessageDialog(this,
                    "Đã xét ngành " + maNganh + "\nĐậu: " + result.get("passCount") + "\nTrượt: "
                            + result.get("failCount"),
                    "Xét tuyển", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Xét tuyển", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processAll() {
        try {
            int count = controller.processAll().size();
            JOptionPane.showMessageDialog(this, "Đã chạy xét tuyển cho " + count + " ngành.", "Xét tuyển",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Xét tuyển", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        NguyenVongXetTuyen row = tableModel.getRowAt(modelRow);
        idField.setText(row.getId() == null ? "" : String.valueOf(row.getId()));
        nnCccdField.setText(row.getNnCccd());
        maNganhField.setText(row.getNvMaNganh());
        maToHopField.setText(row.getMaToHop());
        nvThuTuField.setText(row.getNvThuTu() == null ? "" : row.getNvThuTu().toString());
        diemThxtField.setText(row.getDiemThxt() == null ? "" : row.getDiemThxt().toPlainString());
        diemUtqdField.setText(row.getDiemUtqd() == null ? "" : row.getDiemUtqd().toPlainString());
        diemCongField.setText(row.getDiemCong() == null ? "" : row.getDiemCong().toPlainString());
        diemXetTuyenField.setText(row.getDiemXetTuyen() == null ? "" : row.getDiemXetTuyen().toPlainString());
        ketQuaField.setText(row.getNvKetQua() == null ? "" : row.getNvKetQua());
        nvKeysField.setText(row.getNvKeys() == null ? "" : row.getNvKeys());
        phuongThucField.setText(row.getTtPhuongThuc() == null ? "" : row.getTtPhuongThuc());
        ttThmField.setText(row.getTtThm() == null ? "" : row.getTtThm());
    }

    private void clearDetailFields() {
        idField.setText("");
        nnCccdField.setText("");
        maNganhField.setText("");
        maToHopField.setText("");
        nvThuTuField.setText("");
        diemThxtField.setText("");
        diemUtqdField.setText("");
        diemCongField.setText("");
        diemXetTuyenField.setText("");
        ketQuaField.setText("");
        nvKeysField.setText("");
        phuongThucField.setText("");
        ttThmField.setText("");
    }

    private void clearFields() {
        searchCccdField.setText("");
        clearDetailFields();
        table.clearSelection();
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tất cả dữ liệu? Hành động này không thể hoàn tác!",
                "Xác nhận xóa tất cả",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteAll();
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Xóa tất cả dữ liệu thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Xóa thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}