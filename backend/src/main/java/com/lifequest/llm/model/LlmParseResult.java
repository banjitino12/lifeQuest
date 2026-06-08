package com.lifequest.llm.model;

import java.util.List;

public record LlmParseResult(
        String status,
        List<String> studyTopics,
        List<String> projectWork,
        String mood,
        Integer entertainmentMinutes,
        Integer exerciseMinutes,
        List<String> riskEvents
) {
}
