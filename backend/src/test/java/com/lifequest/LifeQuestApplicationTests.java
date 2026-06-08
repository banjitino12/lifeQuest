package com.lifequest;

import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.route.repository.GrowthRouteRepository;
import com.lifequest.route.repository.RouteChapterRepository;
import com.lifequest.route.repository.RouteLevelRepository;
import com.lifequest.route.repository.UserRouteProgressRepository;
import com.lifequest.scoring.repository.DailyScoreRepository;
import com.lifequest.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LifeQuestApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @MockBean
    private UserAttributeRepository userAttributeRepository;

    @MockBean
    private AttributeChangeRepository attributeChangeRepository;

    @MockBean
    private GrowthRouteRepository growthRouteRepository;

    @MockBean
    private RouteChapterRepository routeChapterRepository;

    @MockBean
    private RouteLevelRepository routeLevelRepository;

    @MockBean
    private UserRouteProgressRepository userRouteProgressRepository;

    @MockBean
    private DailyLogRepository dailyLogRepository;

    @MockBean
    private DailyScoreRepository dailyScoreRepository;

    @MockBean
    private GameEventRepository gameEventRepository;

    @Test
    void contextLoads() {
    }
}
