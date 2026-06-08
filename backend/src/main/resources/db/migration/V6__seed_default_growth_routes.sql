INSERT INTO growth_route (
    route_code,
    route_name,
    goal_type,
    description,
    is_default,
    status
) VALUES
    ('STUDY_EXAM_DEFAULT', '学习考试路线', 'STUDY_EXAM', '面向考试复习和学习计划推进的默认路线', TRUE, 'ACTIVE'),
    ('JOB_INTERVIEW_DEFAULT', '后端实习路线', 'JOB_INTERVIEW', '面向实习、校招和技术面试准备的默认路线', TRUE, 'ACTIVE'),
    ('HEALTHY_LIFE_DEFAULT', '健康生活路线', 'HEALTHY_LIFE', '面向作息、运动和生活节奏修复的默认路线', TRUE, 'ACTIVE'),
    ('GENERAL_GROWTH_DEFAULT', '综合成长路线', 'GENERAL_GROWTH', '面向学习、执行力、生活平衡的综合成长路线', TRUE, 'ACTIVE'),
    ('CUSTOM_DEFAULT', '自定义成长路线', 'CUSTOM', '面向自定义目标的默认兜底路线', TRUE, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    route_name = VALUES(route_name),
    goal_type = VALUES(goal_type),
    description = VALUES(description),
    is_default = VALUES(is_default),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP(6);
