CREATE TABLE growth_route (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_code VARCHAR(64) NOT NULL,
    route_name VARCHAR(128) NOT NULL,
    goal_type VARCHAR(64) NOT NULL,
    description VARCHAR(512) NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_growth_route_code UNIQUE (route_code),
    CONSTRAINT chk_growth_route_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_growth_route_goal_type CHECK (goal_type IN (
        'STUDY_EXAM',
        'JOB_INTERVIEW',
        'HEALTHY_LIFE',
        'GENERAL_GROWTH',
        'CUSTOM'
    )),
    INDEX idx_growth_route_goal_type (goal_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

ALTER TABLE user_profile
    ADD CONSTRAINT fk_user_profile_route
        FOREIGN KEY (route_id) REFERENCES growth_route (id) ON DELETE SET NULL;

CREATE TABLE route_chapter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_id BIGINT NOT NULL,
    chapter_no INT NOT NULL,
    chapter_name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    target_keywords VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_route_chapter_route FOREIGN KEY (route_id) REFERENCES growth_route (id) ON DELETE CASCADE,
    CONSTRAINT uk_route_chapter_no UNIQUE (route_id, chapter_no),
    CONSTRAINT chk_route_chapter_no CHECK (chapter_no > 0),
    INDEX idx_route_chapter_route_id (route_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE route_level (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    level_no INT NOT NULL,
    level_name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    completion_rule_json JSON NULL,
    exp_reward INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_route_level_route FOREIGN KEY (route_id) REFERENCES growth_route (id) ON DELETE CASCADE,
    CONSTRAINT fk_route_level_chapter FOREIGN KEY (chapter_id) REFERENCES route_chapter (id) ON DELETE CASCADE,
    CONSTRAINT uk_route_level_no UNIQUE (chapter_id, level_no),
    CONSTRAINT chk_route_level_no CHECK (level_no > 0),
    CONSTRAINT chk_route_level_exp_reward CHECK (exp_reward >= 0),
    INDEX idx_route_level_route_id (route_id),
    INDEX idx_route_level_chapter_id (chapter_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_route_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    current_chapter_id BIGINT NULL,
    current_level_id BIGINT NULL,
    progress_percent DECIMAL(5, 2) NOT NULL DEFAULT 0,
    completed_level_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_user_route_progress_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_route_progress_route FOREIGN KEY (route_id) REFERENCES growth_route (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_route_progress_chapter FOREIGN KEY (current_chapter_id) REFERENCES route_chapter (id) ON DELETE SET NULL,
    CONSTRAINT fk_user_route_progress_level FOREIGN KEY (current_level_id) REFERENCES route_level (id) ON DELETE SET NULL,
    CONSTRAINT uk_user_route_progress UNIQUE (user_id, route_id),
    CONSTRAINT chk_user_route_progress_percent CHECK (progress_percent >= 0 AND progress_percent <= 100),
    CONSTRAINT chk_user_route_progress_completed_count CHECK (completed_level_count >= 0),
    CONSTRAINT chk_user_route_progress_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    INDEX idx_user_route_progress_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
