package com.lifequest.weekly.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklyReportSummaryResponse(
        Long weeklyReportId,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        BigDecimal averageScore,
        String mainEnemy,
        String status
) {
}
