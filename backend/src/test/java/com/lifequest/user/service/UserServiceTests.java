package com.lifequest.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.user.dto.CurrentUserResponse;
import com.lifequest.user.dto.UpdateCurrentUserRequest;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserServiceTests {

    private final CurrentUserService currentUserService = org.mockito.Mockito.mock(CurrentUserService.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserService userService = new UserService(currentUserService, userRepository);

    @Test
    void updateCurrentUserUsesCurrentUserIdAndSavesAllowedFields() {
        UserEntity user = user(9L, "old_name");
        when(currentUserService.requireCurrentUserId()).thenReturn(9L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("new_name")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CurrentUserResponse response = userService.updateCurrentUser(new UpdateCurrentUserRequest(
                " new_name ",
                " https://example.com/avatar.png "
        ));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.username()).isEqualTo("new_name");
        assertThat(response.avatar()).isEqualTo("https://example.com/avatar.png");
        verify(userRepository).findById(9L);
    }

    @Test
    void updateCurrentUserRejectsDuplicateUsernameFromAnotherUser() {
        when(currentUserService.requireCurrentUserId()).thenReturn(9L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user(9L, "old_name")));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(user(10L, "taken")));

        assertThatThrownBy(() -> userService.updateCurrentUser(new UpdateCurrentUserRequest("taken", null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    private UserEntity user(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
