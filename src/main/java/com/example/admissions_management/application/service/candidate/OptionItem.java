package com.example.admissions_management.application.service.candidate;

public class OptionItem {

    private final String value;
    private final String label;

    public OptionItem(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
