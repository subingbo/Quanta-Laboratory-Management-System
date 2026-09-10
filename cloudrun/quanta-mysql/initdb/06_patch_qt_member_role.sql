-- =============================================================================
-- Quanta 塔员角色补丁（可重复执行）
-- 依赖: sql/ry_20260417.sql + sql/lab/lab_init.sql + sql/lab/lab_patch_admin.sql + sql/lab/lab_seed.sql
-- 用法: mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_patch_qt_member_role.sql
--
-- 背景:
--   lab_seed.sql 把 qt_member 绑到 role_id=2（若依默认普通角色），而 4000 段
--   Quanta 菜单只授权给角色 3/4/5。导致 qt_member 登录后 /getInfo 无任何 qt:*
--   权限，前端守卫把 /dashboard（需 qt:dashboard:stats）重定向到 /403。
--   本补丁新建「塔员」角色并授予塔员端可见菜单，把 qt_member 加绑该角色
--   （保留 role_id=2，可重复执行、不与 seed 冲突）。
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

-- 1) 新增「塔员」角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 6, '塔员', 'qt_member', 6, '1', 1, 1, '0', '0', 'admin', NOW(), 'Quanta 塔员，仅塔员端可见菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'qt_member');

-- 2) 授予塔员端可见菜单（父菜单 + 5 个 C 菜单 + 资料下载按钮）
--    4000 Quanta管理(父)        -> 菜单树可见
--    4004 控制台统计             -> qt:dashboard:stats        (/dashboard)
--    4003 学习资料               -> qt:material:list          (/learning-materials)
--    4033 资料下载               -> qt:material:download
--    4016 借阅记录               -> system:borrow:list        (/book-borrows)
--    4017 预约记录               -> system:reservation:list   (/workstations)
--    4014 塔服订单               -> system:order:list        (/clothing-orders)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 6, menu_id FROM sys_menu WHERE menu_id IN (4000, 4003, 4004, 4014, 4016, 4017, 4033);

-- 3) 把 qt_member 加绑「塔员」角色（保留原有 role_id=2，INSERT IGNORE 可重复执行）
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 6 FROM sys_user u WHERE u.user_name = 'qt_member';

SET FOREIGN_KEY_CHECKS = 1;

-- 校验：qt_member 现拥有 5 个前端菜单权限
SELECT 'qt_member_permissions' AS item,
       GROUP_CONCAT(m.perms ORDER BY m.menu_id SEPARATOR ', ') AS perms
FROM sys_user_role ur
JOIN sys_role_menu rm ON rm.role_id = ur.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
JOIN sys_user u ON u.user_id = ur.user_id
WHERE u.user_name = 'qt_member' AND m.perms <> '';
