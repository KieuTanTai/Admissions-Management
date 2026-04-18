package com.example.admissions_management.domain.model;

public class Applicant {

    private Long id;
    private String fullName;
    private String email;
    private String program;

    public Applicant() {
    }

    public Applicant(Long id, String fullName, String email, String program) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.program = program;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}
