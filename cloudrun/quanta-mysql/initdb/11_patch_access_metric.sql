-- =============================================================================
-- 访问指标表 + 管理端菜单（可重复执行）
-- 已有生产库必须手动执行一次：initdb 不会对旧数据目录重放。
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

CREATE TABLE IF NOT EXISTS qt_access_metric (
  id             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  snapshot_time  datetime     NOT NULL COMMENT '窗口结束时间',
  window_ms      int          NOT NULL DEFAULT 60000 COMMENT '窗口长度毫秒',
  uri            varchar(255) NOT NULL COMMENT '路径模板，* 表示全局',
  method         varchar(16)  NOT NULL DEFAULT '*' COMMENT 'HTTP 方法，* 表示全局',
  request_count  int          NOT NULL DEFAULT 0 COMMENT '请求数',
  error_4xx      int          NOT NULL DEFAULT 0 COMMENT 'HTTP 4xx',
  error_5xx      int          NOT NULL DEFAULT 0 COMMENT 'HTTP 5xx',
  avg_cost_ms    int          NOT NULL DEFAULT 0 COMMENT '平均耗时毫秒',
  max_cost_ms    int          NOT NULL DEFAULT 0 COMMENT '最大耗时毫秒',
  qps            decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '窗口 QPS',
  in_flight_max  int          DEFAULT NULL COMMENT '窗口内最大并发',
  jvm_used_mb    int          DEFAULT NULL COMMENT 'JVM 已用堆 MB',
  jvm_max_mb     int          DEFAULT NULL COMMENT 'JVM 最大堆 MB',
  db_active      int          DEFAULT NULL COMMENT 'Druid 活跃连接',
  db_max         int          DEFAULT NULL COMMENT 'Druid 最大连接',
  PRIMARY KEY (id),
  KEY idx_snapshot_time (snapshot_time),
  KEY idx_uri_time (uri, snapshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问指标分钟快照';

INSERT IGNORE INTO sys_menu VALUES
(4019, '访问指标', 4000, 15, 'metrics', 'qt/metrics/index', '', '', 1, 0, 'C', '0', '0', 'qt:metrics:list', 'monitor', 'admin', NOW(), '', NULL, 'QPS/延迟/错误率');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 4019);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 4019);

SET FOREIGN_KEY_CHECKS = 1;
