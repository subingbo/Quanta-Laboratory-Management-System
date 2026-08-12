# 实验室模块 SQL 说明

## 文件清单

| 文件 | 作用 |
|------|------|
| [`lab_init.sql`](lab_init.sql) | 表结构 + `sys_user` 扩展字段 + 面试轮次 + 服装订单触发器（可重复执行） |
| [`lab_seed.sql`](lab_seed.sql) | 测试数据（可重复执行，按 `create_by='lab_seed'` / 业务键幂等） |
| [`lab_full.sql`](lab_full.sql) | `lab_init` + `lab_seed` 一键合并脚本 |
| [`lab_patch_sys_user.sql`](lab_patch_sys_user.sql) | 仅补跑 `sys_user` 扩展字段（旧库兼容） |
| [`lab_patch_register.sql`](lab_patch_register.sql) | 开启新生自助注册开关 `sys.account.registerUser=true` |

## 执行顺序（新库）

```bash
# 1. RuoYi 基础库
mysql -uroot -p --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS \`ry-vue\` DEFAULT CHARACTER SET utf8mb4;"
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/ry_20260417.sql

# 2. 可选：定时任务
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/quartz.sql

# 3. 实验室结构 + 测试数据（二选一）
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_full.sql
# 或分步：
# mysql ... < sql/lab/lab_init.sql
# mysql ... < sql/lab/lab_seed.sql
```

> `lab_init.sql` 含 `DELIMITER` 触发器定义，请优先用官方 `mysql` 客户端执行；部分 GUI 对 `DELIMITER` 支持不完整。

## 已有库只补结构

```bash
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_init.sql
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_seed.sql
```

若只缺用户扩展字段：

```bash
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < sql/lab/lab_patch_sys_user.sql
```

## 与代码对齐的表

- `sys_user` 扩展：`member_no` / `member_department` / `member_title` / `member_cohort` / `student_no` / `class_name` / `is_quanta_member`
- 工位：`qt_workstation`、`qt_workstation_reservation`
- 活动：`qt_activity`、`qt_activity_signup`
- 服装：`qt_clothing_item`、`qt_payment_config`、`qt_clothing_order`
- 图书：`qt_book`、`qt_book_borrow`
- 面试：`qt_interview_application`、`qt_interview_profile`、`qt_interview_round`、`qt_interview_result`

字段命名与 `ruoyi-qt` 模块 Mapper/实体一致（`qt_` 前缀）。

## 测试账号（密码均为 `admin123`）

| 账号 | 身份 | 说明 |
|------|------|------|
| `admin` | 塔员 | `is_quanta_member=1`，成员编号 Q001 |
| `ry` | 新生 | `is_quanta_member=0` |
| `qt_member` | 塔员 | 有工位预约/借书/服装订单种子 |
| `qt_fresh` | 新生 | 有活动报名 + 面试投递/一面通过种子 |

`lab_seed.sql` / `lab_patch_register.sql` 会将 `sys.account.registerUser` 设为 `true`，允许新生在小程序端自助注册（塔员仍需后台开通）。

## 种子数据覆盖

- 3 个工位（含 1 个停用）+ 1 条预约
- 2 个已发布活动 + 1 个草稿 + 报名记录
- 2 款塔服 + 2 个付款码配置 + 1 条已提交订单
- 3 本图书 + 1 条借阅
- 面试轮次一面/二面/终面 + 新生投递 + 一面 PASS / 二面 WAITING
