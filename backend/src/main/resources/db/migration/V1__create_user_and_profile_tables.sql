CREATE TABLE `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128) NULL,
    phone VARCHAR(32) NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_phone UNIQUE (phone),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(64) NOT NULL,
    current_goal VARCHAR(255) NOT NULL,
    goal_period VARCHAR(64) NULL,
    weekly_plan_hours DECIMAL(5, 2) NULL,
    current_stage VARCHAR(512) NULL,
    feedback_style VARCHAR(64) NOT NULL,
    route_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_profile_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_profile_weekly_plan_hours CHECK (weekly_plan_hours IS NULL OR weekly_plan_hours >= 0),
    CONSTRAINT chk_user_profile_goal_type CHECK (goal_type IN (
        'STUDY_EXAM',
        'JOB_INTERVIEW',
        'HEALTHY_LIFE',
        'GENERAL_GROWTH',
        'CUSTOM'
    )),
    CONSTRAINT chk_user_profile_feedback_style CHECK (feedback_style IN (
        'CALM_COACH',
        'GENTLE_COMPANION',
        'SHARP_SUPERVISOR',
        'GAME_NARRATOR',
        'GALGAME_CHARACTER'
    )),
    INDEX idx_user_profile_route_id (route_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
