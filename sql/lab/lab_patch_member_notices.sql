-- =============================================================================
-- Replace RuoYi demo notices with short Quanta display notices (idempotent).
-- Existing production DBs must run this once: initdb does not replay on old volumes.
-- =============================================================================

SET NAMES utf8mb4;

DELETE FROM sys_notice_read;
DELETE FROM sys_notice;

INSERT INTO sys_notice (notice_title, notice_type, notice_content, status, create_by, create_time, remark) VALUES
('本周例会通知', '1', '本周五晚 19:30 在实验室开例会，请各部门准时参加。', '0', 'admin', NOW(), '展示数据'),
('招新进度同步', '2', '本轮招新二面进行中，请面试官及时提交评分和录用决定。', '0', 'admin', NOW(), '展示数据'),
('实验室使用提醒', '1', '离开实验室请随手关灯、锁门，设备使用后放回原位。', '0', 'admin', NOW(), '展示数据');
