-- =============================================================================
-- Quanta debug seed (ASCII-only, re-runnable). Part B: workstation/activity/book/clothing/material
-- Run after Part A. All rows tagged create_by='lab_debug'.
-- =============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @admin_id=(SELECT user_id FROM sys_user WHERE user_name='admin');

-- 5) workstations (10 new) + reservations (12)
INSERT INTO qt_workstation (workstation_code,location_desc,capacity,status,create_by,create_time,remark) VALUES
('WS-A02','A window seat 2',1,'0','lab_debug',NOW(),'debug'),
('WS-A03','A window seat 3',2,'0','lab_debug',NOW(),'debug'),
('WS-A04','A window seat 4',1,'0','lab_debug',NOW(),'debug'),
('WS-A05','A window seat 5',1,'0','lab_debug',NOW(),'debug'),
('WS-B03','B meeting seat 3',3,'0','lab_debug',NOW(),'debug'),
('WS-B04','B meeting seat 4',2,'0','lab_debug',NOW(),'debug'),
('WS-B05','B meeting seat 5',4,'0','lab_debug',NOW(),'debug'),
('WS-D01','D silent pod 1',1,'0','lab_debug',NOW(),'debug'),
('WS-D02','D silent pod 2',1,'0','lab_debug',NOW(),'debug'),
('WS-E01','E discussion zone',6,'1','lab_debug',NOW(),'debug-disabled');

INSERT INTO qt_workstation_reservation (workstation_id,user_id,reserve_start,reserve_end,status,purpose,create_by,create_time)
SELECT w.workstation_id,u.user_id,
  DATE_ADD(CURDATE(),INTERVAL (u.user_id%7) DAY)+INTERVAL 9 HOUR,
  DATE_ADD(CURDATE(),INTERVAL (u.user_id%7) DAY)+INTERVAL 12 HOUR,
  CASE (u.user_id%4) WHEN 0 THEN 'APPROVED' WHEN 1 THEN 'PENDING' WHEN 2 THEN 'FINISHED' ELSE 'CANCELED' END,
  CONCAT(u.nick_name,' debug reservation'),
  'lab_debug',NOW()
FROM qt_workstation w
JOIN sys_user u ON u.user_name=CASE w.workstation_code
  WHEN 'WS-A02' THEN 'qt_member' WHEN 'WS-A03' THEN 'qt_m102' WHEN 'WS-A04' THEN 'qt_m103'
  WHEN 'WS-A05' THEN 'qt_m104' WHEN 'WS-B03' THEN 'qt_m105' WHEN 'WS-B04' THEN 'qt_m106'
  WHEN 'WS-B05' THEN 'qt_m107' WHEN 'WS-D01' THEN 'qt_m108' WHEN 'WS-D02' THEN 'qt_m109'
  WHEN 'WS-A01' THEN 'qt_m110' WHEN 'WS-B02' THEN 'qt_m111' ELSE 'qt_m112' END
WHERE w.create_by='lab_debug' OR w.workstation_code IN ('WS-A01','WS-B02');

-- 6) activities (10 new) + signups (~12)
INSERT INTO qt_activity (title,description,signup_start,signup_end,activity_start,activity_end,location_desc,capacity,activity_type,status,creator_user_id,create_by,create_time) VALUES
('Quanta Autumn Recruitment Talk','Lab directions and recruitment flow',DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(NOW(),INTERVAL 8 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY)+INTERVAL 2 HOUR,'Hall 3F',120,'LECTURE','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Vue3 Composition API','Composition API and project structure',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 4 DAY),DATE_ADD(NOW(),INTERVAL 3 DAY),DATE_ADD(NOW(),INTERVAL 3 DAY)+INTERVAL 2 HOUR,'Lab A',40,'SHARING','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Spring Boot Intro','Backend microservice starter',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY),DATE_ADD(NOW(),INTERVAL 5 DAY),DATE_ADD(NOW(),INTERVAL 5 DAY)+INTERVAL 2 HOUR,'Lab B',30,'SHARING','PUBLISHED',@admin_id,'lab_debug',NOW()),
('UI Design Workshop','Figma and design specs',NOW(),DATE_ADD(NOW(),INTERVAL 10 DAY),DATE_ADD(NOW(),INTERVAL 9 DAY),DATE_ADD(NOW(),INTERVAL 9 DAY)+INTERVAL 3 HOUR,'Design studio',20,'SHARING','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Android Jetpack','Modern Android dev',DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_ADD(NOW(),INTERVAL 3 DAY),DATE_ADD(NOW(),INTERVAL 2 DAY),DATE_ADD(NOW(),INTERVAL 2 DAY)+INTERVAL 2 HOUR,'Lab A',35,'SHARING','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Product Thinking','From idea to delivery',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 7 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY)+INTERVAL 2 HOUR,'Room 1',25,'SHARING','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Algorithm Basics','DS and algo intro',DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_ADD(NOW(),INTERVAL 2 DAY),DATE_ADD(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 1 DAY)+INTERVAL 2 HOUR,'Hall 3F',100,'LECTURE','PUBLISHED',@admin_id,'lab_debug',NOW()),
('Team Building','End-of-term team building',DATE_ADD(NOW(),INTERVAL 5 DAY),DATE_ADD(NOW(),INTERVAL 12 DAY),DATE_ADD(NOW(),INTERVAL 14 DAY),DATE_ADD(NOW(),INTERVAL 14 DAY)+INTERVAL 4 HOUR,'Playground',50,'GENERAL','PUBLISHED',@admin_id,'lab_debug',NOW()),
('DRAFT Tech Stack','unpublished draft',NOW(),DATE_ADD(NOW(),INTERVAL 3 DAY),DATE_ADD(NOW(),INTERVAL 4 DAY),DATE_ADD(NOW(),INTERVAL 4 DAY)+INTERVAL 1 HOUR,'online',15,'GENERAL','DRAFT',@admin_id,'lab_debug',NOW()),
('CANCELED Exchange','canceled sample',DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)+INTERVAL 2 HOUR,'other school',30,'LECTURE','CANCELED',@admin_id,'lab_debug',NOW());

