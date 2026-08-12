-- =============================================================================
-- 实验室业务测试数据（可重复执行）
-- 依赖: ry_20260417.sql + lab_init.sql
-- 用法: mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_seed.sql
--
-- 测试账号（密码均为 admin123）:
--   admin / ry / qt_member / qt_fresh
-- =============================================================================

SET NAMES utf8mb4;

-- Enable freshman self-registration for Quanta C-end
UPDATE sys_config
SET config_value = 'true', update_by = 'lab_seed', update_time = NOW()
WHERE config_key = 'sys.account.registerUser';

SET FOREIGN_KEY_CHECKS = 0;
SET @pwd = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';

UPDATE sys_user SET member_no='Q001', member_department='BACKEND', member_title='负责人', member_cohort='20th', student_no=NULL, class_name=NULL, is_quanta_member='1', update_by='lab_seed', update_time=NOW() WHERE user_name='admin';
UPDATE sys_user SET member_no=NULL, member_department=NULL, member_title=NULL, member_cohort=NULL, student_no='2024001001', class_name='计科2401', is_quanta_member='0', update_by='lab_seed', update_time=NOW() WHERE user_name='ry';

INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,update_by,update_time,remark,member_no,member_department,member_title,member_cohort,student_no,class_name,is_quanta_member)
SELECT 103,'qt_member','塔员小王','00','member@quanta.local','13800000001','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_seed',NOW(),'',NULL,'实验室塔员测试号','Q101','FRONTEND','成员','21st',NULL,NULL,'1'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='qt_member');

INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,update_by,update_time,remark,member_no,member_department,member_title,member_cohort,student_no,class_name,is_quanta_member)
SELECT 105,'qt_fresh','新生小李','00','fresh@quanta.local','13800000002','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_seed',NOW(),'',NULL,'实验室新生测试号',NULL,NULL,NULL,NULL,'2024002002','软工2402','0'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name='qt_fresh');

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 2 FROM sys_user u WHERE u.user_name IN ('qt_member','qt_fresh','ry');

SET @admin_id=(SELECT user_id FROM sys_user WHERE user_name='admin' LIMIT 1);
SET @ry_id=(SELECT user_id FROM sys_user WHERE user_name='ry' LIMIT 1);
SET @member_id=(SELECT user_id FROM sys_user WHERE user_name='qt_member' LIMIT 1);
SET @fresh_id=(SELECT user_id FROM sys_user WHERE user_name='qt_fresh' LIMIT 1);

INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-A01','A区靠窗工位',1,'0','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-A01');
INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-B02','B区会议旁工位',2,'0','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-B02');
INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark)
SELECT 'WS-C03','C区停用示例',1,'1','lab_seed',NOW(),'种子数据' WHERE NOT EXISTS (SELECT 1 FROM qt_workstation WHERE workstation_code='WS-C03');
SET @ws_a=(SELECT workstation_id FROM qt_workstation WHERE workstation_code='WS-A01' LIMIT 1);
DELETE FROM qt_workstation_reservation WHERE purpose='SEED_RESERVATION' OR create_by='lab_seed';
INSERT INTO qt_workstation_reservation (workstation_id,user_id,reserve_start,reserve_end,status,purpose,create_by,create_time)
VALUES (@ws_a,@member_id,DATE_ADD(CURDATE(), INTERVAL 1 DAY)+INTERVAL 9 HOUR,DATE_ADD(CURDATE(), INTERVAL 1 DAY)+INTERVAL 12 HOUR,'APPROVED','SEED_RESERVATION','lab_seed',NOW());

