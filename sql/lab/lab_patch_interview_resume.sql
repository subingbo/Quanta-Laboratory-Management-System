-- =============================================================================
-- Interview PDF resume columns (idempotent)
-- Existing production DBs must run this once: initdb does not replay on old volumes.
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'resume_url') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN resume_url VARCHAR(255) NULL COMMENT ''PDF resume storage path''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_application' AND COLUMN_NAME = 'resume_file_name') = 0,
    'ALTER TABLE qt_interview_application ADD COLUMN resume_file_name VARCHAR(255) NULL COMMENT ''Original PDF file name''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET FOREIGN_KEY_CHECKS = 1;
