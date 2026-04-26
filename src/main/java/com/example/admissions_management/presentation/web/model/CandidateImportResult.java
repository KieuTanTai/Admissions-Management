package com.example.admissions_management.presentation.web.model;

import java.util.ArrayList;
import java.util.List;

public class CandidateImportResult {

    private int importedCount;
    private int updatedCount;
    private String message;
    private final List<String> errors = new ArrayList<>();

    public int getImportedCount() {
        return importedCount;
    }

    public void incrementImportedCount() {
        this.importedCount++;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void incrementUpdatedCount() {
        this.updatedCount++;
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
            this.errors.add(error);
        }
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}
