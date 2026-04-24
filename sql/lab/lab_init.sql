-- 实验室管理业务初始化脚本
-- 依赖: ry-vue 基础表已导入（含 sys_user）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 扩展 RuoYi 用户表（成员画像）
ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS member_no VARCHAR(32) NULL COMMENT '成员编号',
    ADD COLUMN IF NOT EXISTS member_department VARCHAR(64) NULL COMMENT '部门',
    ADD COLUMN IF NOT EXISTS member_title VARCHAR(64) NULL COMMENT '职称',
    ADD COLUMN IF NOT EXISTS member_cohort VARCHAR(16) NULL COMMENT '届次, 例如 20th';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_member_no ON sys_user(member_no);

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

SET FOREIGN_KEY_CHECKS = 1;
