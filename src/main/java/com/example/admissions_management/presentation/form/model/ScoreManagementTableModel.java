package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ScoreManagementTableModel extends AbstractTableModel {

    private final String[] columns = {
            "ID", "CCCD", "So Bao Danh", "d_phuongthuc", "TO", "LI", "HO", "SI", "SU", "DI", "VA", "N1_THI", "N1_CC", "NL1"
    };

    private List<XtDiemThiXetTuyenEntity> rows = new ArrayList<>();

    public void setRows(List<XtDiemThiXetTuyenEntity> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public XtDiemThiXetTuyenEntity getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
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
        XtDiemThiXetTuyenEntity row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getId();
            case 1 -> row.getCccd();
            case 2 -> row.getSoBaoDanh();
            case 3 -> row.getdPhuongThuc();
            case 4 -> row.getTo();
            case 5 -> row.getLi();
            case 6 -> row.getHo();
            case 7 -> row.getSi();
            case 8 -> row.getSu();
            case 9 -> row.getDi();
            case 10 -> row.getVa();
            case 11 -> row.getN1Thi();
            case 12 -> row.getN1Cc();
            case 13 -> row.getNl1();
            default -> "";
        };
    }
}
