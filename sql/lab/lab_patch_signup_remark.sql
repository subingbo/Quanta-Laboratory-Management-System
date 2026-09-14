-- =============================================================================
-- 补丁:为 qt_activity_signup 增加 remark 列
-- 背景: QtActivitySignup 继承 BaseEntity(含 remark),mapper 的 detail 查询与
--   resultMap 均映射 s.remark，但建表时漏了该列，导致宣讲会报名列表报
--   Unknown column 's.remark'。其余引用 remark 的表(qt_cohort/qt_material/
--   qt_workstation/qt_book_borrow)均已含该列，仅本表遗漏。
-- 依赖: lab_init.sql(已建表)。可重复执行。
-- =============================================================================
SET NAMES utf8mb4;
SET @db = DATABASE();
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='qt_activity_signup' AND COLUMN_NAME='remark') = 0,
    'ALTER TABLE qt_activity_signup ADD COLUMN remark VARCHAR(500) NULL COMMENT ''备注'' AFTER cancel_time',
    'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 校验
SELECT 'signup_remark_column' AS chk, COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA=@db AND TABLE_NAME='qt_activity_signup' AND COLUMN_NAME='remark';
