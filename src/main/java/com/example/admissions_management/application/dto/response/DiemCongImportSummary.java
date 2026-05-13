package com.example.admissions_management.application.dto.response;

public class DiemCongImportSummary {
    private int totalRows;
    private int newCount;
    private int updatedCount;
    private int skippedCount;

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getProcessedCount() {
        return newCount + updatedCount;
    }

    public String toMessage() {
        return "Import xong!\n" +
                "Tổng dòng đọc: " + totalRows + "\n" +
                "Mới: " + newCount + "\n" +
                "Cập nhật: " + updatedCount + "\n" +
                "Bỏ qua: " + skippedCount;
    }
}
