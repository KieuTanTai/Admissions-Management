package com.example.admissions_management.application.service.candidate;

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
