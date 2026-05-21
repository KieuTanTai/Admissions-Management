package com.example.admissions_management.application.service.dto;

import java.math.BigDecimal;

public record AdmissionDecision(boolean admitted, String majorCode, BigDecimal score, String combinationCode,
        String methodName) {
}
