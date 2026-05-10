package com.example.admissions_management.application.dto.request;

public class RegisterApplicantRequest {

    private String fullName;
    private String email;
    private String program;

    public RegisterApplicantRequest() {
    }

    public RegisterApplicantRequest(String fullName, String email, String program) {
        this.fullName = fullName;
        this.email = email;
        this.program = program;
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
