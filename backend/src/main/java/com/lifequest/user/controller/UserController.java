package com.lifequest.user.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.user.dto.CurrentUserResponse;
import com.lifequest.user.dto.UpdateCurrentUserRequest;
import com.lifequest.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.ok(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    public ApiResponse<CurrentUserResponse> updateMe(@Valid @RequestBody UpdateCurrentUserRequest request) {
        return ApiResponse.ok(userService.updateCurrentUser(request));
    }
}
