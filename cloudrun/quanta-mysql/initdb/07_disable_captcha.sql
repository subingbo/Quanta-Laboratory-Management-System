-- 招新联调阶段关闭图形验证码
UPDATE sys_config SET config_value='false' WHERE config_key='sys.account.captchaEnabled';