-- signups: 3 users per published activity
INSERT IGNORE INTO qt_activity_signup (activity_id,user_id,status,signup_time,create_by,create_time)
SELECT a.activity_id,u.user_id,
  CASE (u.user_id%3) WHEN 0 THEN 'APPROVED' WHEN 1 THEN 'APPLIED' ELSE 'CANCELED' END,
  DATE_SUB(NOW(),INTERVAL (u.user_id%4) DAY),'lab_debug',NOW()
FROM qt_activity a
JOIN sys_user u ON u.user_name IN
  ('qt_s110','qt_s111','qt_s112','qt_s113','qt_s114','qt_s115','qt_s116','qt_s117','qt_s118','qt_s119','qt_s120','qt_s121','qt_fresh','ry')
WHERE a.create_by='lab_debug' AND a.status='PUBLISHED';

-- 7) books (10 new) + borrows (12)
INSERT INTO qt_book (isbn,book_name,author,publisher,publish_date,total_count,available_count,location_desc,book_type,status,create_by,create_time) VALUES
('9787111558422','Effective Java','Joshua Bloch','Mech Industry','2019-01-01',3,2,'Shelf A-02','tech','0','lab_debug',NOW()),
('9787544291170','Grokking Algorithms','Aditya Bhargava','PT Press','2018-04-01',2,1,'Shelf A-03','tech','0','lab_debug',NOW()),
('9787121367304','Refactoring','Martin Fowler','EP Press','2019-03-01',2,2,'Shelf B-01','tech','0','lab_debug',NOW()),
('9787508686579','Principles','Ray Dalio','CITIC','2018-01-01',1,0,'Shelf B-02','mgmt','0','lab_debug',NOW()),
('9787115415349','HTTP Illustrated','Ueno','PT Press','2016-05-01',2,1,'Shelf C-01','tech','0','lab_debug',NOW()),
('9787121369810','Clean Code','Robert C. Martin','EP Press','2020-01-01',2,2,'Shelf C-03','tech','0','lab_debug',NOW()),
('9787115460518','Deep Node.js','Piao Ling','PT Press','2013-12-01',1,1,'Shelf D-01','tech','0','lab_debug',NOW()),
('9787521732039','Thinking Fast Slow','Kahneman','CITIC','2021-06-01',1,1,'Shelf D-02','mgmt','0','lab_debug',NOW()),
('9787111646861','MySQL Must Know','Ben Forta','Mech Industry','2020-09-01',2,1,'Shelf E-01','tech','0','lab_debug',NOW()),
('9787115546081','Design Psychology','Donald Norman','PT Press','2020-10-01',1,0,'Shelf E-02','design','1','lab_debug',NOW());

INSERT INTO qt_book_borrow (book_id,user_id,borrow_time,due_time,return_time,status,remark,create_by,create_time)
SELECT b.book_id,u.user_id,
  DATE_SUB(NOW(),INTERVAL (b.book_id%30) DAY),
  DATE_SUB(NOW(),INTERVAL (b.book_id%30)-14 DAY),
  CASE WHEN b.book_id%4=0 THEN DATE_SUB(NOW(),INTERVAL (b.book_id%30)-5 DAY) ELSE NULL END,
  CASE WHEN b.book_id%4=0 THEN 'RETURNED' WHEN b.book_id%7=0 THEN 'OVERDUE' ELSE 'BORROWED' END,
  CONCAT(u.nick_name,' debug borrow'),'lab_debug',NOW()
FROM qt_book b
JOIN sys_user u ON u.user_name=CASE b.book_id
  WHEN 1 THEN 'qt_member' WHEN 2 THEN 'qt_m102' WHEN 3 THEN 'qt_m103' WHEN 4 THEN 'qt_m104'
  WHEN 5 THEN 'qt_m105' WHEN 6 THEN 'qt_m106' WHEN 7 THEN 'qt_m107' WHEN 8 THEN 'qt_m108'
  WHEN 9 THEN 'qt_m109' WHEN 10 THEN 'qt_m110' WHEN 11 THEN 'qt_m111' WHEN 12 THEN 'qt_m112' ELSE 'qt_m113' END
