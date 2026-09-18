-- =============================================================================
-- Quanta 管理端补丁（可重复执行）
-- 依赖: sql/ry_20260417.sql + sql/lab/lab_init.sql
-- 用法: mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_patch_admin.sql
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

-- 1) sys_user.major
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'major') = 0,
    'ALTER TABLE sys_user ADD COLUMN major VARCHAR(64) NULL COMMENT ''专业''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2) 活动类型
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_activity' AND COLUMN_NAME = 'activity_type') = 0,
    'ALTER TABLE qt_activity ADD COLUMN activity_type VARCHAR(16) NOT NULL DEFAULT ''GENERAL'' COMMENT ''活动类型(LECTURE/SHARING/GENERAL)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3) 图书类型
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_book' AND COLUMN_NAME = 'book_type') = 0,
    'ALTER TABLE qt_book ADD COLUMN book_type VARCHAR(32) NULL COMMENT ''图书类型''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 4) 订单确认审计
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_clothing_order' AND COLUMN_NAME = 'confirmed_by') = 0,
    'ALTER TABLE qt_clothing_order ADD COLUMN confirmed_by VARCHAR(64) NULL COMMENT ''确认收款人''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_clothing_order' AND COLUMN_NAME = 'confirmed_at') = 0,
    'ALTER TABLE qt_clothing_order ADD COLUMN confirmed_at DATETIME NULL COMMENT ''确认收款时间''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 5) 投递扩展列
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'offered_department') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN offered_department VARCHAR(16) NULL COMMENT ''录用部门''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'join_status') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN join_status VARCHAR(16) NULL COMMENT ''入职确认(PENDING/ACCEPTED/DECLINED)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'final_status') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN final_status VARCHAR(16) NULL COMMENT ''管理员最终状态''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'notice_status') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN notice_status VARCHAR(16) NULL DEFAULT ''NONE'' COMMENT ''通知状态(NONE/SENT)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 6) 面试结果唯一键改为 申请+轮次+部门
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_result' AND INDEX_NAME = 'uk_qir_user_round') > 0,
    'ALTER TABLE qt_interview_result DROP INDEX uk_qir_user_round', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_result' AND INDEX_NAME = 'uk_qir_app_round_dept') = 0,
    'ALTER TABLE qt_interview_result ADD UNIQUE KEY uk_qir_app_round_dept (application_id, round_id, department)', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 7) 届次
