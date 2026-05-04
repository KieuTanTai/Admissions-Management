package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.application.dto.response.CombinationResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CombinationTableModel extends AbstractTableModel {

    private final String[] columns = {
            "id",
            "manganh",
            "matohop",
            "th_mon1",
            "hsmon1",
            "th_mon2",
            "hsmon2",
            "th_mon3",
            "hsmon3",
            "tb_keys",
            "N1",
            "TO",
            "LI",
            "HO",
            "SI",
            "VA",
            "SU",
            "DI",
            "TI",
            "KHAC",
            "KTPL",
            "Độ lệch"
    };

    private List<CombinationResponse> rows = new ArrayList<>();

    public void setRows(List<CombinationResponse> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public CombinationResponse getRowAt(int rowIndex) {
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
        CombinationResponse r = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.getId();
            case 1 -> r.getMaNganh();
            case 2 -> r.getMaToHop();
            case 3 -> r.getThMon1();
            case 4 -> r.getHsMon1();
            case 5 -> r.getThMon2();
            case 6 -> r.getHsMon2();
            case 7 -> r.getThMon3();
            case 8 -> r.getHsMon3();
            case 9 -> r.getTbKeys();
            case 10 -> r.getN1();
            case 11 -> r.getTo();
            case 12 -> r.getLi();
            case 13 -> r.getHo();
            case 14 -> r.getSi();
            case 15 -> r.getVa();
            case 16 -> r.getSu();
            case 17 -> r.getDi();
            case 18 -> r.getTi();
            case 19 -> r.getKhac();
            case 20 -> r.getKtpl();
            case 21 -> r.getDoLech();
            default -> "";
        };
    }
}

