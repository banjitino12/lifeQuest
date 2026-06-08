package com.lifequest.dailylog.dto;

import com.lifequest.dailylog.entity.DailyLogEntity;

public record DailyLogSubmitResult(
        DailyLogEntity dailyLog,
        String sourceType,
        String llmStatus,
        boolean updated
) {
}