DELETE FROM qt_activity_signup WHERE create_by='lab_seed';
DELETE FROM qt_activity WHERE create_by='lab_seed';
INSERT INTO qt_activity (title,description,signup_start,signup_end,activity_start,activity_end,location_desc,capacity,status,creator_user_id,create_by,create_time) VALUES
('Quanta春季招新宣讲','介绍实验室方向与招新流程',DATE_SUB(NOW(), INTERVAL 3 DAY),DATE_ADD(NOW(), INTERVAL 10 DAY),DATE_ADD(NOW(), INTERVAL 7 DAY),DATE_ADD(NOW(), INTERVAL 7 DAY)+INTERVAL 2 HOUR,'实验楼3楼大厅',120,'PUBLISHED',@admin_id,'lab_seed',NOW()),
('前端技术分享会','Vue / 小程序实战分享',DATE_SUB(NOW(), INTERVAL 1 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY),DATE_ADD(NOW(), INTERVAL 5 DAY)+INTERVAL 2 HOUR,'实验室A区',40,'PUBLISHED',@admin_id,'lab_seed',NOW()),
('草稿活动-不可见','未发布活动',NOW(),DATE_ADD(NOW(), INTERVAL 3 DAY),DATE_ADD(NOW(), INTERVAL 4 DAY),DATE_ADD(NOW(), INTERVAL 4 DAY)+INTERVAL 1 HOUR,'线上',20,'DRAFT',@admin_id,'lab_seed',NOW());
SET @act1=(SELECT activity_id FROM qt_activity WHERE title='Quanta春季招新宣讲' AND create_by='lab_seed' LIMIT 1);
INSERT INTO qt_activity_signup (activity_id,user_id,status,signup_time,create_by,create_time) VALUES
(@act1,@fresh_id,'APPLIED',NOW(),'lab_seed',NOW()),
(@act1,@ry_id,'APPROVED',NOW(),'lab_seed',NOW());

DELETE FROM qt_clothing_order WHERE create_by='lab_seed';
DELETE FROM qt_clothing_item WHERE create_by='lab_seed';
DELETE FROM qt_payment_config WHERE create_by='lab_seed';
INSERT INTO qt_payment_config (payment_name,qr_image_path,enabled,create_by,create_time) VALUES
('实验室微信收款码','/profile/upload/qt/payment-qr/seed-wechat.png','1','lab_seed',NOW()),
('实验室支付宝收款码','/profile/upload/qt/payment-qr/seed-alipay.png','0','lab_seed',NOW());
INSERT INTO qt_clothing_item (item_name,effect_image_path,color_options_json,size_options_json,status,create_by,create_time) VALUES
('Quanta经典短袖','/profile/upload/qt/clothing-item/seed-tee.png',JSON_ARRAY('黑色','白色','深蓝'),JSON_ARRAY('S','M','L','XL'),'0','lab_seed',NOW()),
('Quanta卫衣','/profile/upload/qt/clothing-item/seed-hoodie.png',JSON_ARRAY('灰色','黑色'),JSON_ARRAY('M','L','XL'),'0','lab_seed',NOW());
SET @pay1=(SELECT config_id FROM qt_payment_config WHERE payment_name='实验室微信收款码' AND create_by='lab_seed' LIMIT 1);
SET @item1=(SELECT item_id FROM qt_clothing_item WHERE item_name='Quanta经典短袖' AND create_by='lab_seed' LIMIT 1);
INSERT INTO qt_clothing_order (order_no,user_id,item_id,selected_color,selected_size,quantity,unit_price,total_amount,payment_config_id,payment_proof_path,payment_time,status,create_by,create_time)
VALUES ('QTSEED0001',@member_id,@item1,'黑色','L',1,79.00,79.00,@pay1,'/profile/upload/qt/payment-proof/seed-proof.png',NOW(),'SUBMITTED','lab_seed',NOW());

