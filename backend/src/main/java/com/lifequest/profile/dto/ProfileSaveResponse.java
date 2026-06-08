package com.lifequest.profile.dto;

public record ProfileSaveResponse(
        Long profileId,
        boolean attributeInitialized,
        boolean routeProgressInitialized
) {
}
