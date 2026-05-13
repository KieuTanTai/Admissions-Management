package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DiemCongTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "CCCD", "Mã Ngành", "Mã Tổ Hợp", "Phương Thức", "Điểm CC", "Điểm UT", "Tổng Điểm", "Ghi Chú"};
    private List<DiemCongXetTuyen> rows = new ArrayList<>();

    public void setRows(List<DiemCongXetTuyen> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public DiemCongXetTuyen getRowAt(int rowIndex) {
        return rows.get(rowIndex);
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
        DiemCongXetTuyen row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getId();
            case 1 -> row.getTsCccd();
            case 2 -> row.getMaNganh();
            case 3 -> row.getMaToHop();
            case 4 -> row.getPhuongThuc();
            case 5 -> row.getDiemCc();
            case 6 -> row.getDiemUtxt();
            case 7 -> row.getDiemTong();
            case 8 -> row.getGhiChu();
            default -> "";
        };
    }
}
