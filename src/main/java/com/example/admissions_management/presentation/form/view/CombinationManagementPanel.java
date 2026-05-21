package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.presentation.form.controller.CombinationFormController;
import com.example.admissions_management.presentation.form.model.CombinationTableModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CombinationManagementPanel extends JPanel {

    private static final Color BG_COLOR = new Color(245, 247, 244);
    private static final Color SURFACE_COLOR = Color.WHITE;
    private static final Color SOFT_COLOR = new Color(236, 244, 239);
    private static final Color LINE_COLOR = new Color(214, 224, 218);
    private static final Color PRIMARY_COLOR = new Color(17, 110, 88);
    private static final Color ACCENT_COLOR = new Color(220, 155, 63);
    private static final Color TEXT_COLOR = new Color(23, 45, 38);
    private static final Color MUTED_COLOR = new Color(102, 121, 113);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private final CombinationFormController controller;
    private final CombinationTableModel tableModel = new CombinationTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);
    private final JLabel pageInfoLabel = new JLabel("Trang 1 / 1", SwingConstants.CENTER);

    private int currentPage = 0;
    private int totalPages = 0;
    private String currentSearchQuery = "";

    public CombinationManagementPanel(CombinationFormController controller) {
        this.controller = controller;

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);

        styleTable();
        refreshAll();
    }

    private JPanel buildHeaderPanel() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(SURFACE_COLOR);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        toolbar.add(createToolbarLabel("Tìm kiếm mã ngành"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        styleTextField(searchField);
        toolbar.add(searchField, c);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
        buttons.setOpaque(false);
        buttons.add(createButton("Tìm", ButtonStyle.PRIMARY, this::onSearch));
        buttons.add(createButton("Thêm", ButtonStyle.ACCENT, this::onInsert));
        buttons.add(createButton("Làm mới", ButtonStyle.SECONDARY, this::refreshAll));

        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        toolbar.add(buttons, c);
        return toolbar;
    }
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(12, 12));
        footer.setBackground(SURFACE_COLOR);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel actionButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtons.setOpaque(false);
        actionButtons.add(createButton("Sửa", ButtonStyle.PRIMARY, this::onUpdateSelected));
        actionButtons.add(createButton("Làm mới dữ liệu", ButtonStyle.SECONDARY, this::refreshAll));

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pager.setOpaque(false);
        JButton prevButton = createButton("<< Trước", ButtonStyle.SECONDARY, this::previousPage);
        JButton nextButton = createButton("Sau >>", ButtonStyle.SECONDARY, this::nextPage);

        pageInfoLabel.setOpaque(true);
        pageInfoLabel.setBackground(SOFT_COLOR);
        pageInfoLabel.setForeground(TEXT_COLOR);
        pageInfoLabel.setFont(UI_FONT_BOLD);
        pageInfoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(11, 18, 11, 18)));

        pager.add(prevButton);
        pager.add(pageInfoLabel);
        pager.add(nextButton);

        footer.add(actionButtons, BorderLayout.WEST);
        footer.add(pager, BorderLayout.EAST);
        return footer;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadPage();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadPage();
        }
    }

    private void loadPage() {
        if (currentSearchQuery.isEmpty()) {
            loadPageData(controller.loadAllPaged(currentPage));
        } else {
            loadPageData(controller.findPaged(currentSearchQuery, currentPage));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadPageData(Map<String, Object> pageData) {
        List<CombinationResponse> content = (List<CombinationResponse>) pageData.get("content");
        currentPage = (Integer) pageData.get("page");
        totalPages = (Integer) pageData.get("totalPages");
        tableModel.setRows(content);
        pageInfoLabel.setText(String.format("Trang %d / %d", currentPage + 1, Math.max(totalPages, 1)));
    }

    private void refreshAll() {
        searchField.setText("");
        currentPage = 0;
        currentSearchQuery = "";
        loadPageData(controller.loadAllPaged(0));
    }

    private void onSearch() {
        currentSearchQuery = searchField.getText().trim();
        currentPage = 0;
        if (currentSearchQuery.isEmpty()) {
            loadPageData(controller.loadAllPaged(0));
        } else {
            loadPageData(controller.findPaged(currentSearchQuery, 0));
        }
    }

    private void onInsert() {
        try {
            CombinationResponse input = showEditorDialog("Thêm ngành - tổ hợp", null);
            if (input == null) {
                return;
            }
            controller.insert(input);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thêm thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng cần chỉnh sửa.", "Chưa chọn dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        CombinationResponse selected = tableModel.getRowAt(modelRow);
        if (selected == null || selected.getId() == null) {
            JOptionPane.showMessageDialog(this, "Không thể đọc dữ liệu đang chọn.", "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            CombinationResponse input = showEditorDialog("Cập nhật ngành - tổ hợp", selected);
            if (input == null) {
                return;
            }
            controller.update(selected.getId(), input);
            loadPage();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cập nhật thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private CombinationResponse showEditorDialog(String title, CombinationResponse initial) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SURFACE_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField maNganhField = new JTextField();
        JTextField maToHopField = new JTextField();
        JTextField thMon1Field = new JTextField();
        JTextField hsMon1Field = new JTextField();
        JTextField thMon2Field = new JTextField();
        JTextField hsMon2Field = new JTextField();
        JTextField thMon3Field = new JTextField();
        JTextField hsMon3Field = new JTextField();
        JTextField tbKeysField = new JTextField();
        JTextField doLechField = new JTextField();

        JTextField[] fields = { maNganhField, maToHopField, thMon1Field, hsMon1Field, thMon2Field, hsMon2Field,
                thMon3Field, hsMon3Field, tbKeysField, doLechField };
        for (JTextField field : fields) {
            styleTextField(field);
        }

        if (initial != null) {
            maNganhField.setText(nullToEmpty(initial.getMaNganh()));
            maToHopField.setText(nullToEmpty(initial.getMaToHop()));
            thMon1Field.setText(nullToEmpty(initial.getThMon1()));
            hsMon1Field.setText(initial.getHsMon1() == null ? "" : String.valueOf(initial.getHsMon1()));
            thMon2Field.setText(nullToEmpty(initial.getThMon2()));
            hsMon2Field.setText(initial.getHsMon2() == null ? "" : String.valueOf(initial.getHsMon2()));
            thMon3Field.setText(nullToEmpty(initial.getThMon3()));
            hsMon3Field.setText(initial.getHsMon3() == null ? "" : String.valueOf(initial.getHsMon3()));
            tbKeysField.setText(nullToEmpty(initial.getTbKeys()));
            doLechField.setText(initial.getDoLech() == null ? "" : initial.getDoLech().toPlainString());
            maNganhField.setEditable(false);
            tbKeysField.setEditable(false);
        }

        int row = 0;
        row = addRow(form, gbc, row, "Mã ngành", maNganhField);
        row = addRow(form, gbc, row, "Mã tổ hợp", maToHopField);
        row = addRow(form, gbc, row, "Môn 1", thMon1Field);
        row = addRow(form, gbc, row, "Hệ số 1", hsMon1Field);
        row = addRow(form, gbc, row, "Môn 2", thMon2Field);
        row = addRow(form, gbc, row, "Hệ số 2", hsMon2Field);
        row = addRow(form, gbc, row, "Môn 3", thMon3Field);
        row = addRow(form, gbc, row, "Hệ số 3", hsMon3Field);
        row = addRow(form, gbc, row, "TB Keys", tbKeysField);
        addRow(form, gbc, row, "Độ lệch", doLechField);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setPreferredSize(new Dimension(560, 480));
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE_COLOR));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String thMon1 = blankToNull(thMon1Field.getText());
        String thMon2 = blankToNull(thMon2Field.getText());
        String thMon3 = blankToNull(thMon3Field.getText());

        CombinationResponse out = new CombinationResponse();
        out.setMaNganh(maNganhField.getText().trim());
        out.setMaToHop(maToHopField.getText().trim());
        out.setThMon1(thMon1);
        out.setHsMon1(parseByteOrNull(hsMon1Field.getText()));
        out.setThMon2(thMon2);
        out.setHsMon2(parseByteOrNull(hsMon2Field.getText()));
        out.setThMon3(thMon3);
        out.setHsMon3(parseByteOrNull(hsMon3Field.getText()));
        out.setTbKeys(blankToNull(tbKeysField.getText()));
        out.setDoLech(parseBigDecimalOrNull(doLechField.getText()));

        out.setN1(matches("N1", thMon1, thMon2, thMon3));
        out.setTo(matches("TO", thMon1, thMon2, thMon3));
        out.setLi(matches("LI", thMon1, thMon2, thMon3));
        out.setHo(matches("HO", thMon1, thMon2, thMon3));
        out.setSi(matches("SI", thMon1, thMon2, thMon3));
        out.setVa(matches("VA", thMon1, thMon2, thMon3));
        out.setSu(matches("SU", thMon1, thMon2, thMon3));
        out.setDi(matches("DI", thMon1, thMon2, thMon3));
        out.setTi(matches("TI", thMon1, thMon2, thMon3));
        out.setKhac(matches("KHAC", thMon1, thMon2, thMon3));
        out.setKtpl(matches("KTPL", thMon1, thMon2, thMon3));

        if (initial != null) {
            out.setId(initial.getId());
        }
        return out;
    }

    private int addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField input) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel view = new JLabel(label);
        view.setFont(UI_FONT_BOLD);
        view.setForeground(MUTED_COLOR);
        form.add(view, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(input, gbc);
        return row + 1;
    }

    private JLabel createToolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT_BOLD);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton createButton(String text, ButtonStyle style, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        button.setPreferredSize(new Dimension(168, 46));
        button.setMinimumSize(new Dimension(168, 46));
        button.setOpaque(true);

        if (style == ButtonStyle.PRIMARY) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else if (style == ButtonStyle.ACCENT) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(TEXT_COLOR);
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
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }

    private void styleTable() {
        table.setFont(UI_FONT);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(233, 239, 235));
        table.setSelectionBackground(new Color(221, 237, 231));
        table.setSelectionForeground(TEXT_COLOR);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(UI_FONT_BOLD);
        header.setBackground(new Color(242, 246, 243));
        header.setForeground(TEXT_COLOR);
        header.setReorderingAllowed(false);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Byte parseByteOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) {
            return null;
        }
        try {
            return Byte.valueOf(v);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị hệ số không hợp lệ: " + value);
        }
    }

    private static BigDecimal parseBigDecimalOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị độ lệch không hợp lệ: " + value);
        }
    }

    private static boolean matches(String inputName, String thMon1, String thMon2, String thMon3) {
        return inputName.equals(thMon1) || inputName.equals(thMon2) || inputName.equals(thMon3);
    }

    private enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        ACCENT
    }
}
