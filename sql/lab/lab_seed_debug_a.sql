-- =============================================================================
-- Quanta debug seed (ASCII-only, re-runnable). Part A: users + cohorts + recruitment
-- All rows tagged create_by='lab_debug'. Re-run cleans by that tag first.
-- =============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @pwd = '$2a$10$7JB720yubVSZVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2'; -- admin123

-- 0) cleanup previous lab_debug rows (keeps lab_seed originals)
DELETE FROM qt_interview_evaluation WHERE create_by='lab_debug';
DELETE FROM qt_interview_result WHERE create_by='lab_debug';
DELETE FROM qt_interview_profile WHERE create_by='lab_debug';
DELETE FROM qt_interview_application WHERE create_by='lab_debug';
DELETE FROM qt_material WHERE create_by='lab_debug';
DELETE FROM qt_book_borrow WHERE create_by='lab_debug';
DELETE FROM qt_book WHERE create_by='lab_debug';
DELETE FROM qt_clothing_order WHERE create_by='lab_debug';
DELETE FROM qt_clothing_item WHERE create_by='lab_debug';
DELETE FROM qt_payment_config WHERE create_by='lab_debug';
DELETE FROM qt_activity_signup WHERE create_by='lab_debug';
DELETE FROM qt_activity WHERE create_by='lab_debug';
DELETE FROM qt_workstation_reservation WHERE create_by='lab_debug';
DELETE FROM qt_workstation WHERE create_by='lab_debug';
DELETE FROM qt_member_record WHERE create_by='lab_debug';
DELETE FROM qt_cohort WHERE create_by='lab_debug';
DELETE ur FROM sys_user_role ur JOIN sys_user u ON u.user_id=ur.user_id WHERE u.create_by='lab_debug';
DELETE FROM sys_user WHERE create_by='lab_debug';

-- 1) cohorts
INSERT INTO qt_cohort (cohort_name,cohort_year,is_current,status,create_by,create_time,remark)
SELECT '20th',2020,'0','ARCHIVED','lab_debug',NOW(),'cohort 20'
WHERE NOT EXISTS (SELECT 1 FROM qt_cohort WHERE cohort_name='20th');
INSERT INTO qt_cohort (cohort_name,cohort_year,is_current,status,create_by,create_time,remark)
SELECT '21st',2021,'1','ACTIVE','lab_debug',NOW(),'cohort 21 current'
WHERE NOT EXISTS (SELECT 1 FROM qt_cohort WHERE cohort_name='21st');
SET @c20=(SELECT cohort_id FROM qt_cohort WHERE cohort_name='20th');
SET @c21=(SELECT cohort_id FROM qt_cohort WHERE cohort_name='21st');

-- 2) 12 quanta members Q102-Q113
INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,remark,member_no,member_department,member_title,member_cohort,is_quanta_member) VALUES
(103,'qt_m102','Chen Siyuan','00','m102@quanta.local','13900000102','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q102','BACKEND','lead','21st','1'),
(103,'qt_m103','Lin Wanqing','00','m103@quanta.local','13900000103','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q103','FRONTEND','member','21st','1'),
(103,'qt_m104','Zhao Zixuan','00','m104@quanta.local','13900000104','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q104','PRODUCT','manager','21st','1'),
(103,'qt_m105','Huang Jiayi','00','m105@quanta.local','13900000105','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q105','DESIGN','member','21st','1'),
(103,'qt_m106','Zhou Mingzhe','00','m106@quanta.local','13900000106','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q106','ANDROID','intern','21st','1'),
(103,'qt_m107','Wu Yutong','00','m107@quanta.local','13900000107','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q107','BACKEND','member','21st','1'),
(103,'qt_m108','Xu Haoran','00','m108@quanta.local','13900000108','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q108','FRONTEND','member','20th','1'),
(103,'qt_m109','Sun Yajing','00','m109@quanta.local','13900000109','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q109','PRODUCT','member','20th','1'),
(103,'qt_m110','Ma Tianyu','00','m110@quanta.local','13900000110','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q110','DESIGN','manager','20th','1'),
(103,'qt_m111','Hu Mengyao','00','m111@quanta.local','13900000111','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q111','BACKEND','member','21st','1'),
(103,'qt_m112','Guo Junjie','00','m112@quanta.local','13900000112','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q112','ANDROID','member','21st','1'),
(103,'qt_m113','Gao Xinran','00','m113@quanta.local','13900000113','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug member','Q113','FRONTEND','intern','21st','1');

