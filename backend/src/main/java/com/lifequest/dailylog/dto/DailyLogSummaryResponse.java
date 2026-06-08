package com.lifequest.dailylog.dto;

import java.time.LocalDate;

public record DailyLogSummaryResponse(
        Long dailyLogId,
        LocalDate logDate,
        String sourceType,
        String llmStatus,
        boolean updated
) {
}
