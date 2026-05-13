package com.example.admissions_management.presentation.form.model;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class NguyenVongTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "CCCD", "Mã Ngành", "Mã Tổ Hợp", "NV TT", "Điểm THXT", "Điểm UTQD", "Điểm Cộng", "Điểm Xét Tuyển", "Kết Quả", "NV Keys", "Phương Thức", "TT THM"};
    private List<NguyenVongXetTuyen> rows = new ArrayList<>();

    public void setRows(List<NguyenVongXetTuyen> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public NguyenVongXetTuyen getRowAt(int rowIndex) {
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
        NguyenVongXetTuyen row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getId();
            case 1 -> row.getNnCccd();
            case 2 -> row.getNvMaNganh();
            case 3 -> row.getMaToHop();
            case 4 -> row.getNvThuTu();
            case 5 -> formatDecimal(row.getDiemThxt());
            case 6 -> formatDecimal(row.getDiemUtqd());
            case 7 -> formatDecimal(row.getDiemCong());
            case 8 -> formatDecimal(row.getDiemXetTuyen());
            case 9 -> row.getNvKetQua();
            case 10 -> row.getNvKeys();
            case 11 -> row.getTtPhuongThuc();
            case 12 -> row.getTtThm();
            default -> "";
        };
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
