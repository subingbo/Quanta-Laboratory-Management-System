-- Web 管理端：合并活动报名入口，并向所有后台管理角色开放只读名单。
-- 可重复执行。

UPDATE sys_menu
SET menu_name = '旧活动报名名单', visible = '1', remark = '已由统一活动报名入口替代'
WHERE menu_id = 4005;

UPDATE sys_menu
SET menu_name = '活动报名',
    path = 'activity-signups',
    component = 'activity-signups/index',
    perms = 'qt:signup:list',
    visible = '0',
    status = '0',
    remark = '统一活动报名名单'
WHERE menu_id = 4018;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 4000
FROM sys_role
WHERE role_key IN ('admin', 'ceo', 'qt_mgmt', 'qt_manager');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 4018
FROM sys_role
WHERE role_key IN ('admin', 'ceo', 'qt_mgmt', 'qt_manager');