INSERT INTO sys_user_role (user_id,role_id)
SELECT user_id,6 FROM sys_user WHERE user_name LIKE 'qt_m1__' AND create_by='lab_debug'
ON DUPLICATE KEY UPDATE role_id=6;

-- member records (admin, qt_member, 12 new)
INSERT INTO qt_member_record (user_id,cohort_id,role_category,member_status,join_time,retain_flag,create_by,create_time)
SELECT u.user_id,@c21,
  CASE u.user_name WHEN 'admin' THEN 'CEO' WHEN 'qt_m104' THEN 'MGMT' WHEN 'qt_m110' THEN 'MGMT'
       WHEN 'qt_m106' THEN 'INTERN' WHEN 'qt_m113' THEN 'INTERN' ELSE 'MEMBER' END,
  CASE WHEN u.user_name IN ('qt_m108','qt_m109','qt_m110') THEN 'RESIGNED' ELSE 'ACTIVE' END,
  CASE WHEN u.user_name LIKE 'qt_m10%' THEN DATE_SUB(NOW(),INTERVAL 720 DAY) ELSE DATE_SUB(NOW(),INTERVAL 360 DAY) END,
  CASE WHEN u.user_name IN ('qt_m108','qt_m109') THEN '1' ELSE '0' END,
  'lab_debug',NOW()
FROM sys_user u
WHERE u.user_name IN ('admin','qt_member') OR (u.user_name LIKE 'qt_m1__' AND u.create_by='lab_debug');

-- 3) 12 freshmen for recruitment
INSERT INTO sys_user (dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,login_ip,login_date,pwd_update_date,create_by,create_time,remark,student_no,class_name,is_quanta_member) VALUES
(105,'qt_s110','Liu Yiming','00','s110@quanta.local','13900001110','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001010','CS2401','0'),
(105,'qt_s111','Qian Simin','00','s111@quanta.local','13900001111','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001011','CS2401','0'),
(105,'qt_s112','Sun Hao','00','s112@quanta.local','13900001112','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001012','SE2402','0'),
(105,'qt_s113','Li Tingting','00','s113@quanta.local','13900001113','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001013','SE2402','0'),
(105,'qt_s114','Zhou Jie','00','s114@quanta.local','13900001114','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001014','CS2402','0'),
(105,'qt_s115','Wu Jing','00','s115@quanta.local','13900001115','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001015','CS2402','0'),
(105,'qt_s116','Zheng Kai','00','s116@quanta.local','13900001116','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001016','SE2401','0'),
(105,'qt_s117','Wang Yue','00','s117@quanta.local','13900001117','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001017','SE2401','0'),
(105,'qt_s118','Feng Lei','00','s118@quanta.local','13900001118','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001018','CS2401','0'),
(105,'qt_s119','Chen Xue','00','s119@quanta.local','13900001119','1','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001019','CS2401','0'),
(105,'qt_s120','Chu Yang','00','s120@quanta.local','13900001120','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001020','SE2402','0'),
(105,'qt_s121','Wei Ming','00','s121@quanta.local','13900001121','0','',@pwd,'0','0','127.0.0.1',NOW(),NOW(),'lab_debug',NOW(),'debug fresh','2024001021','SE2402','0');

