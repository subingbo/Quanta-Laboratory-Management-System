-- =============================================================================
-- Production read-only aggregations. COUNT / GROUP BY only.
-- Do NOT select real names, student numbers, emails, photos, or resume text.
-- Run as a read-only session: SET SESSION transaction_read_only = ON;
-- =============================================================================

SET NAMES utf8mb4;
SET SESSION transaction_read_only = ON;

-- Application total (not user total)
SELECT COUNT(*) AS application_count
FROM qt_interview_application;

-- By apply status
SELECT apply_status, COUNT(*) AS cnt
FROM qt_interview_application
GROUP BY apply_status
ORDER BY cnt DESC;

-- New applications per day (no PII)
SELECT DATE(create_time) AS apply_date, COUNT(*) AS cnt
FROM qt_interview_application
GROUP BY DATE(create_time)
ORDER BY apply_date;

-- Login success / fail (status: 0 success, 1 fail)
SELECT status, COUNT(*) AS cnt
FROM sys_logininfor
GROUP BY status;

-- Logins per day
SELECT DATE(login_time) AS login_date, status, COUNT(*) AS cnt
FROM sys_logininfor
GROUP BY DATE(login_time), status
ORDER BY login_date DESC
LIMIT 60;

-- Access metrics: last 24h global windows (uri='*' is site-wide)
SELECT COUNT(*) AS global_windows,
       ROUND(AVG(qps), 2) AS avg_qps,
       MAX(qps) AS max_qps,
       SUM(error_4xx) AS err4,
       SUM(error_5xx) AS err5
FROM qt_access_metric
WHERE uri = '*'
  AND snapshot_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Hot URIs (no query string, no accounts)
SELECT uri, method,
       SUM(request_count) AS requests,
       SUM(error_4xx) AS err4,
       SUM(error_5xx) AS err5
FROM qt_access_metric
WHERE uri <> '*'
  AND snapshot_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY uri, method
ORDER BY requests DESC
LIMIT 20;
