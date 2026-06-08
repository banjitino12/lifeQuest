package com.lifequest.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifequest.attribute.entity.AttributeChangeEntity;
import com.lifequest.attribute.entity.UserAttributeEntity;
import com.lifequest.common.enums.AccountStatus;
import com.lifequest.common.enums.DailyLogSourceType;
import com.lifequest.common.enums.DailyRating;
import com.lifequest.common.enums.FeedbackStyle;
import com.lifequest.common.enums.GameEventType;
import com.lifequest.common.enums.GeneratedBy;
import com.lifequest.common.enums.GoalType;
import com.lifequest.common.enums.LlmFeedbackStatus;
import com.lifequest.common.enums.LlmFeedbackType;
import com.lifequest.common.enums.RecordStatus;
import com.lifequest.common.enums.RouteProgressStatus;
import com.lifequest.common.enums.TomorrowTaskStatus;
import com.lifequest.common.enums.TomorrowTaskType;
import com.lifequest.common.enums.WeeklyReportStatus;
import com.lifequest.dailylog.entity.DailyLogEntity;
import com.lifequest.gamification.entity.GameEventEntity;
import com.lifequest.llm.entity.LlmFeedbackEntity;
import com.lifequest.llm.entity.TomorrowTaskEntity;
import com.lifequest.profile.entity.UserProfileEntity;
import com.lifequest.route.entity.GrowthRouteEntity;
import com.lifequest.route.entity.RouteChapterEntity;
import com.lifequest.route.entity.RouteLevelEntity;
import com.lifequest.route.entity.UserRouteProgressEntity;
import com.lifequest.scoring.entity.DailyScoreEntity;
import com.lifequest.user.entity.UserEntity;
import com.lifequest.weekly.entity.WeeklyReportEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JpaEntityMappingTests {

    private static final Map<Class<?>, String> ENTITY_TABLES = Map.ofEntries(
            Map.entry(UserEntity.class, "`user`"),
            Map.entry(UserProfileEntity.class, "user_profile"),
            Map.entry(DailyLogEntity.class, "daily_log"),
            Map.entry(DailyScoreEntity.class, "daily_score"),
            Map.entry(UserAttributeEntity.class, "user_attribute"),
            Map.entry(AttributeChangeEntity.class, "attribute_change"),
            Map.entry(GameEventEntity.class, "game_event"),
            Map.entry(GrowthRouteEntity.class, "growth_route"),
            Map.entry(RouteChapterEntity.class, "route_chapter"),
            Map.entry(RouteLevelEntity.class, "route_level"),
            Map.entry(UserRouteProgressEntity.class, "user_route_progress"),
            Map.entry(LlmFeedbackEntity.class, "llm_feedback"),
            Map.entry(TomorrowTaskEntity.class, "tomorrow_task"),
            Map.entry(WeeklyReportEntity.class, "weekly_report")
    );

    private static final Class<?>[] USER_SCOPED_ENTITIES = {
            UserProfileEntity.class,
            DailyLogEntity.class,
            DailyScoreEntity.class,
            UserAttributeEntity.class,
            AttributeChangeEntity.class,
            GameEventEntity.class,
            UserRouteProgressEntity.class,
            LlmFeedbackEntity.class,
            TomorrowTaskEntity.class,
            WeeklyReportEntity.class
    };

    @Test
    void requestedTablesAreMappedAsJpaEntities() {
        ENTITY_TABLES.forEach((entityClass, tableName) -> {
            assertThat(entityClass.getAnnotation(Entity.class)).isNotNull();
            assertThat(entityClass.getAnnotation(Table.class).name()).isEqualTo(tableName);
        });
    }

    @Test
    void userScopedBusinessEntitiesExposeUserIdColumn() throws NoSuchFieldException {
        for (Class<?> entityClass : USER_SCOPED_ENTITIES) {
            Field userId = entityClass.getDeclaredField("userId");

            assertThat(userId.getType()).isEqualTo(Long.class);
            assertThat(userId.getAnnotation(Column.class).name()).isEqualTo("user_id");
            assertThat(userId.getAnnotation(Column.class).nullable()).isFalse();
        }
    }

    @Test
    void keyColumnsMatchMigrationNaming() throws NoSuchFieldException {
        assertColumn(DailyLogEntity.class, "taskCompletionRate", "task_completion_rate");
        assertColumn(DailyLogEntity.class, "sourceType", "source_type");
        assertColumn(DailyScoreEntity.class, "dailyLogId", "daily_log_id");
        assertColumn(DailyScoreEntity.class, "reasonJson", "reason_json");
        assertColumn(AttributeChangeEntity.class, "focusDelta", "focus_delta");
        assertColumn(AttributeChangeEntity.class, "reasonJson", "reason_json");
        assertColumn(GameEventEntity.class, "eventType", "event_type");
        assertColumn(LlmFeedbackEntity.class, "feedbackType", "feedback_type");
        assertColumn(TomorrowTaskEntity.class, "sourceDailyLogId", "source_daily_log_id");
        assertColumn(WeeklyReportEntity.class, "weekStartDate", "week_start_date");
    }

    @Test
    void enumValuesMatchMigrationChecks() {
        assertEnumValues(AccountStatus.class, "ACTIVE", "DISABLED");
        assertEnumValues(GoalType.class, "STUDY_EXAM", "JOB_INTERVIEW", "HEALTHY_LIFE", "GENERAL_GROWTH", "CUSTOM");
        assertEnumValues(
                FeedbackStyle.class,
                "CALM_COACH",
                "GENTLE_COMPANION",
                "SHARP_SUPERVISOR",
                "GAME_NARRATOR",
                "GALGAME_CHARACTER"
        );
        assertEnumValues(DailyLogSourceType.class, "FORM", "NATURAL_LANGUAGE", "MIXED");
        assertEnumValues(DailyRating.class, "S", "A", "B", "C", "D", "E");
        assertEnumValues(GameEventType.class, "ENEMY", "BUFF", "DEBUFF", "ACHIEVEMENT", "STORY", "ROUTE");
        assertEnumValues(LlmFeedbackType.class, "PARSE_LOG", "DAILY_FEEDBACK", "TOMORROW_TASKS", "WEEKLY_REPORT", "STORY");
        assertEnumValues(LlmFeedbackStatus.class, "PENDING", "SUCCESS", "FAILED", "FALLBACK");
        assertEnumValues(TomorrowTaskType.class, "MAIN", "SIDE", "DEFENSE");
        assertEnumValues(TomorrowTaskStatus.class, "TODO", "DONE", "SKIPPED");
        assertEnumValues(GeneratedBy.class, "LLM", "RULE");
        assertEnumValues(RecordStatus.class, "ACTIVE", "DISABLED");
        assertEnumValues(RouteProgressStatus.class, "IN_PROGRESS", "COMPLETED");
        assertEnumValues(WeeklyReportStatus.class, "GENERATING", "GENERATED", "FALLBACK", "FAILED");
    }

    private static void assertColumn(Class<?> entityClass, String fieldName, String columnName)
            throws NoSuchFieldException {
        Field field = entityClass.getDeclaredField(fieldName);
        assertThat(field.getAnnotation(Column.class).name()).isEqualTo(columnName);
    }

    private static <E extends Enum<E>> void assertEnumValues(Class<E> enumClass, String... values) {
        assertThat(Arrays.stream(enumClass.getEnumConstants()).map(Enum::name))
                .containsExactly(values);
    }
}
