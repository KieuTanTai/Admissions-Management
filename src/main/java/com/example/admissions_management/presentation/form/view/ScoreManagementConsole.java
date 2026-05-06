package com.example.admissions_management.presentation.form.view;

import com.example.admissions_management.application.service.ScoreManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.presentation.form.controller.ScoreManagementConsoleController;
import com.example.admissions_management.presentation.form.model.ScoreManagementTableModel;
import com.example.admissions_management.presentation.web.model.ScoreManagementForm;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.math.BigDecimal;

@Component
public class ScoreManagementConsole extends JFrame {

    private final ScoreManagementConsoleController controller;
    private final ObjectProvider<AdminConsole> adminConsoleProvider;
    private final ScoreManagementTableModel tableModel;

    private final JTextField idField = new JTextField();
    private final JTextField cccdField = new JTextField();
    private final JTextField soBaoDanhField = new JTextField();
    private final JTextField phuongThucField = new JTextField("THPT_A00");

    private final JTextField toField = new JTextField();
    private final JTextField liField = new JTextField();
    private final JTextField hoField = new JTextField();
    private final JTextField siField = new JTextField();
    private final JTextField suField = new JTextField();
    private final JTextField diField = new JTextField();
    private final JTextField vaField = new JTextField();
    private final JTextField n1ThiField = new JTextField();
    private final JTextField n1CcField = new JTextField();
    private final JTextField nl1Field = new JTextField();

    private final JComboBox<String> filterType = new JComboBox<>(new String[]{"ALL", "THPT", "VSAT", "DGNL"});
    private final JTable table;