DELETE FROM qt_book_borrow WHERE create_by='lab_seed';
DELETE FROM qt_book WHERE create_by='lab_seed';
INSERT INTO qt_book (isbn,book_name,author,publisher,publish_date,total_count,available_count,location_desc,status,create_by,create_time) VALUES
('9787111544937','深入理解计算机系统','Randal E. Bryant','机械工业出版社','2016-01-01',3,2,'书架A-01','0','lab_seed',NOW()),
('9787115428028','JavaScript高级程序设计','Nicholas C. Zakas','人民邮电出版社','2019-01-01',2,2,'书架B-03','0','lab_seed',NOW()),
('9787121367298','设计模式','GoF','电子工业出版社','2019-06-01',1,1,'书架C-02','0','lab_seed',NOW());
SET @book1=(SELECT book_id FROM qt_book WHERE isbn='9787111544937' LIMIT 1);
INSERT INTO qt_book_borrow (book_id,user_id,borrow_time,due_time,return_time,status,remark,create_by,create_time)
VALUES (@book1,@member_id,NOW(),DATE_ADD(NOW(), INTERVAL 14 DAY),NULL,'BORROWED','种子借阅','lab_seed',NOW());

INSERT INTO qt_interview_round (round_no,round_name,enabled,create_by,create_time) VALUES
(1,'一面','1','lab_seed',NOW()),(2,'二面','1','lab_seed',NOW()),(3,'终面','1','lab_seed',NOW())
ON DUPLICATE KEY UPDATE round_name=VALUES(round_name),enabled=VALUES(enabled),update_time=NOW();

DELETE FROM qt_interview_result WHERE create_by='lab_seed';
DELETE FROM qt_interview_profile WHERE create_by='lab_seed';
DELETE FROM qt_interview_application WHERE create_by='lab_seed';
INSERT INTO qt_interview_application (user_id,real_name,gender,class_name,first_choice,second_choice,photo_url,apply_status,create_by,create_time)
VALUES (@fresh_id,'新生小李','1','软工2402','FRONTEND','BACKEND','/profile/upload/qt/interview-photo/seed-fresh.jpg','PROCESSING','lab_seed',NOW());
SET @app_id=(SELECT application_id FROM qt_interview_application WHERE user_id=@fresh_id LIMIT 1);
SET @round1=(SELECT round_id FROM qt_interview_round WHERE round_no=1 LIMIT 1);
SET @round2=(SELECT round_id FROM qt_interview_round WHERE round_no=2 LIMIT 1);
INSERT INTO qt_interview_profile (user_id,application_id,self_intro,coding_experience,coding_experience_desc,quanta_understanding,create_by,create_time)
VALUES (@fresh_id,@app_id,'热爱前端与产品体验，希望加入Quanta。','1','自学过Vue与小程序基础','Quanta是把想法做成作品的实验室。','lab_seed',NOW());
INSERT INTO qt_interview_result (application_id,user_id,round_id,department,result_status,score,feedback,interview_time,published_time,create_by,create_time) VALUES
(@app_id,@fresh_id,@round1,'FRONTEND','PASS',86.50,'基础扎实，表达清晰',DATE_SUB(NOW(), INTERVAL 2 DAY),DATE_SUB(NOW(), INTERVAL 1 DAY),'lab_seed',NOW()),
(@app_id,@fresh_id,@round2,'FRONTEND','WAITING',NULL,'二面待安排',NULL,NOW(),'lab_seed',NOW());

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'sys_user_members' AS item, COUNT(*) AS cnt FROM sys_user WHERE is_quanta_member='1' AND del_flag='0'
UNION ALL SELECT 'qt_workstation', COUNT(*) FROM qt_workstation
UNION ALL SELECT 'qt_activity_published', COUNT(*) FROM qt_activity WHERE status='PUBLISHED'
UNION ALL SELECT 'qt_clothing_item', COUNT(*) FROM qt_clothing_item
UNION ALL SELECT 'qt_payment_config', COUNT(*) FROM qt_payment_config
UNION ALL SELECT 'qt_book', COUNT(*) FROM qt_book
UNION ALL SELECT 'qt_interview_application', COUNT(*) FROM qt_interview_application
UNION ALL SELECT 'qt_interview_result', COUNT(*) FROM qt_interview_result;