package com.lifequest.trend.dto;

import java.util.List;

public record TrendSummaryResponse(
        List<?> scoreTrend,
        List<?> studyHoursTrend,
        List<?> sleepTrend,
        List<?> moodTrend,
        List<?> attributeTrend,
        List<?> eventStats
) {
}