    public ScoreManagementConsole(ScoreManagementConsoleController controller,
                                  ObjectProvider<AdminConsole> adminConsoleProvider) {
        this.controller = controller;
        this.adminConsoleProvider = adminConsoleProvider;
        this.tableModel = new ScoreManagementTableModel();
        this.table = new JTable(tableModel);

        setTitle("Admissions - Score Management Console");
        setSize(1260, 700);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateFromSelectedRow();
            }
        });

        refreshTable();
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel left = new JPanel(new GridLayout(1, 5, 8, 8));
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Form");
        JButton deleteButton = new JButton("Delete Selected");
        JButton importButton = new JButton("Import Excel");
        JButton backButton = new JButton("Back To Admin");

        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearForm());
        deleteButton.addActionListener(e -> deleteSelected());
        importButton.addActionListener(e -> importExcel());
        backButton.addActionListener(e -> backToAdmin());

        left.add(refreshButton);
        left.add(clearButton);
        left.add(deleteButton);
        left.add(importButton);
        left.add(backButton);

        JPanel right = new JPanel(new GridLayout(1, 3, 8, 8));
        right.add(new JLabel("Filter by type"));
        right.add(filterType);
        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(e -> refreshTable());
        right.add(filterButton);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel fields = new JPanel(new GridLayout(4, 7, 8, 8));
        idField.setEditable(false);

        addField(fields, "ID", idField);
        addField(fields, "CCCD", cccdField);
        addField(fields, "So Bao Danh", soBaoDanhField);
        addField(fields, "d_phuongthuc", phuongThucField);
        addField(fields, "TO", toField);
        addField(fields, "LI", liField);
        addField(fields, "HO", hoField);
        addField(fields, "SI", siField);
        addField(fields, "SU", suField);
        addField(fields, "DI", diField);
        addField(fields, "VA", vaField);
        addField(fields, "N1_THI", n1ThiField);
        addField(fields, "N1_CC", n1CcField);
        addField(fields, "NL1", nl1Field);

        JPanel actions = new JPanel(new GridLayout(1, 1, 8, 8));
        JButton saveButton = new JButton("Save / Update");
        saveButton.addActionListener(e -> saveOrUpdate());
        actions.add(saveButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private void addField(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private void refreshTable() {
        String selectedType = filterType.getSelectedItem() == null ? "ALL" : filterType.getSelectedItem().toString();
        tableModel.setRows(controller.loadByType(selectedType));
    }

    private void populateFromSelectedRow() {
        int selectedRow = table.getSelectedRow();
        XtDiemThiXetTuyenEntity row = tableModel.getRowAt(selectedRow);
        if (row == null) {
            return;
        }

        idField.setText(toText(row.getId()));
        cccdField.setText(toText(row.getCccd()));
        soBaoDanhField.setText(toText(row.getSoBaoDanh()));
        phuongThucField.setText(toText(row.getdPhuongThuc()));
        toField.setText(toText(row.getTo()));
        liField.setText(toText(row.getLi()));
        hoField.setText(toText(row.getHo()));
        siField.setText(toText(row.getSi()));
        suField.setText(toText(row.getSu()));
        diField.setText(toText(row.getDi()));
        vaField.setText(toText(row.getVa()));
        n1ThiField.setText(toText(row.getN1Thi()));
        n1CcField.setText(toText(row.getN1Cc()));
        nl1Field.setText(toText(row.getNl1()));
    }

    private void saveOrUpdate() {
        try {
            ScoreManagementForm form = new ScoreManagementForm();
            if (!idField.getText().trim().isEmpty()) {
                form.setId(Integer.parseInt(idField.getText().trim()));
            }

            form.setCccd(cccdField.getText().trim());
            form.setSoBaoDanh(soBaoDanhField.getText().trim());
            form.setdPhuongThuc(phuongThucField.getText().trim());
            form.setTo(parseDecimal(toField.getText()));
            form.setLi(parseDecimal(liField.getText()));
            form.setHo(parseDecimal(hoField.getText()));
            form.setSi(parseDecimal(siField.getText()));
            form.setSu(parseDecimal(suField.getText()));
            form.setDi(parseDecimal(diField.getText()));
            form.setVa(parseDecimal(vaField.getText()));
            form.setN1Thi(parseDecimal(n1ThiField.getText()));
            form.setN1Cc(parseDecimal(n1CcField.getText()));
            form.setNl1(parseDecimal(nl1Field.getText()));

            if (form.getCccd() == null || form.getCccd().isBlank()) {
                throw new IllegalArgumentException("CCCD la bat buoc.");
            }

            XtDiemThiXetTuyenEntity saved = controller.save(form);
            JOptionPane.showMessageDialog(this,
                    "Da luu ban ghi diem: ID=" + saved.getId() + ", CCCD=" + saved.getCccd(),
                    "Save Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "Luu that bai: " + exception.getMessage(),
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        XtDiemThiXetTuyenEntity row = tableModel.getRowAt(selectedRow);
        if (row == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon 1 dong de xoa.",
                    "Delete Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban chac chan muon xoa ID=" + row.getId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteById(row.getId());
            clearForm();
            refreshTable();
        }
    }

    private void importExcel() {
        System.out.println("DEBUG: importExcel() called");
        try {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel files (*.xlsx, *.xls)", "xlsx", "xls");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Select Excel file to import");
            
            System.out.println("DEBUG: JFileChooser created");
            System.out.println("DEBUG: About to show dialog");
            
            int result = fileChooser.showOpenDialog(ScoreManagementConsole.this);
            
            System.out.println("DEBUG: Dialog result = " + result + " (APPROVE_OPTION=" + JFileChooser.APPROVE_OPTION + ")");
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("DEBUG: Selected file: " + selectedFile.getAbsolutePath());
                
                try {
                    ScoreManagementService.ImportResult importResult = controller.importExcel(selectedFile);
                    System.out.println("DEBUG: Import result: " + importResult.message());
                    
                    JOptionPane.showMessageDialog(this,
                            importResult.message(),
                            "Import Result",
                            importResult.message().toLowerCase().contains("that bai") || importResult.message().toLowerCase().contains("khong") 
                                ? JOptionPane.WARNING_MESSAGE 
                                : JOptionPane.INFORMATION_MESSAGE);
                    refreshTable();
                } catch (Exception exception) {
                    System.out.println("DEBUG: Exception during import: " + exception.getMessage());
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Loi khi import: " + exception.getMessage(),
                            "Import Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("DEBUG: Dialog canceled");
            }
        } catch (Exception ex) {
            System.out.println("DEBUG: Exception in importExcel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void backToAdmin() {
        AdminConsole adminConsole = adminConsoleProvider.getObject();
        adminConsole.setVisible(true);
        setVisible(false);
    }

    private void clearForm() {
        idField.setText("");
        cccdField.setText("");
        soBaoDanhField.setText("");
        phuongThucField.setText("THPT_A00");
        toField.setText("");
        liField.setText("");
        hoField.setText("");
        siField.setText("");
        suField.setText("");
        diField.setText("");
        vaField.setText("");
        n1ThiField.setText("");
        n1CcField.setText("");
        nl1Field.setText("");
        table.clearSelection();
    }

    private BigDecimal parseDecimal(String value) {
        return controller.parseDecimal(value);
    }

    private String toText(Object value) {
        return value == null ? "" : value.toString();
    }
}
