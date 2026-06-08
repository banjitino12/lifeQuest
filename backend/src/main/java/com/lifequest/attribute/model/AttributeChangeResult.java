package com.lifequest.attribute.model;

import java.util.Map;

public record AttributeChangeResult(
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
