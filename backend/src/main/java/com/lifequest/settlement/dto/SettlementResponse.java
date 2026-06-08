package com.lifequest.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SettlementResponse(
        Long dailyLogId,
        LocalDate logDate,
        String sourceType,
        boolean dailyLogUpdated,
        ScoreBlock score,
        AttributeChangeBlock attributeChange,
        List<GameEventBlock> events,
        LlmBlock llm,
        List<TomorrowTaskBlock> tomorrowTasks,
        String basicSuggestion
) {

    public record ScoreBlock(
            BigDecimal dailyScore,
            String rating,
            BigDecimal growthScore,
            BigDecimal executionScore,
            BigDecimal energyScore,
            BigDecimal moodScore,
            BigDecimal distractionScore,
            BigDecimal reflectionScore,
            Map<String, String> reasons
    ) {
    }

    public record AttributeChangeBlock(
            Integer focusDelta,
            Integer disciplineDelta,
            Integer knowledgeDelta,
            Integer energyDelta,
            Integer moodDelta,
            Integer executionDelta,
            Integer balanceDelta,
            Integer expDelta,
            Map<String, String> reasons
    ) {
    }

    public record GameEventBlock(
            String eventType,
            String eventCode,
            String eventName,
            Integer eventLevel,
            String eventDescription,
            String effectJson
    ) {
    }

    public record LlmBlock(
            String status,
            boolean fallbackUsed,
            String feedback,
            String storyNarration
    ) {
    }

    public record TomorrowTaskBlock(
            String taskType,
            String title,
            String generatedBy
    ) {
    }
}
