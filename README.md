# Quanta 实验室管理系统（RuoYi-Vue-Qt）

基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 3.9.2 二次开发的 **Quanta 实验室** 业务系统：塔员 / 新生双入口、成员名录、活动报名、图书借阅、工位预约、塔服订购、招新面试。

| 项 | 说明 |
|---|---|
| 后端 | Spring Boot 4.x、JDK 17+、Spring Security + JWT、MyBatis、Redis |
| 管理端 | Vue 2 + Element UI（`ruoyi-ui`） |
| C 端 | 实验室移动端（`ruoyi-qt-ui`） |
| 数据库 | MySQL 8.0，库名默认 `ry-vue` |

本仓库对应 GitHub：`https://github.com/subingbo/RuoYi-Vue-Qt`

---

## 模块结构

```
RuoYi-Vue-Qt
├── ruoyi-admin      # 启动入口（默认端口 8080）
├── ruoyi-framework  # 安全、Redis、配置
├── ruoyi-system     # 用户 / 角色 / 菜单 / 字典
├── ruoyi-qt         # 实验室业务（成员、活动、图书、工位、塔服、面试）
├── ruoyi-quartz     # 定时任务
├── ruoyi-ui         # 管理端前端
├── ruoyi-qt-ui      # C 端前端
├── sql/lab          # 实验室表结构与种子数据
└── docs             # 部署说明与接口文档
```

## 业务能力

- **认证**：验证码登录；`loginType` 区分新生 / 塔员；登录与 `/getInfo` 返回成员扩展字段（编号、部门、届次等）
- **成员名录**：在职塔员列表，支持姓名 / 部门 / 届次筛选
- **活动报名**：活动发布与 C 端报名、报名明细
- **图书借阅**：库存与借还记录
- **工位预约**：工位配置与预约记录
- **塔服订购**：款式 / 付款码 / 订单与付款截图
- **招新面试**：双志愿投递、证件照、轮次结果查询

## 快速启动（后端）

环境：JDK 17+、Maven 3.8+、MySQL 8、Redis。完整步骤见 [docs/后端部署文档.md](docs/后端部署文档.md)。

```bash
# 1. 启动 MySQL / Redis（可选 Docker）
docker compose up -d

# 2. 初始化库表（顺序固定）
mysql -h127.0.0.1 -P3306 -uroot -ppassword --default-character-set=utf8mb4 ry-vue < sql/ry_20260417.sql
mysql -h127.0.0.1 -P3306 -uroot -ppassword --default-character-set=utf8mb4 ry-vue < sql/quartz.sql
mysql -h127.0.0.1 -P3306 -uroot -ppassword --default-character-set=utf8mb4 ry-vue < sql/lab/lab_full.sql

# 3. 修改 ruoyi-admin 下 application.yml / application-druid.yml 中的库、Redis、上传目录
# 4. 启动
cd ruoyi-admin
mvn spring-boot:run
```

接口根地址：`http://localhost:8080`（无 context-path）。对接说明：[docs/接口文档.md](docs/接口文档.md)。

### 种子账号（密码均为 `admin123`）

| 账号 | 身份 | 用途 |
|------|------|------|
| `admin` | 塔员 | 后台与塔员接口 |
| `ry` | 新生 | 新生登录 |
| `qt_member` | 塔员 | 预约 / 借书 / 服装 |
| `qt_fresh` | 新生 | 报名 / 面试 |

## 前端

```bash
# 管理端
cd ruoyi-ui && npm install && npm run dev

# C 端
cd ruoyi-qt-ui && npm install && npm run dev
```

开发时将前端代理或 `VUE_APP_BASE_API` 指向后端 `http://localhost:8080`。

## 文档

- [后端部署文档](docs/后端部署文档.md)
- [接口文档](docs/接口文档.md)
- [实验室 SQL 说明](sql/lab/README.md)

## 上游

框架能力（用户、角色、菜单、字典、代码生成等）沿用若依，上游项目：[RuoYi-Vue](https://github.com/yangzongzhuan/RuoYi-Vue)。本仓库仅保留实验室业务相关说明，不再使用若依官方演示与宣传内容。
