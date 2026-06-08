package com.lifequest.gamification.model;

public record GameEventResult(
        String eventType,
        String eventCode,
        String eventName,
        Integer eventLevel,
        String eventDescription,
        String effectJson
) {
}
