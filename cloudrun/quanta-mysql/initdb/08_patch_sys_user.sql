-- 补跑 sys_user 扩展字段（适用于已执行过旧版 lab_init.sql 的数据库）
-- 可重复执行；与 lab_init.sql 第 1 节逻辑一致

SET NAMES utf8mb4;
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
