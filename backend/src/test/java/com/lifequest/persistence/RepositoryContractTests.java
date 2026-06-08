package com.lifequest.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.llm.repository.LlmFeedbackRepository;
import com.lifequest.llm.repository.TomorrowTaskRepository;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.route.repository.UserRouteProgressRepository;
import com.lifequest.scoring.repository.DailyScoreRepository;
import com.lifequest.weekly.repository.WeeklyReportRepository;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class RepositoryContractTests {

    @Test
    void userScopedRepositoriesExposeUserIdQueries() {
        assertHasMethodStartingWith(UserProfileRepository.class, "findByUserId");
        assertHasMethodStartingWith(DailyLogRepository.class, "findByUserId");
        assertHasMethodStartingWith(DailyScoreRepository.class, "findByUserId");
        assertHasMethodStartingWith(UserAttributeRepository.class, "findByUserId");
        assertHasMethodStartingWith(AttributeChangeRepository.class, "findByUserId");
        assertHasMethodStartingWith(GameEventRepository.class, "findByUserId");
        assertHasMethodStartingWith(UserRouteProgressRepository.class, "findByUserId");
        assertHasMethodStartingWith(LlmFeedbackRepository.class, "findByUserId");
        assertHasMethodStartingWith(TomorrowTaskRepository.class, "findByUserId");
        assertHasMethodStartingWith(WeeklyReportRepository.class, "findByUserId");
    }

    private static void assertHasMethodStartingWith(Class<?> repositoryClass, String prefix) {
        assertThat(Arrays.stream(repositoryClass.getDeclaredMethods()).map(Method::getName))
                .anyMatch(methodName -> methodName.startsWith(prefix));
    }
}
