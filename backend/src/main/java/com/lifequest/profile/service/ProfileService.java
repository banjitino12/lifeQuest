package com.lifequest.profile.service;

import com.lifequest.attribute.entity.UserAttributeEntity;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.auth.service.CurrentUserService;
import com.lifequest.common.enums.FeedbackStyle;
import com.lifequest.common.enums.GoalType;
import com.lifequest.common.enums.RecordStatus;
import com.lifequest.common.enums.RouteProgressStatus;
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
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final UserProfileRepository userProfileRepository;
    private final UserAttributeRepository userAttributeRepository;
    private final GrowthRouteRepository growthRouteRepository;
    private final RouteChapterRepository routeChapterRepository;
    private final RouteLevelRepository routeLevelRepository;
    private final UserRouteProgressRepository userRouteProgressRepository;

    public ProfileService(
            CurrentUserService currentUserService,
            UserProfileRepository userProfileRepository,
            UserAttributeRepository userAttributeRepository,
            GrowthRouteRepository growthRouteRepository,
            RouteChapterRepository routeChapterRepository,
            RouteLevelRepository routeLevelRepository,
            UserRouteProgressRepository userRouteProgressRepository
    ) {
        this.currentUserService = currentUserService;
        this.userProfileRepository = userProfileRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.growthRouteRepository = growthRouteRepository;
        this.routeChapterRepository = routeChapterRepository;
        this.routeLevelRepository = routeLevelRepository;
        this.userRouteProgressRepository = userRouteProgressRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile() {
        Long userId = currentUserService.requireCurrentUserId();
        return userProfileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> new ProfileResponse(null, null, null, null, null, null, null, null, false));
    }

    @Transactional
    public ProfileSaveResponse saveProfile(ProfileRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        GoalType goalType = parseEnum(GoalType.class, request.goalType(), "goalType");
        FeedbackStyle feedbackStyle = parseEnum(FeedbackStyle.class, request.feedbackStyle(), "feedbackStyle");
        GrowthRouteEntity route = resolveRoute(goalType, request.routeId());

        boolean firstSave = userProfileRepository.findByUserId(userId).isEmpty();
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseGet(UserProfileEntity::new);
        profile.setUserId(userId);
        profile.setGoalType(goalType);
        profile.setCurrentGoal(request.currentGoal().trim());
        profile.setGoalPeriod(normalizeOptional(request.goalPeriod()));
        profile.setWeeklyPlanHours(toBigDecimal(request.weeklyPlanHours()));
        profile.setCurrentStage(normalizeOptional(request.currentStage()));
        profile.setFeedbackStyle(feedbackStyle);
        profile.setRouteId(route.getId());

        UserProfileEntity savedProfile = userProfileRepository.save(profile);
        boolean attributeInitialized = initializeAttributesIfNeeded(userId);
        boolean routeProgressInitialized = initializeRouteProgressIfNeeded(userId, route);

        return new ProfileSaveResponse(
                savedProfile.getId(),
                firstSave && attributeInitialized,
                firstSave && routeProgressInitialized
        );
    }

    private GrowthRouteEntity resolveRoute(GoalType goalType, Long routeId) {
        GrowthRouteEntity route = routeId == null
                ? growthRouteRepository.findByGoalTypeAndDefaultRouteTrue(goalType)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到默认成长路线"))
                : growthRouteRepository.findById(routeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "成长路线不存在"));
        if (route.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "成长路线不可用");
        }
        return route;
    }

    private boolean initializeAttributesIfNeeded(Long userId) {
        if (userAttributeRepository.existsByUserId(userId)) {
            return false;
        }
        UserAttributeEntity attribute = new UserAttributeEntity();
        attribute.setUserId(userId);
        userAttributeRepository.save(attribute);
        return true;
    }

    private boolean initializeRouteProgressIfNeeded(Long userId, GrowthRouteEntity route) {
        if (userRouteProgressRepository.findByUserIdAndRouteId(userId, route.getId()).isPresent()) {
            return false;
        }
        UserRouteProgressEntity progress = new UserRouteProgressEntity();
        progress.setUserId(userId);
        progress.setRouteId(route.getId());
        progress.setStatus(RouteProgressStatus.IN_PROGRESS);
        routeChapterRepository.findByRouteIdOrderByChapterNoAsc(route.getId())
                .stream()
                .findFirst()
                .ifPresent(chapter -> setCurrentRouteNode(progress, chapter));
        userRouteProgressRepository.save(progress);
        return true;
    }

    private void setCurrentRouteNode(UserRouteProgressEntity progress, RouteChapterEntity chapter) {
        progress.setCurrentChapterId(chapter.getId());
        routeLevelRepository.findByChapterIdOrderByLevelNoAsc(chapter.getId())
                .stream()
                .findFirst()
                .map(RouteLevelEntity::getId)
                .ifPresent(progress::setCurrentLevelId);
    }

    private ProfileResponse toResponse(UserProfileEntity profile) {
        String routeName = Optional.ofNullable(profile.getRouteId())
                .flatMap(growthRouteRepository::findById)
                .map(GrowthRouteEntity::getRouteName)
                .orElse(null);
        return new ProfileResponse(
                profile.getGoalType().name(),
                profile.getCurrentGoal(),
                profile.getGoalPeriod(),
                profile.getWeeklyPlanHours() == null ? null : profile.getWeeklyPlanHours().doubleValue(),
                profile.getCurrentStage(),
                profile.getFeedbackStyle().name(),
                profile.getRouteId(),
                routeName,
                true
        );
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.trim());
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " 不支持该取值");
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
