package com.example.admissions_management.application.service.dto;

import java.math.BigDecimal;
import java.util.List;

public record MajorConfig(String code,
        String name,
        String originalCombination,
        BigDecimal dgnlThreshold,
        BigDecimal dgnlAdmission,
        BigDecimal regularThreshold,
        BigDecimal regularAdmission,
        List<CombinationSpec> combinations) {
}