-- 4) recruitment applications + profiles + results
INSERT INTO qt_interview_application (user_id,real_name,gender,class_name,first_choice,second_choice,photo_url,apply_status,offered_department,join_status,final_status,notice_status,create_by,create_time)
SELECT u.user_id,u.nick_name,
  CASE WHEN u.sex='0' THEN '0' ELSE '1' END, u.class_name,
  CASE (u.user_id % 5) WHEN 0 THEN 'BACKEND' WHEN 1 THEN 'FRONTEND' WHEN 2 THEN 'PRODUCT' WHEN 3 THEN 'DESIGN' ELSE 'ANDROID' END,
  CASE (u.user_id % 5) WHEN 0 THEN 'FRONTEND' WHEN 1 THEN 'BACKEND' WHEN 2 THEN 'DESIGN' WHEN 3 THEN 'PRODUCT' ELSE 'BACKEND' END,
  CONCAT('/profile/upload/qt/interview-photo/debug-',u.user_name,'.jpg'),
  CASE WHEN u.user_name IN ('qt_s110','qt_s111','qt_s112','qt_s113') THEN 'OFFERED'
       WHEN u.user_name IN ('qt_s118','qt_s119','qt_s120','qt_s121') THEN 'REJECTED' ELSE 'PROCESSING' END,
  -- offered_department must match a volunteer dept so round-2 PASS shows in the list
  CASE WHEN u.user_name IN ('qt_s110','qt_s111','qt_s112') THEN
    (CASE (u.user_id % 5) WHEN 0 THEN 'BACKEND' WHEN 1 THEN 'FRONTEND' WHEN 2 THEN 'PRODUCT' WHEN 3 THEN 'DESIGN' ELSE 'ANDROID' END)
       WHEN u.user_name = 'qt_s113' THEN
    (CASE (u.user_id % 5) WHEN 0 THEN 'FRONTEND' WHEN 1 THEN 'BACKEND' WHEN 2 THEN 'DESIGN' WHEN 3 THEN 'PRODUCT' ELSE 'BACKEND' END)
       ELSE NULL END,
  CASE WHEN u.user_name IN ('qt_s110','qt_s112') THEN 'ACCEPTED' WHEN u.user_name IN ('qt_s111','qt_s113') THEN 'PENDING' ELSE NULL END,
  CASE WHEN u.user_name IN ('qt_s110','qt_s112') THEN 'JOINED' WHEN u.user_name IN ('qt_s111','qt_s113') THEN 'PENDING' ELSE NULL END,
  CASE WHEN u.user_name IN ('qt_s110','qt_s111','qt_s112','qt_s113') THEN 'SENT' ELSE 'NONE' END,
  'lab_debug',NOW()
FROM sys_user u WHERE u.user_name LIKE 'qt_s1__' AND u.create_by='lab_debug';

INSERT INTO qt_interview_profile (user_id,application_id,self_intro,coding_experience,coding_experience_desc,quanta_understanding,create_by,create_time)
SELECT u.user_id,a.application_id,CONCAT(u.nick_name,' self intro'),
  CASE WHEN u.user_id%2=0 THEN '1' ELSE '0' END,
  CASE WHEN u.user_id%2=0 THEN 'self-taught Vue/Java' ELSE NULL END,
  'Quanta turns ideas into products.','lab_debug',NOW()
FROM sys_user u JOIN qt_interview_application a ON a.user_id=u.user_id
WHERE u.user_name LIKE 'qt_s1__' AND u.create_by='lab_debug';

SET @r1=(SELECT round_id FROM qt_interview_round WHERE round_no=1);
SET @r2=(SELECT round_id FROM qt_interview_round WHERE round_no=2);
INSERT INTO qt_interview_result (application_id,user_id,round_id,department,result_status,score,feedback,interview_time,published_time,create_by,create_time)
SELECT a.application_id,a.user_id,@r1,a.first_choice,
  CASE WHEN a.apply_status='REJECTED' THEN 'FAIL' ELSE 'PASS' END,
  ROUND(70+(a.user_id%25),2),
  CASE WHEN a.apply_status='REJECTED' THEN 'weak basics' ELSE 'solid basics, clear expression' END,
  DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY),'lab_debug',NOW()
FROM qt_interview_application a WHERE a.create_by='lab_debug';
INSERT INTO qt_interview_result (application_id,user_id,round_id,department,result_status,score,feedback,interview_time,published_time,create_by,create_time)
SELECT a.application_id,a.user_id,@r2,a.offered_department,
  CASE WHEN a.apply_status='OFFERED' THEN 'PASS' ELSE 'WAITING' END,
  CASE WHEN a.apply_status='OFFERED' THEN ROUND(80+(a.user_id%15),2) ELSE NULL END,
  CASE WHEN a.apply_status='OFFERED' THEN 'excellent round 2' ELSE 'round 2 pending' END,
  CASE WHEN a.apply_status='OFFERED' THEN DATE_SUB(NOW(),INTERVAL 2 DAY) ELSE NULL END,
  CASE WHEN a.apply_status='OFFERED' THEN DATE_SUB(NOW(),INTERVAL 1 DAY) ELSE NOW() END,
  'lab_debug',NOW()
FROM qt_interview_application a WHERE a.create_by='lab_debug' AND a.apply_status='OFFERED';

SET FOREIGN_KEY_CHECKS = 1;
