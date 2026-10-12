-- =============================================================================
-- Widen interview coding experience text (idempotent)
-- Portal textarea had no maxlength; VARCHAR(500) made POST /qt/interview/apply
-- throw Data truncation and surface as an unknown 500.
-- =============================================================================

SET NAMES utf8mb4;

SET @db = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'qt_interview_profile'
      AND COLUMN_NAME = 'coding_experience_desc' AND DATA_TYPE = 'varchar') > 0,
    'ALTER TABLE qt_interview_profile MODIFY COLUMN coding_experience_desc TEXT NULL COMMENT ''编程经历补充说明''',
    'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
