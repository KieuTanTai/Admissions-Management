package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.domain.model.BangQuyDoi;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class BangQuyDoiTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Phương Thức", "Tổ Hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã Quy Đổi", "Phân Vị"};
    private List<BangQuyDoi> rows = new ArrayList<>();

    public void setRows(List<BangQuyDoi> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public BangQuyDoi getRowAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() { return rows.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BangQuyDoi r = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.getId();
            case 1 -> r.getPhuongThuc();
            case 2 -> r.getToHop();
            case 3 -> r.getMon();
            case 4 -> r.getDiemA();
            case 5 -> r.getDiemB();
            case 6 -> r.getDiemC();
            case 7 -> r.getDiemD();
            case 8 -> r.getMaQuyDoi();
            case 9 -> r.getPhanVi();
            default -> "";
        };
    }
}