CREATE TABLE IF NOT EXISTS qt_cohort (
    cohort_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '届次ID',
    cohort_name VARCHAR(32) NOT NULL COMMENT '届次名称，如 21st',
    cohort_year INT NULL COMMENT '年份',
    is_current CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否当前届(0否 1是)',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/ARCHIVED)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (cohort_id),
    UNIQUE KEY uk_qt_cohort_name (cohort_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室届次';

CREATE TABLE IF NOT EXISTS qt_member_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员届次档案ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    cohort_id BIGINT NOT NULL COMMENT '届次ID',
    role_category VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'CEO/MGMT/MANAGER/INTERN/MEMBER',
    member_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESIGNED/RETAINED',
    join_time DATETIME NULL COMMENT '加入时间',
    retain_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否已确认留任(0否 1是)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_qmr_user_cohort (user_id, cohort_id),
    KEY idx_qmr_cohort_status (cohort_id, member_status),
    CONSTRAINT fk_qmr_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT fk_qmr_cohort FOREIGN KEY (cohort_id) REFERENCES qt_cohort(cohort_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员届次档案';

CREATE TABLE IF NOT EXISTS qt_interview_evaluation (
    evaluation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '面评ID',
    application_id BIGINT NOT NULL COMMENT '投递ID',
    round_id BIGINT NOT NULL COMMENT '轮次ID',
    department VARCHAR(16) NOT NULL COMMENT '面试部门',
    evaluator_user_id BIGINT NOT NULL COMMENT '评价人',
    content TEXT NOT NULL COMMENT '面评内容',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (evaluation_id),
    UNIQUE KEY uk_qie_app_round_dept_eval (application_id, round_id, department, evaluator_user_id),
    CONSTRAINT fk_qie_app FOREIGN KEY (application_id) REFERENCES qt_interview_application(application_id),
    CONSTRAINT fk_qie_round FOREIGN KEY (round_id) REFERENCES qt_interview_round(round_id),
    CONSTRAINT fk_qie_eval FOREIGN KEY (evaluator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招新面评';

CREATE TABLE IF NOT EXISTS qt_material (
    material_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '资料ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '字节大小',
    category VARCHAR(64) NULL COMMENT '分类',
    visibility VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'ALL/MEMBER/DEPT',
    uploader_id BIGINT NOT NULL COMMENT '上传者',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (material_id),
    KEY idx_qm_category (category),
    CONSTRAINT fk_qm_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资料';

-- 8) 角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 3, 'CEO', 'ceo', 3, '1', 1, 1, '0', '0', 'admin', NOW(), 'Quanta CEO 全量权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'ceo');
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 4, '管理层', 'qt_mgmt', 4, '1', 1, 1, '0', '0', 'admin', NOW(), 'Quanta 管理层，成员只读，招新本部门'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'qt_mgmt');
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 5, '经理层', 'qt_manager', 5, '3', 1, 1, '0', '0', 'admin', NOW(), 'Quanta 经理层，仅本部门二面面评'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'qt_manager');

-- 9) 菜单（4000 段）
INSERT IGNORE INTO sys_menu VALUES
(4000, 'Quanta管理', 0, 5, 'quanta', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', NOW(), '', NULL, 'Quanta 实验室管理'),
(4001, '成员管理', 4000, 1, 'member', 'qt/member/index', '', '', 1, 0, 'C', '0', '0', 'qt:member:list', 'user', 'admin', NOW(), '', NULL, '成员名单'),
(4002, '招新管理', 4000, 2, 'interview', 'qt/interview/index', '', '', 1, 0, 'C', '0', '0', 'qt:interview:admin:list', 'education', 'admin', NOW(), '', NULL, '招新候选人'),
(4003, '学习资料', 4000, 3, 'material', 'qt/material/index', '', '', 1, 0, 'C', '0', '0', 'qt:material:list', 'documentation', 'admin', NOW(), '', NULL, '学习资料'),
(4004, '控制台统计', 4000, 4, 'dashboard-stats', 'qt/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'qt:dashboard:stats', 'chart', 'admin', NOW(), '', NULL, '控制台卡片'),
(4005, '活动报名名单', 4000, 5, 'registrations', 'qt/activity/registrations', '', '', 1, 0, 'C', '0', '0', 'qt:activity:registrations', 'list', 'admin', NOW(), '', NULL, '宣讲会/分享会'),
(4006, '活动管理', 4000, 6, 'activity', 'qt/activity/index', '', '', 1, 0, 'C', '0', '0', 'qt:activity:list', 'date', 'admin', NOW(), '', NULL, '活动维护'),
(4007, '图书管理', 4000, 7, 'book', 'qt/book/index', '', '', 1, 0, 'C', '0', '0', 'qt:book:list', 'education', 'admin', NOW(), '', NULL, '图书维护'),
(4008, '工位管理', 4000, 8, 'workstation', 'qt/workstation/index', '', '', 1, 0, 'C', '0', '0', 'qt:workstation:list', 'tree', 'admin', NOW(), '', NULL, '工位维护'),
(4009, '塔服款式', 4000, 9, 'item', 'qt/item/index', '', '', 1, 0, 'C', '0', '0', 'qt:item:list', 'shopping', 'admin', NOW(), '', NULL, '服装款式'),
(4014, '塔服订单', 4000, 10, 'order', 'qt/order/index', '', '', 1, 0, 'C', '0', '0', 'qt:order:list', 'money', 'admin', NOW(), '', NULL, '服装订单'),
(4015, '付款码配置', 4000, 11, 'payment-config', 'qt/payment/index', '', '', 1, 0, 'C', '0', '0', 'qt:payment-config:list', 'qrcode', 'admin', NOW(), '', NULL, '付款码'),
(4016, '借阅记录', 4000, 12, 'borrow', 'qt/borrow/index', '', '', 1, 0, 'C', '0', '0', 'qt:borrow:list', 'log', 'admin', NOW(), '', NULL, '借阅'),
(4017, '预约记录', 4000, 13, 'reservation', 'qt/reservation/index', '', '', 1, 0, 'C', '0', '0', 'qt:reservation:list', 'time', 'admin', NOW(), '', NULL, '工位预约'),
(4018, '报名记录', 4000, 14, 'signup', 'qt/signup/index', '', '', 1, 0, 'C', '0', '0', 'qt:signup:list', 'form', 'admin', NOW(), '', NULL, '活动报名'),
(4010, '成员留任', 4001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:member:retain', '#', 'admin', NOW(), '', NULL, ''),
(4011, '成员删除', 4001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:member:remove', '#', 'admin', NOW(), '', NULL, ''),
(4012, '重置密码', 4001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:member:resetPwd', '#', 'admin', NOW(), '', NULL, ''),
(4013, '成员导入', 4001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:member:import', '#', 'admin', NOW(), '', NULL, ''),
(4020, '招新查询', 4002, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:interview:admin:query', '#', 'admin', NOW(), '', NULL, ''),
(4021, '招新面评', 4002, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:interview:admin:evaluate', '#', 'admin', NOW(), '', NULL, ''),
(4022, '招新录用', 4002, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:interview:admin:offer', '#', 'admin', NOW(), '', NULL, ''),
(4023, '招新导出', 4002, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:interview:admin:export', '#', 'admin', NOW(), '', NULL, ''),
(4030, '资料上传', 4003, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:material:add', '#', 'admin', NOW(), '', NULL, ''),
(4031, '资料修改', 4003, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:material:edit', '#', 'admin', NOW(), '', NULL, ''),
(4032, '资料删除', 4003, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:material:remove', '#', 'admin', NOW(), '', NULL, ''),
(4033, '资料下载', 4003, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:material:download', '#', 'admin', NOW(), '', NULL, ''),
(4040, '活动新增', 4006, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:activity:add', '#', 'admin', NOW(), '', NULL, ''),
(4041, '活动修改', 4006, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:activity:edit', '#', 'admin', NOW(), '', NULL, ''),
(4042, '活动删除', 4006, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:activity:remove', '#', 'admin', NOW(), '', NULL, ''),
(4043, '活动导出', 4006, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:activity:export', '#', 'admin', NOW(), '', NULL, ''),
(4050, '图书新增', 4007, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:book:add', '#', 'admin', NOW(), '', NULL, ''),
(4051, '图书修改', 4007, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:book:edit', '#', 'admin', NOW(), '', NULL, ''),
(4052, '图书删除', 4007, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:book:remove', '#', 'admin', NOW(), '', NULL, ''),
(4053, '图书导出', 4007, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:book:export', '#', 'admin', NOW(), '', NULL, ''),
(4060, '工位新增', 4008, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:workstation:add', '#', 'admin', NOW(), '', NULL, ''),
(4061, '工位修改', 4008, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:workstation:edit', '#', 'admin', NOW(), '', NULL, ''),
(4062, '工位删除', 4008, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:workstation:remove', '#', 'admin', NOW(), '', NULL, ''),
(4070, '款式新增', 4009, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:item:add', '#', 'admin', NOW(), '', NULL, ''),
(4071, '款式修改', 4009, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:item:edit', '#', 'admin', NOW(), '', NULL, ''),
(4072, '款式删除', 4009, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:item:remove', '#', 'admin', NOW(), '', NULL, ''),
(4080, '确认收款', 4014, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:order:approve', '#', 'admin', NOW(), '', NULL, ''),
(4081, '订单修改', 4014, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:order:edit', '#', 'admin', NOW(), '', NULL, ''),
(4082, '订单删除', 4014, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:order:remove', '#', 'admin', NOW(), '', NULL, ''),
(4083, '订单导出', 4014, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:order:export', '#', 'admin', NOW(), '', NULL, ''),
(4090, '付款码新增', 4015, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:payment-config:add', '#', 'admin', NOW(), '', NULL, ''),
(4091, '付款码修改', 4015, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:payment-config:edit', '#', 'admin', NOW(), '', NULL, ''),
(4092, '付款码删除', 4015, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:payment-config:remove', '#', 'admin', NOW(), '', NULL, ''),
(4100, '借阅修改', 4016, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:borrow:edit', '#', 'admin', NOW(), '', NULL, ''),
(4101, '借阅删除', 4016, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:borrow:remove', '#', 'admin', NOW(), '', NULL, ''),
(4102, '借阅导出', 4016, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:borrow:export', '#', 'admin', NOW(), '', NULL, ''),
(4110, '预约修改', 4017, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:reservation:edit', '#', 'admin', NOW(), '', NULL, ''),
(4111, '预约删除', 4017, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:reservation:remove', '#', 'admin', NOW(), '', NULL, ''),
(4112, '预约导出', 4017, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:reservation:export', '#', 'admin', NOW(), '', NULL, ''),
(4120, '报名修改', 4018, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:signup:edit', '#', 'admin', NOW(), '', NULL, ''),
(4121, '报名删除', 4018, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:signup:remove', '#', 'admin', NOW(), '', NULL, ''),
(4122, '报名导出', 4018, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qt:signup:export', '#', 'admin', NOW(), '', NULL, '');

-- CEO 全部 Quanta 菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 3, menu_id FROM sys_menu WHERE menu_id >= 4000 AND menu_id < 4200;

-- 管理层：成员只读、招新本部门、活动/资料/订单/预约，无留任删除导入
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 4, menu_id FROM sys_menu WHERE menu_id IN (
  4000,4001,4002,4003,4004,4005,4006,4007,4008,4009,4014,4015,4016,4017,4018,
  4020,4021,4022,4023,4030,4031,4032,4033,
  4040,4041,4042,4043,4050,4051,4052,4053,4060,4061,4062,4070,4071,4072,
  4080,4081,4082,4083,4090,4091,4092,4100,4101,4102,4110,4111,4112,4120,4121,4122
);

-- 经理层：招新面评 + 成员只读 + 资料下载，无工位/塔服
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 5, menu_id FROM sys_menu WHERE menu_id IN (4000,4001,4002,4003,4004,4020,4021,4033);

-- 10) 字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 100, 'Quanta部门', 'qt_department', '0', 'admin', NOW(), '招新与成员部门'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_department');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 101, '招新轮次', 'qt_interview_round', '0', 'admin', NOW(), '一面/二面/终面'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_interview_round');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 102, '投递状态', 'qt_apply_status', '0', 'admin', NOW(), '招新投递状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_apply_status');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 103, '面试结果', 'qt_result_status', '0', 'admin', NOW(), 'PASS/OUT 等'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_result_status');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 104, '成员角色类别', 'qt_member_role', '0', 'admin', NOW(), 'CEO/管理层/经理/实习生/成员'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_member_role');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 105, '成员届次状态', 'qt_member_status', '0', 'admin', NOW(), '在职/卸任/留任'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_member_status');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 106, '活动类型', 'qt_activity_type', '0', 'admin', NOW(), '宣讲会/分享会/普通'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_activity_type');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 107, '资料可见范围', 'qt_material_visibility', '0', 'admin', NOW(), '全部/成员/部门'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='qt_material_visibility');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1001, 1, '后端', 'BACKEND', 'qt_department', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_department' AND dict_value='BACKEND');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1002, 2, '产品', 'PRODUCT', 'qt_department', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_department' AND dict_value='PRODUCT');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1003, 3, '设计', 'DESIGN', 'qt_department', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_department' AND dict_value='DESIGN');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1004, 4, '前端', 'FRONTEND', 'qt_department', '', 'primary', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_department' AND dict_value='FRONTEND');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1005, 5, '安卓', 'ANDROID', 'qt_department', '', 'success', 'N', '1', 'admin', NOW(), '已取消' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_department' AND dict_value='ANDROID');
UPDATE sys_dict_data SET status='1', remark='已取消' WHERE dict_type='qt_department' AND dict_value='ANDROID';

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1011, 1, '一面', '1', 'qt_interview_round', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_interview_round' AND dict_value='1');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1012, 2, '二面', '2', 'qt_interview_round', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_interview_round' AND dict_value='2');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1013, 3, '终面', '3', 'qt_interview_round', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_interview_round' AND dict_value='3');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1021, 1, '已提交', 'SUBMITTED', 'qt_apply_status', '', 'info', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_apply_status' AND dict_value='SUBMITTED');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1022, 2, '考核中', 'PROCESSING', 'qt_apply_status', '', 'warning', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_apply_status' AND dict_value='PROCESSING');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1023, 3, '已录取', 'OFFERED', 'qt_apply_status', '', 'success', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_apply_status' AND dict_value='OFFERED');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1024, 4, '未通过', 'REJECTED', 'qt_apply_status', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_apply_status' AND dict_value='REJECTED');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1031, 1, '待定', 'PENDING', 'qt_result_status', '', 'info', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_result_status' AND dict_value='PENDING');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1032, 2, '通过', 'PASS', 'qt_result_status', '', 'success', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_result_status' AND dict_value='PASS');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1033, 3, '未通过', 'FAIL', 'qt_result_status', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_result_status' AND dict_value='FAIL');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1034, 4, '等待面试', 'WAITING', 'qt_result_status', '', 'warning', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_result_status' AND dict_value='WAITING');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1035, 5, '淘汰', 'OUT', 'qt_result_status', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_result_status' AND dict_value='OUT');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1041, 1, 'CEO', 'CEO', 'qt_member_role', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_role' AND dict_value='CEO');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1042, 2, '管理层', 'MGMT', 'qt_member_role', '', 'warning', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_role' AND dict_value='MGMT');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1043, 3, '经理', 'MANAGER', 'qt_member_role', '', 'primary', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_role' AND dict_value='MANAGER');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1044, 4, '实习生', 'INTERN', 'qt_member_role', '', 'info', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_role' AND dict_value='INTERN');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1045, 5, '成员', 'MEMBER', 'qt_member_role', '', 'default', 'Y', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_role' AND dict_value='MEMBER');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1051, 1, '在职', 'ACTIVE', 'qt_member_status', '', 'success', 'Y', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_status' AND dict_value='ACTIVE');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1052, 2, '卸任', 'RESIGNED', 'qt_member_status', '', 'info', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_status' AND dict_value='RESIGNED');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1053, 3, '已留任', 'RETAINED', 'qt_member_status', '', 'primary', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_member_status' AND dict_value='RETAINED');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1061, 1, '宣讲会', 'LECTURE', 'qt_activity_type', '', 'primary', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_activity_type' AND dict_value='LECTURE');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1062, 2, '精英分享会', 'SHARING', 'qt_activity_type', '', 'success', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_activity_type' AND dict_value='SHARING');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1063, 3, '普通活动', 'GENERAL', 'qt_activity_type', '', 'default', 'Y', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_activity_type' AND dict_value='GENERAL');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1071, 1, '全部可见', 'ALL', 'qt_material_visibility', '', 'success', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_material_visibility' AND dict_value='ALL');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1072, 2, '成员可见', 'MEMBER', 'qt_material_visibility', '', 'primary', 'Y', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_material_visibility' AND dict_value='MEMBER');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1073, 3, '本部门可见', 'DEPT', 'qt_material_visibility', '', 'warning', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='qt_material_visibility' AND dict_value='DEPT');

-- 11) 换届定时任务（每年 8 月 1 日 00:00，默认暂停，上线后启用）
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT 100, 'Quanta成员换届', 'DEFAULT', 'qtCohortJob.rollover', '0 0 0 1 8 ?', '3', '1', '1', 'admin', NOW(), '每年8月1日成员换届'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'qtCohortJob.rollover');

SET FOREIGN_KEY_CHECKS = 1;
