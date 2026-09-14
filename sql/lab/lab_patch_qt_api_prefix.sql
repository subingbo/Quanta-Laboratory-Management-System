-- Existing databases: rename QT menu perms from system:* to qt:* to match /qt/* APIs.
-- Safe to run more than once.

UPDATE sys_menu SET perms = REPLACE(perms, 'system:activity:', 'qt:activity:') WHERE perms LIKE 'system:activity:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:book:', 'qt:book:') WHERE perms LIKE 'system:book:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:workstation:', 'qt:workstation:') WHERE perms LIKE 'system:workstation:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:item:', 'qt:item:') WHERE perms LIKE 'system:item:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:order:', 'qt:order:') WHERE perms LIKE 'system:order:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:payment-config:', 'qt:payment-config:') WHERE perms LIKE 'system:payment-config:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:borrow:', 'qt:borrow:') WHERE perms LIKE 'system:borrow:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:reservation:', 'qt:reservation:') WHERE perms LIKE 'system:reservation:%';
UPDATE sys_menu SET perms = REPLACE(perms, 'system:signup:', 'qt:signup:') WHERE perms LIKE 'system:signup:%';
