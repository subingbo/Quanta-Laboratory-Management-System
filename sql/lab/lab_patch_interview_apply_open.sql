-- =============================================================================
-- Close freshman interview to new applications. Existing applicants can still
-- update. Idempotent. Existing production DBs must run this once: initdb does
-- not replay on old volumes.
-- After changing this key in param settings, refresh the config cache.
-- =============================================================================

SET NAMES utf8mb4;

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'interview-apply-open', 'qt.interview.applyOpen', 'false', 'Y', 'lab_patch', NOW(),
       'true: allow new apply; false: existing applicants can still update'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'qt.interview.applyOpen');

UPDATE sys_config
SET config_value = 'false', update_by = 'lab_patch', update_time = NOW()
WHERE config_key = 'qt.interview.applyOpen';
