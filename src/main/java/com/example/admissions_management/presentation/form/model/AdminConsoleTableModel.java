package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.application.dto.response.ApplicantResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class AdminConsoleTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Full Name", "Email", "Program"};
    private List<ApplicantResponse> rows = new ArrayList<>();

    public void setRows(List<ApplicantResponse> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ApplicantResponse row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getId();
            case 1 -> row.getFullName();
            case 2 -> row.getEmail();
            case 3 -> row.getProgram();
            default -> "";
        };
    }
}
