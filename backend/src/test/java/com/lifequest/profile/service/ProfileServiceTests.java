package com.lifequest.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifequest.attribute.entity.UserAttributeEntity;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.enums.FeedbackStyle;
import com.lifequest.common.enums.GoalType;
import com.lifequest.common.enums.RecordStatus;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import com.lifequest.profile.dto.ProfileRequest;
import com.lifequest.profile.dto.ProfileResponse;
import com.lifequest.profile.dto.ProfileSaveResponse;
import com.lifequest.profile.entity.UserProfileEntity;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.route.entity.GrowthRouteEntity;
import com.lifequest.route.entity.RouteChapterEntity;
import com.lifequest.route.entity.RouteLevelEntity;
import com.lifequest.route.entity.UserRouteProgressEntity;
import com.lifequest.route.repository.GrowthRouteRepository;
import com.lifequest.route.repository.RouteChapterRepository;
import com.lifequest.route.repository.RouteLevelRepository;
import com.lifequest.route.repository.UserRouteProgressRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProfileServiceTests {

    private final CurrentUserService currentUserService = org.mockito.Mockito.mock(CurrentUserService.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final UserAttributeRepository userAttributeRepository = org.mockito.Mockito.mock(UserAttributeRepository.class);
    private final GrowthRouteRepository growthRouteRepository = org.mockito.Mockito.mock(GrowthRouteRepository.class);
    private final RouteChapterRepository routeChapterRepository = org.mockito.Mockito.mock(RouteChapterRepository.class);
    private final RouteLevelRepository routeLevelRepository = org.mockito.Mockito.mock(RouteLevelRepository.class);
    private final UserRouteProgressRepository userRouteProgressRepository = org.mockito.Mockito.mock(UserRouteProgressRepository.class);

    private final ProfileService profileService = new ProfileService(
            currentUserService,
            userProfileRepository,
            userAttributeRepository,
            growthRouteRepository,
            routeChapterRepository,
            routeLevelRepository,
            userRouteProgressRepository
    );

    @Test
    void getCurrentProfileReturnsIncompleteWhenMissing() {
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(userProfileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        ProfileResponse response = profileService.getCurrentProfile();

        assertThat(response.completed()).isFalse();
        assertThat(response.goalType()).isNull();
    }

    @Test
    void saveProfileInitializesAttributeAndRouteProgressOnFirstSave() {
        GrowthRouteEntity route = route(3L, "后端实习路线", GoalType.JOB_INTERVIEW);
        RouteChapterEntity chapter = new RouteChapterEntity();
        chapter.setId(11L);
        RouteLevelEntity level = new RouteLevelEntity();
        level.setId(12L);

        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(userProfileRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(growthRouteRepository.findByGoalTypeAndDefaultRouteTrue(GoalType.JOB_INTERVIEW)).thenReturn(Optional.of(route));
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(invocation -> {
            UserProfileEntity profile = invocation.getArgument(0);
            profile.setId(21L);
            return profile;
        });
        when(userAttributeRepository.existsByUserId(7L)).thenReturn(false);
        when(userRouteProgressRepository.findByUserIdAndRouteId(7L, 3L)).thenReturn(Optional.empty());
        when(routeChapterRepository.findByRouteIdOrderByChapterNoAsc(3L)).thenReturn(List.of(chapter));
        when(routeLevelRepository.findByChapterIdOrderByLevelNoAsc(11L)).thenReturn(List.of(level));

        ProfileSaveResponse response = profileService.saveProfile(new ProfileRequest(
                "JOB_INTERVIEW",
                "准备后端实习",
                "3个月",
                20.0,
                "Redis 阶段",
                "GAME_NARRATOR",
                null
        ));

        assertThat(response.profileId()).isEqualTo(21L);
        assertThat(response.attributeInitialized()).isTrue();
        assertThat(response.routeProgressInitialized()).isTrue();
        verify(userAttributeRepository).save(any(UserAttributeEntity.class));
        verify(userRouteProgressRepository).save(any(UserRouteProgressEntity.class));
    }

    @Test
    void getCurrentProfileReturnsRouteName() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);
        profile.setGoalType(GoalType.JOB_INTERVIEW);
        profile.setCurrentGoal("准备后端实习");
        profile.setWeeklyPlanHours(BigDecimal.valueOf(20.0));
        profile.setFeedbackStyle(FeedbackStyle.GAME_NARRATOR);
        profile.setRouteId(3L);
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(userProfileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(growthRouteRepository.findById(3L)).thenReturn(Optional.of(route(3L, "后端实习路线", GoalType.JOB_INTERVIEW)));

        ProfileResponse response = profileService.getCurrentProfile();

        assertThat(response.completed()).isTrue();
        assertThat(response.routeName()).isEqualTo("后端实习路线");
    }

    @Test
    void saveProfileRejectsMissingDefaultRoute() {
        when(currentUserService.requireCurrentUserId()).thenReturn(7L);
        when(growthRouteRepository.findByGoalTypeAndDefaultRouteTrue(GoalType.JOB_INTERVIEW)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.saveProfile(new ProfileRequest(
                "JOB_INTERVIEW",
                "准备后端实习",
                null,
                null,
                null,
                "GAME_NARRATOR",
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private GrowthRouteEntity route(Long id, String name, GoalType goalType) {
        GrowthRouteEntity route = new GrowthRouteEntity();
        route.setId(id);
        route.setRouteName(name);
        route.setGoalType(goalType);
        route.setStatus(RecordStatus.ACTIVE);
        route.setDefaultRoute(true);
        return route;
    }
}
