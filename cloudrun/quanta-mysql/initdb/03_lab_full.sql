-- Lab one-shot sync: schema + seed
-- mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_full.sql

-- =============================================================================
-- 实验室业务表结构初始化（可重复执行）
-- 依赖: 已执行 sql/ry_20260417.sql（至少含 sys_user）
-- 用法:
--   mysql -uroot -p ry-vue < sql/lab/lab_init.sql
-- 之后建议再执行: sql/lab/lab_seed.sql（测试数据）
-- 表清单与代码模块对齐:
--   sys_user 扩展 / qt_workstation* / qt_activity* / qt_clothing* /
--   qt_payment_config / qt_book* / qt_interview*
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 扩展 RuoYi 用户表（成员画像 + 面试字段，MySQL 幂等）
SET @db = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'member_no') = 0,
    'ALTER TABLE sys_user ADD COLUMN member_no VARCHAR(32) NULL COMMENT ''成员编号''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'member_department') = 0,
    'ALTER TABLE sys_user ADD COLUMN member_department VARCHAR(64) NULL COMMENT ''部门''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'member_title') = 0,
    'ALTER TABLE sys_user ADD COLUMN member_title VARCHAR(64) NULL COMMENT ''职称''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'member_cohort') = 0,
    'ALTER TABLE sys_user ADD COLUMN member_cohort VARCHAR(16) NULL COMMENT ''届次, 例如 20th''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'student_no') = 0,
    'ALTER TABLE sys_user ADD COLUMN student_no VARCHAR(32) NULL COMMENT ''学号''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'class_name') = 0,
    'ALTER TABLE sys_user ADD COLUMN class_name VARCHAR(64) NULL COMMENT ''班级''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'is_quanta_member') = 0,
    'ALTER TABLE sys_user ADD COLUMN is_quanta_member CHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''是否塔员(0新生 1塔员)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_member_no') = 0,
    'CREATE UNIQUE INDEX uk_sys_user_member_no ON sys_user(member_no)', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_student_no') = 0,
    'CREATE UNIQUE INDEX uk_sys_user_student_no ON sys_user(student_no)', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2) 工位与预约
