-- =============================================================================
-- Backfill sys_user.member_department for applicants who already received
-- an offer email but still have an empty department (idempotent).
-- Existing production DBs must run this once: initdb does not replay on old volumes.
-- CONVERT() avoids utf8mb4_general_ci vs utf8mb4_0900_ai_ci mix on live MySQL 8.
-- =============================================================================

SET NAMES utf8mb4;

UPDATE sys_user u
INNER JOIN qt_interview_application a ON a.user_id = u.user_id
SET
    u.member_department = CONVERT(a.offered_department USING utf8mb4),
    u.is_quanta_member = '1',
    u.update_time = NOW()
WHERE CONVERT(a.notice_status USING utf8mb4) = 'SENT'
  AND CONVERT(a.apply_status USING utf8mb4) = 'OFFERED'
  AND a.offered_department IS NOT NULL
  AND CONVERT(a.offered_department USING utf8mb4) <> ''
  AND (u.member_department IS NULL OR CONVERT(u.member_department USING utf8mb4) = '');
