package com.example.admissions_management.presentation.form.model;

import java.util.ArrayList;
import java.util.List;

public class ToHopMonThiImportResult {

    private int importedCount;
    private int updatedCount;
    private String message;
    private final List<String> errors = new ArrayList<>();

    public int getImportedCount() {
        return importedCount;
    }

    public void incrementImportedCount() {
        importedCount++;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void incrementUpdatedCount() {
        updatedCount++;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        if (error != null) {
            errors.add(error);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}