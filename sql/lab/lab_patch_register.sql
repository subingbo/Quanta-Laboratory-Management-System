-- Enable Quanta freshman self-registration (safe re-run)
SET NAMES utf8mb4;
UPDATE sys_config
SET config_value = 'true', update_by = 'lab_patch', update_time = NOW()
WHERE config_key = 'sys.account.registerUser';