CREATE TABLE IF NOT EXISTS qt_workstation (
    workstation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '工位ID',
    workstation_code VARCHAR(32) NOT NULL COMMENT '工位编号',
    location_desc VARCHAR(128) NOT NULL COMMENT '位置描述',
    capacity INT NOT NULL DEFAULT 1 COMMENT '可容纳人数',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0可用 1停用)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (workstation_id),
    UNIQUE KEY uk_qt_workstation_code (workstation_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室工位';

CREATE TABLE IF NOT EXISTS qt_workstation_reservation (
    reservation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    workstation_id BIGINT NOT NULL COMMENT '工位ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.user_id)',
    reserve_start DATETIME NOT NULL COMMENT '预约开始时间',
    reserve_end DATETIME NOT NULL COMMENT '预约结束时间',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/CANCELED/FINISHED)',
    purpose VARCHAR(255) NULL COMMENT '用途',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_id),
    KEY idx_lwr_user_time (user_id, reserve_start),
    KEY idx_lwr_workstation_time (workstation_id, reserve_start, reserve_end),
    CONSTRAINT fk_lwr_workstation FOREIGN KEY (workstation_id) REFERENCES qt_workstation(workstation_id),
    CONSTRAINT fk_lwr_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT ck_lwr_time CHECK (reserve_end > reserve_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工位预约记录';

-- 3) 活动与报名
CREATE TABLE IF NOT EXISTS qt_activity (
    activity_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    title VARCHAR(128) NOT NULL COMMENT '活动标题',
    description TEXT NULL COMMENT '活动描述',
    signup_start DATETIME NULL COMMENT '报名开始',
    signup_end DATETIME NULL COMMENT '报名截止',
    activity_start DATETIME NOT NULL COMMENT '活动开始',
    activity_end DATETIME NOT NULL COMMENT '活动结束',
    location_desc VARCHAR(128) NULL COMMENT '活动地点',
    capacity INT NULL COMMENT '名额',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态(DRAFT/PUBLISHED/CANCELED/DELETED)',
    creator_user_id BIGINT NOT NULL COMMENT '创建人',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (activity_id),
    KEY idx_la_time (activity_start, activity_end),
    CONSTRAINT fk_la_creator FOREIGN KEY (creator_user_id) REFERENCES sys_user(user_id),
    CONSTRAINT ck_la_time CHECK (activity_end > activity_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室活动';

CREATE TABLE IF NOT EXISTS qt_activity_signup (
    signup_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(16) NOT NULL DEFAULT 'APPLIED' COMMENT '状态(APPLIED/APPROVED/CANCELED/REJECTED)',
    signup_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    cancel_time DATETIME NULL COMMENT '取消时间',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (signup_id),
    UNIQUE KEY uk_las_activity_user (activity_id, user_id),
    KEY idx_las_user_time (user_id, signup_time),
    CONSTRAINT fk_las_activity FOREIGN KEY (activity_id) REFERENCES qt_activity(activity_id),
    CONSTRAINT fk_las_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名';

-- 4) 服装订购
CREATE TABLE IF NOT EXISTS qt_clothing_item (
    item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '服装款式ID',
    item_name VARCHAR(128) NOT NULL COMMENT '款式名称',
    effect_image_path VARCHAR(255) NULL COMMENT '服装效果图路径',
    color_options_json JSON NOT NULL COMMENT '可选颜色(JSON数组)',
    size_options_json JSON NOT NULL COMMENT '可选尺码(JSON数组)',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0上架 1下架)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服装配置';

CREATE TABLE IF NOT EXISTS qt_payment_config (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付配置ID',
    payment_name VARCHAR(64) NOT NULL DEFAULT '实验室服装收款码' COMMENT '支付配置名称',
    qr_image_path VARCHAR(255) NOT NULL COMMENT '收款码图片路径(后台上传)',
    enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '启用状态(0停用 1启用)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定付款码配置';

CREATE TABLE IF NOT EXISTS qt_clothing_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '服装款式ID',
    selected_color VARCHAR(32) NOT NULL COMMENT '颜色',
    selected_size VARCHAR(16) NOT NULL COMMENT '尺码',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10,2) NULL COMMENT '单价',
    total_amount DECIMAL(10,2) NULL COMMENT '总价',
    payment_config_id BIGINT NULL COMMENT '固定付款码配置ID',
    payment_proof_path VARCHAR(255) NULL COMMENT '付款截图路径',
    payment_time DATETIME NULL COMMENT '付款时间',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态(DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELED)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_lco_order_no (order_no),
    KEY idx_lco_user_time (user_id, create_time),
    CONSTRAINT fk_lco_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT fk_lco_item FOREIGN KEY (item_id) REFERENCES qt_clothing_item(item_id),
    CONSTRAINT fk_lco_payment_config FOREIGN KEY (payment_config_id) REFERENCES qt_payment_config(config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室服装订单';

DROP TRIGGER IF EXISTS trg_qt_clothing_order_submit_check;
DELIMITER $$
CREATE TRIGGER trg_qt_clothing_order_submit_check
BEFORE UPDATE ON qt_clothing_order
FOR EACH ROW
BEGIN
    IF NEW.status IN ('SUBMITTED', 'APPROVED')
       AND (NEW.payment_proof_path IS NULL OR LENGTH(TRIM(NEW.payment_proof_path)) = 0) THEN
       SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '提交或审核订单前必须上传付款截图';
    END IF;
END$$
DELIMITER ;

-- 5) 图书借阅
CREATE TABLE IF NOT EXISTS qt_book (
    book_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
    isbn VARCHAR(32) NULL COMMENT 'ISBN',
    book_name VARCHAR(128) NOT NULL COMMENT '书名',
    author VARCHAR(128) NULL COMMENT '作者',
    publisher VARCHAR(128) NULL COMMENT '出版社',
    publish_date DATE NULL COMMENT '出版日期',
    total_count INT NOT NULL DEFAULT 1 COMMENT '总库存',
    available_count INT NOT NULL DEFAULT 1 COMMENT '可借数量',
    location_desc VARCHAR(128) NULL COMMENT '存放位置',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0可借 1停借)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id),
    UNIQUE KEY uk_lb_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室图书';

CREATE TABLE IF NOT EXISTS qt_book_borrow (
    borrow_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '借阅ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    borrow_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    due_time DATETIME NOT NULL COMMENT '应还时间',
    return_time DATETIME NULL COMMENT '归还时间',
    status VARCHAR(16) NOT NULL DEFAULT 'BORROWED' COMMENT '状态(BORROWED/RETURNED/OVERDUE)',
    remark VARCHAR(255) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (borrow_id),
    KEY idx_lbb_user_time (user_id, borrow_time),
    KEY idx_lbb_book_status (book_id, status),
    CONSTRAINT fk_lbb_book FOREIGN KEY (book_id) REFERENCES qt_book(book_id),
    CONSTRAINT fk_lbb_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT ck_lbb_due_time CHECK (due_time > borrow_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书借阅记录';

-- 6) 面试投递
CREATE TABLE IF NOT EXISTS qt_interview_application (
    application_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '投递ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.user_id)',
    real_name VARCHAR(64) NOT NULL COMMENT '姓名',
    gender CHAR(1) NOT NULL COMMENT '性别(0男 1女 2未知)',
    class_name VARCHAR(64) NOT NULL COMMENT '班级',
    first_choice VARCHAR(16) NOT NULL COMMENT '第一志愿(BACKEND/PRODUCT/DESIGN/FRONTEND/ANDROID)',
    second_choice VARCHAR(16) NOT NULL COMMENT '第二志愿(BACKEND/PRODUCT/DESIGN/FRONTEND/ANDROID)',
    photo_url VARCHAR(255) NOT NULL COMMENT '证件照URL/本地访问路径',
    apply_status VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED' COMMENT '投递状态(SUBMITTED/PROCESSING/OFFERED/REJECTED)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (application_id),
    UNIQUE KEY uk_qia_user_id (user_id),
    KEY idx_qia_choice (first_choice, second_choice),
    CONSTRAINT fk_qia_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT ck_qia_choice_diff CHECK (first_choice <> second_choice)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试投递主表';

CREATE TABLE IF NOT EXISTS qt_interview_profile (
    profile_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展信息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.user_id)',
    application_id BIGINT NOT NULL COMMENT '投递ID(qt_interview_application.application_id)',
    self_intro TEXT NULL COMMENT '个人介绍',
    coding_experience CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否接触过编程(0否 1是)',
    coding_experience_desc VARCHAR(500) NULL COMMENT '编程经历补充说明',
    quanta_understanding TEXT NULL COMMENT '对Quanta的了解',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (profile_id),
    UNIQUE KEY uk_qip_user_id (user_id),
    UNIQUE KEY uk_qip_application_id (application_id),
    CONSTRAINT fk_qip_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT fk_qip_application FOREIGN KEY (application_id) REFERENCES qt_interview_application(application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试投递扩展信息';

CREATE TABLE IF NOT EXISTS qt_interview_round (
    round_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮次ID',
    round_no INT NOT NULL COMMENT '轮次序号(1,2,3...)',
    round_name VARCHAR(64) NOT NULL COMMENT '轮次名称(一面/二面/终面)',
    enabled CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用(0否 1是)',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (round_id),
    UNIQUE KEY uk_qir_round_no (round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试轮次定义';

CREATE TABLE IF NOT EXISTS qt_interview_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    application_id BIGINT NOT NULL COMMENT '投递ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.user_id)',
    round_id BIGINT NOT NULL COMMENT '轮次ID(qt_interview_round.round_id)',
    department VARCHAR(16) NOT NULL COMMENT '面试部门(BACKEND/PRODUCT/DESIGN/FRONTEND/ANDROID)',
    result_status VARCHAR(16) NOT NULL COMMENT '结果(PENDING/PASS/FAIL/WAITING)',
    score DECIMAL(5,2) NULL COMMENT '面试分数(可选)',
    feedback VARCHAR(1000) NULL COMMENT '面试评价/反馈',
    interview_time DATETIME NULL COMMENT '面试时间',
    published_time DATETIME NULL COMMENT '结果发布时间',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_qir_user_round (user_id, round_id),
    KEY idx_qir_app (application_id),
    KEY idx_qir_user_status (user_id, result_status),
    CONSTRAINT fk_qirs_app FOREIGN KEY (application_id) REFERENCES qt_interview_application(application_id),
    CONSTRAINT fk_qirs_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT fk_qirs_round FOREIGN KEY (round_id) REFERENCES qt_interview_round(round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试每轮结果';

INSERT INTO qt_interview_round (round_no, round_name, enabled, create_by, create_time)
VALUES
(1, '一面', '1', 'admin', NOW()),
(2, '二面', '1', 'admin', NOW()),
(3, '终面', '1', 'admin', NOW())
ON DUPLICATE KEY UPDATE round_name = VALUES(round_name), enabled = VALUES(enabled), update_time = NOW();

SET FOREIGN_KEY_CHECKS = 1;

-- ===== SEED =====
-- =============================================================================
-- 实验室业务测试数据（可重复执行）
-- 依赖: ry_20260417.sql + lab_init.sql
-- 用法: mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_seed.sql
--
-- 测试账号（密码均为 admin123）:
--   admin / ry / qt_member / qt_fresh
-- =============================================================================

SET NAMES utf8mb4;

-- Enable freshman self-registration for Quanta C-end
UPDATE sys_config
SET config_value = 'true', update_by = 'lab_seed', update_time = NOW()
WHERE config_key = 'sys.account.registerUser';

-- Close new interview applications; existing applicants may still update
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'interview-apply-open', 'qt.interview.applyOpen', 'false', 'Y', 'lab_seed', NOW(),
       'true: allow new apply; false: existing applicants can still update'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qt.interview.applyOpen');

UPDATE sys_config
SET config_value = 'false', update_by = 'lab_seed', update_time = NOW()
WHERE config_key = 'qt.interview.applyOpen';

SET FOREIGN_KEY_CHECKS = 0;
SET @pwd = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';

UPDATE sys_user SET member_no='Q001', member_department='BACKEND', member_title='负责人', member_cohort='20th', student_no=NULL, class_name=NULL, is_quanta_member='1', update_by='lab_seed', update_time=NOW() WHERE user_name='admin';
UPDATE sys_user SET member_no=NULL, member_department=NULL, member_title=NULL, member_cohort=NULL, student_no='2024001001', class_name='计科2401', is_quanta_member='0', update_by='lab_seed', update_time=NOW() WHERE user_name='ry';

INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,update_by,update_time,remark,member_no,member_department,member_title,member_cohort,student_no,class_name,is_quanta_member)
SELECT 103,'qt_member','塔员小王','00','member@quanta.local','13800000001','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_seed',NOW(),'',NULL,'实验室塔员测试号','Q101','FRONTEND','成员','21st',NULL,NULL,'1'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='qt_member');

INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,update_by,update_time,remark,member_no,member_department,member_title,member_cohort,student_no,class_name,is_quanta_member)
SELECT 105,'qt_fresh','新生小李','00','fresh@quanta.local','13800000002','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_seed',NOW(),'',NULL,'实验室新生测试号',NULL,NULL,NULL,NULL,'2024002002','软工2402','0'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='qt_fresh');

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2 FROM sys_user u WHERE u.user_name IN ('qt_member','qt_fresh','ry');

SET @admin_id=(SELECT user_id FROM sys_user WHERE user_name='admin' LIMIT 1);
SET @ry_id=(SELECT user_id FROM sys_user WHERE user_name='ry' LIMIT 1);
SET @member_id=(SELECT user_id FROM sys_user WHERE user_name='qt_member' LIMIT 1);
SET @fresh_id=(SELECT user_id FROM sys_user WHERE user_name='qt_fresh' LIMIT 1);

INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-A01','A区靠窗工位',1,'0','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-A01');
INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-B02','B区会议旁工位',2,'0','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-B02');
INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-C03','C区停用示例',1,'1','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-C03');
SET @ws_a=(SELECT workstation_id FROM qt_workstation WHERE workstation_code='WS-A01' LIMIT 1);
DELETE FROM qt_workstation_reservation WHERE purpose='SEED_RESERVATION' OR create_by='lab_seed';
INSERT INTO qt_workstation_reservation (workstation_id,user_id,reserve_start,reserve_end,status,purpose,create_by,create_time)
VALUES (@ws_a,@member_id,DATE_ADD(CURDATE(), INTERVAL 1 DAY)+INTERVAL 9 HOUR,DATE_ADD(CURDATE(), INTERVAL 1 DAY)+INTERVAL 12 HOUR,'APPROVED','SEED_RESERVATION','lab_seed',NOW());

DELETE FROM qt_activity_signup WHERE create_by='lab_seed';
DELETE FROM qt_activity WHERE create_by='lab_seed';
INSERT INTO qt_activity (title,description,signup_start,signup_end,activity_start,activity_end,location_desc,capacity,status,creator_user_id,create_by,create_time) VALUES
('Quanta春季招新宣讲','介绍实验室方向与招新流程',DATE_SUB(NOW(), INTERVAL 3 DAY),DATE_ADD(NOW(), INTERVAL 10 DAY),DATE_ADD(NOW(), INTERVAL 7 DAY),DATE_ADD(NOW(), INTERVAL 7 DAY)+INTERVAL 2 HOUR,'实验楼3楼大厅',120,'PUBLISHED',@admin_id,'lab_seed',NOW()),
('前端技术分享会','Vue / 小程序实战分享',DATE_SUB(NOW(), INTERVAL 1 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY)+INTERVAL 2 HOUR,'实验室A区',40,'PUBLISHED',@admin_id,'lab_seed',NOW()),
('草稿活动-不可见','未发布活动',NOW(),DATE_ADD(NOW(), INTERVAL 3 DAY),DATE_ADD(NOW(), INTERVAL 4 DAY),DATE_ADD(NOW(), INTERVAL 4 DAY)+INTERVAL 1 HOUR,'线上',20,'DRAFT',@admin_id,'lab_seed',NOW());
SET @act1=(SELECT activity_id FROM qt_activity WHERE title='Quanta春季招新宣讲' AND create_by='lab_seed' LIMIT 1);
INSERT INTO qt_activity_signup (activity_id,user_id,status,signup_time,create_by,create_time) VALUES
(@act1,@fresh_id,'APPLIED',NOW(),'lab_seed',NOW()),
(@act1,@ry_id,'APPROVED',NOW(),'lab_seed',NOW());

DELETE FROM qt_clothing_order WHERE create_by='lab_seed';
DELETE FROM qt_clothing_item WHERE create_by='lab_seed';
DELETE FROM qt_payment_config WHERE create_by='lab_seed';
INSERT INTO qt_payment_config (payment_name,qr_image_path,enabled,create_by,create_time) VALUES
('实验室微信收款码','/profile/upload/qt/payment-qr/seed-wechat.png','1','lab_seed',NOW()),
('实验室支付宝收款码','/profile/upload/qt/payment-qr/seed-alipay.png','0','lab_seed',NOW());
INSERT INTO qt_clothing_item (item_name,effect_image_path,color_options_json,size_options_json,status,create_by,create_time) VALUES
('Quanta经典短袖','/profile/upload/qt/clothing-item/seed-tee.png',JSON_ARRAY('黑色','白色','深蓝'),JSON_ARRAY('S','M','L','XL'),'0','lab_seed',NOW()),
('Quanta卫衣','/profile/upload/qt/clothing-item/seed-hoodie.png',JSON_ARRAY('灰色','黑色'),JSON_ARRAY('M','L','XL'),'0','lab_seed',NOW());
SET @pay1=(SELECT config_id FROM qt_payment_config WHERE payment_name='实验室微信收款码' AND create_by='lab_seed' LIMIT 1);
SET @item1=(SELECT item_id FROM qt_clothing_item WHERE item_name='Quanta经典短袖' AND create_by='lab_seed' LIMIT 1);
INSERT INTO qt_clothing_order (order_no,user_id,item_id,selected_color,selected_size,quantity,unit_price,total_amount,payment_config_id,payment_proof_path,payment_time,status,create_by,create_time)
VALUES ('QTSEED0001',@member_id,@item1,'黑色','L',1,79.00,79.00,@pay1,'/profile/upload/qt/payment-proof/seed-proof.png',NOW(),'SUBMITTED','lab_seed',NOW());

DELETE FROM qt_book_borrow WHERE create_by='lab_seed';
DELETE FROM qt_book WHERE create_by='lab_seed';
INSERT INTO qt_book (isbn,book_name,author,publisher,publish_date,total_count,available_count,location_desc,status,create_by,create_time) VALUES
('9787111544937','深入理解计算机系统','Randal E. Bryant','机械工业出版社','2016-01-01',3,2,'书架A-01','0','lab_seed',NOW()),
('9787115428028','JavaScript高级程序设计','Nicholas C. Zakas','人民邮电出版社','2019-01-01',2,2,'书架B-03','0','lab_seed',NOW()),
('9787121367298','设计模式','GoF','电子工业出版社','2019-06-01',1,1,'书架C-02','0','lab_seed',NOW());
SET @book1=(SELECT book_id FROM qt_book WHERE isbn='9787111544937' LIMIT 1);
INSERT INTO qt_book_borrow (book_id,user_id,borrow_time,due_time,return_time,status,remark,create_by,create_time)
VALUES (@book1,@member_id,NOW(),DATE_ADD(NOW(), INTERVAL 14 DAY),NULL,'BORROWED','种子借阅','lab_seed',NOW());

INSERT INTO qt_interview_round (round_no,round_name,enabled,create_by,create_time) VALUES
(1,'一面','1','lab_seed',NOW()),(2,'二面','1','lab_seed',NOW()),(3,'终面','1','lab_seed',NOW())
ON DUPLICATE KEY UPDATE round_name=VALUES(round_name),enabled=VALUES(enabled),update_time=NOW();

DELETE FROM qt_interview_result WHERE create_by='lab_seed';
DELETE FROM qt_interview_profile WHERE create_by='lab_seed';
DELETE FROM qt_interview_application WHERE create_by='lab_seed';
INSERT INTO qt_interview_application (user_id,real_name,gender,class_name,first_choice,second_choice,photo_url,apply_status,create_by,create_time)
VALUES (@fresh_id,'新生小李','1','软工2402','FRONTEND','BACKEND','/profile/upload/qt/interview-photo/seed-fresh.jpg','PROCESSING','lab_seed',NOW());
SET @app_id=(SELECT application_id FROM qt_interview_application WHERE user_id=@fresh_id LIMIT 1);
SET @round1=(SELECT round_id FROM qt_interview_round WHERE round_no=1 LIMIT 1);
SET @round2=(SELECT round_id FROM qt_interview_round WHERE round_no=2 LIMIT 1);
INSERT INTO qt_interview_profile (user_id,application_id,self_intro,coding_experience,coding_experience_desc,quanta_understanding,create_by,create_time)
VALUES (@fresh_id,@app_id,'热爱前端与产品体验，希望加入Quanta。','1','自学过Vue与小程序基础','Quanta是把想法做成作品的实验室。','lab_seed',NOW());
INSERT INTO qt_interview_result (application_id,user_id,round_id,department,result_status,score,feedback,interview_time,published_time,create_by,create_time) VALUES
(@app_id,@fresh_id,@round1,'FRONTEND','PASS',86.50,'基础扎实，表达清晰',DATE_SUB(NOW(), INTERVAL 2 DAY),DATE_SUB(NOW(), INTERVAL 1 DAY),'lab_seed',NOW()),
(@app_id,@fresh_id,@round2,'FRONTEND','WAITING',NULL,'二面待安排',NULL,NOW(),'lab_seed',NOW());

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'sys_user_members' AS item, COUNT(*) AS cnt FROM sys_user WHERE is_quanta_member='1' AND del_flag='0'
UNION ALL SELECT 'qt_workstation', COUNT(*) FROM qt_workstation
UNION ALL SELECT 'qt_activity_published', COUNT(*) FROM qt_activity WHERE status='PUBLISHED'
UNION ALL SELECT 'qt_clothing_item', COUNT(*) FROM qt_clothing_item
UNION ALL SELECT 'qt_payment_config', COUNT(*) FROM qt_payment_config
UNION ALL SELECT 'qt_book', COUNT(*) FROM qt_book
UNION ALL SELECT 'qt_interview_application', COUNT(*) FROM qt_interview_application
UNION ALL SELECT 'qt_interview_result', COUNT(*) FROM qt_interview_result;