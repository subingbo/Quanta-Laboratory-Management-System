# 实验室模块初始化说明

## 1. 启动基础依赖

在项目根目录执行：

```bash
docker compose up -d
```

默认会启动：

- `ruoyi-mysql`（端口 `3306`）
- `ruoyi-redis`（端口 `6379`）

## 2. 初始化数据库

`docker-compose.yml` 已挂载 `./sql` 到 MySQL 初始化目录，首次启动空数据卷时会自动执行。

如果你是已有数据卷，手动执行以下 SQL：

1. `sql/ry_20260417.sql`（RuoYi 基础库）
2. `sql/quartz.sql`（定时任务）
3. `sql/lab/lab_init.sql`（实验室扩展）

## 3. 后端环境变量

可选环境变量如下（不配置则使用默认值）：

- `MYSQL_HOST`（默认 `localhost`）
- `MYSQL_PORT`（默认 `3306`）
- `MYSQL_DATABASE`（默认 `ry-vue`）
- `MYSQL_USER`（默认 `root`）
- `MYSQL_PASSWORD`（默认 `password`）
- `REDIS_HOST`（默认 `localhost`）
- `REDIS_PORT`（默认 `6379`）
- `REDIS_PASSWORD`（默认空）
- `RUOYI_PROFILE`（默认 `D:/ruoyi/uploadPath`）

## 4. 上传目录说明

实验室服装功能涉及以下上传文件，均复用 RuoYi `/common/upload`：

- 服装效果图
- 固定付款码图片
- 用户付款成功截图

这些文件由 `ruoyi.profile` 指向的目录承载。建议在部署环境中将 `RUOYI_PROFILE` 指向独立持久化目录。

## 5. 数据模型说明

`lab_init.sql` 包含：

- `sys_user` 扩展字段：`member_no`、`member_department`、`member_title`、`member_cohort`
- 工位预约：`qt_workstation`、`qt_workstation_reservation`
- 活动管理：`qt_activity`、`qt_activity_signup`
- 服装订购：`qt_clothing_item`、`qt_payment_config`、`qt_clothing_order`
- 图书借阅：`qt_book`、`qt_book_borrow`
- 所有新建业务表均统一包含：`create_time`、`update_time`、`create_by`、`update_by`

并包含订单状态触发器：提交/审核前必须存在付款截图，满足“先上传付款截图再提交订购”的约束。
