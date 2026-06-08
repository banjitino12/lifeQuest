package com.lifequest.auth.security;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifequest.attribute.repository.AttributeChangeRepository;
import com.lifequest.common.enums.AccountStatus;
import com.lifequest.attribute.repository.UserAttributeRepository;
import com.lifequest.dailylog.repository.DailyLogRepository;
import com.lifequest.gamification.repository.GameEventRepository;
import com.lifequest.profile.repository.UserProfileRepository;
import com.lifequest.route.repository.GrowthRouteRepository;
import com.lifequest.route.repository.RouteChapterRepository;
import com.lifequest.route.repository.RouteLevelRepository;
import com.lifequest.route.repository.UserRouteProgressRepository;
import com.lifequest.scoring.repository.DailyScoreRepository;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void registerEndpointIsPublic() throws Exception {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "tiantian",
                                  "email": "tiantian@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("CREATED")))
                .andExpect(jsonPath("$.data.userId", is(1)))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void protectedEndpointUsesCurrentUserFromJwt() throws Exception {
        UserEntity user = activeUser(8L, "life_player");
        user.setEmail("life@example.com");
        user.setCreatedAt(LocalDateTime.of(2026, 6, 8, 10, 0));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        String accessToken = jwtTokenService.generateAccessToken(8L, "life_player");

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.data.id", is(8)))
                .andExpect(jsonPath("$.data.username", is("life_player")))
                .andExpect(jsonPath("$.data.email", is("life@example.com")));
    }

    @Test
    void loginEndpointReturnsTokensForValidPassword() throws Exception {
        UserEntity user = activeUser(3L, "tiantian");
        user.setEmail("tiantian@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByUsername("tiantian@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("tiantian@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "tiantian@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("OK")))
                .andExpect(jsonPath("$.data.userId", is(3)))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    private UserEntity activeUser(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPasswordHash("unused");
        return user;
    }
}
