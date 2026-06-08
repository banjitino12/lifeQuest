package com.lifequest.profile.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.profile.dto.ProfileRequest;
import com.lifequest.profile.dto.ProfileResponse;
import com.lifequest.profile.dto.ProfileSaveResponse;
import com.lifequest.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getCurrentProfile() {
        return ApiResponse.ok(profileService.getCurrentProfile());
    }

    @PutMapping("/me")
    public ApiResponse<ProfileSaveResponse> saveProfile(@Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(profileService.saveProfile(request));
    }
}