WHERE b.create_by='lab_debug';

-- 8) clothing (1 new item + 1 payment) + 12 orders (with payment proof for trigger)
INSERT INTO qt_clothing_item (item_name,effect_image_path,color_options_json,size_options_json,status,create_by,create_time) VALUES
('Quanta Windbreaker','/profile/upload/qt/clothing-item/debug-coat.png',JSON_ARRAY('black','khaki'),JSON_ARRAY('M','L','XL'),'0','lab_debug',NOW());
INSERT INTO qt_payment_config (payment_name,qr_image_path,enabled,create_by,create_time) VALUES
('Campus Card QR','/profile/upload/qt/payment-qr/debug-card.png','1','lab_debug',NOW());

INSERT INTO qt_clothing_order (order_no,user_id,item_id,selected_color,selected_size,quantity,unit_price,total_amount,payment_config_id,payment_proof_path,payment_time,status,confirmed_by,confirmed_at,create_by,create_time)
SELECT CONCAT('QTDBG',LPAD(u.user_id,6,'0')),
  u.user_id,
  CASE (u.user_id%3) WHEN 0 THEN 1 WHEN 1 THEN 2 ELSE 3 END,
  CASE (u.user_id%3) WHEN 0 THEN 'black' WHEN 1 THEN 'gray' ELSE 'khaki' END,
  CASE (u.user_id%4) WHEN 0 THEN 'M' WHEN 1 THEN 'L' WHEN 2 THEN 'XL' ELSE 'S' END,
  1,
  CASE (u.user_id%3) WHEN 0 THEN 79.00 WHEN 1 THEN 129.00 ELSE 199.00 END,
  CASE (u.user_id%3) WHEN 0 THEN 79.00 WHEN 1 THEN 129.00 ELSE 199.00 END,
  CASE (u.user_id%2) WHEN 0 THEN 1 ELSE 3 END,
  '/profile/upload/qt/payment-proof/debug-proof.png',
  DATE_SUB(NOW(),INTERVAL (u.user_id%5) DAY),
  CASE (u.user_id%5) WHEN 0 THEN 'APPROVED' WHEN 1 THEN 'SUBMITTED' WHEN 2 THEN 'SUBMITTED' WHEN 3 THEN 'REJECTED' ELSE 'DRAFT' END,
  CASE WHEN u.user_id%5=0 THEN 'admin' ELSE NULL END,
  CASE WHEN u.user_id%5=0 THEN DATE_SUB(NOW(),INTERVAL 1 DAY) ELSE NULL END,
  'lab_debug',NOW()
FROM sys_user u WHERE u.user_name LIKE 'qt_m1__' AND u.create_by='lab_debug';

-- 9) materials (12)
INSERT INTO qt_material (file_name,stored_name,file_path,file_size,category,visibility,uploader_id,create_by,create_time,remark)
SELECT CONCAT('debug-material-',LPAD(u.user_id,3,'0'),'.pdf'),
  CONCAT('debug-',u.user_id,'.pdf'),
  CONCAT('/profile/upload/qt/material/debug-',u.user_id,'.pdf'),
  1024*(u.user_id*100),
  CASE (u.user_id%4) WHEN 0 THEN 'frontend' WHEN 1 THEN 'backend' WHEN 2 THEN 'design' ELSE 'general' END,
  CASE (u.user_id%3) WHEN 0 THEN 'ALL' WHEN 1 THEN 'MEMBER' ELSE 'DEPT' END,
  u.user_id,'lab_debug',NOW(),CONCAT(u.nick_name,' uploaded debug material')
FROM sys_user u WHERE u.user_name LIKE 'qt_m1__' AND u.create_by='lab_debug';

SET FOREIGN_KEY_CHECKS = 1;

-- validation summary
SELECT 'workstation' AS tbl, COUNT(*) AS c FROM qt_workstation
UNION ALL SELECT 'workstation_reservation', COUNT(*) FROM qt_workstation_reservation
UNION ALL SELECT 'activity', COUNT(*) FROM qt_activity
UNION ALL SELECT 'activity_signup', COUNT(*) FROM qt_activity_signup
UNION ALL SELECT 'book', COUNT(*) FROM qt_book
UNION ALL SELECT 'book_borrow', COUNT(*) FROM qt_book_borrow
UNION ALL SELECT 'clothing_item', COUNT(*) FROM qt_clothing_item
UNION ALL SELECT 'clothing_order', COUNT(*) FROM qt_clothing_order
UNION ALL SELECT 'payment_config', COUNT(*) FROM qt_payment_config
UNION ALL SELECT 'material', COUNT(*) FROM qt_material
UNION ALL SELECT 'interview_application', COUNT(*) FROM qt_interview_application
UNION ALL SELECT 'interview_result', COUNT(*) FROM qt_interview_result
UNION ALL SELECT 'member_record', COUNT(*) FROM qt_member_record
UNION ALL SELECT 'cohort', COUNT(*) FROM qt_cohort
UNION ALL SELECT 'sys_user_quanta', COUNT(*) FROM sys_user WHERE is_quanta_member='1' AND del_flag='0';
