# Quanta 门户 Web 端验收报告

> 验收日期：2026-09-16  
> 验收范围：本地代码、真实本地后端、桌面浏览器  
> 部署状态：**未上线，未修改任何线上环境**

## 结论

Quanta 统一 Web 门户的开发阶段功能已完成。新生、塔员和管理三类账号可以从统一入口登录到各自路由域，门户页面已连接当前后端 `/qt/*`、`/system/*` 与认证接口。原 `Quanta-uniapp` 保留，不参与 Web 正式入口。

## 自动验证

| 项目 | 结果 |
| --- | --- |
| Web 单元/组件测试 | 61 个文件、165 个测试全部通过 |
| ESLint | 通过 |
| Vite 生产构建 | 通过，资源路径使用 `/assets/` |
| 后端相关模块测试 | framework 6 个、qt 15 个测试通过 |
| 后端 Maven 打包 | `ruoyi-admin.jar` 打包成功 |
| 浏览器控制台 | 0 error、0 warning |

## 真实接口与页面验收

| 身份 | 页面/能力 | 真实接口 | 结果 |
| --- | --- | --- | --- |
| 新生 `qt_fresh` | 首页、报名与进度 | `/getInfo`、`/qt/interview/my`、`/qt/interview/myResults` | 通过 |
| 新生 `qt_fresh` | 活动列表 | `/qt/activity/list` | 通过，返回 2 条测试数据 |
| 新生 `qt_fresh` | 账号安全 | `/system/user/profile/updatePwd` | 页面与请求契约通过；未执行改密写操作 |
| 塔员 `qt_member` | 通讯录、个人中心 | `/qt/member/list`、`/getInfo` | 通过，返回 2 条成员数据 |
| 塔员 `qt_member` | 通知与学习资料 | `/system/notice/listTop`、`/qt/materials` | 通过；当前资料列表为空时显示明确空状态 |
| 塔员 `qt_member` | 我的服务 | 三个 `/qt/*/detailList` | 通过，工位/图书/塔服测试记录均正常展示 |
| 塔员 `qt_member` | 图书、工位、塔服 | `/qt/book/list`、`/qt/workstation/list`、`/qt/item/list` | 通过，分别返回 3、3、2 条测试数据 |
| 管理员 `admin` | 管理后台 | `/getRouters`、`/dashboard/*`、管理端 `/qt/*` | 登录并进入 `/admin/dashboard`，通过 |
| 身份隔离 | 新生访问塔员成员接口 | `/qt/member/list` | 后端拒绝，提示“仅塔员可访问该功能” |

浏览器已逐页验证 `/freshman/*`、`/member/*` 与 `/admin/dashboard` 的深链访问。验证过程只执行登录和只读查询，没有提交报名、借阅、预约、订单或密码修改。

## 本地数据与 Mock 边界

业务请求失败时不会自动回退 Mock。仅下列非闭环数据保留在前端：

1. 电子名片：浏览器 `localStorage`，不跨设备同步。
2. 首页品牌横幅：随前端发布的静态素材。

完整清单见 [Quanta门户Web端接口Mock清单.md](./Quanta门户Web端接口Mock清单.md)。

## 上线前仍需确认

1. 现有本地 Docker 数据卷早于最新迁移脚本，缺少 `qt_access_metric` 表；全新环境会由 `cloudrun/quanta-mysql/initdb/11_patch_access_metric.sql` 创建，旧环境升级时必须补执行该脚本。
2. 活动报名的时间窗和容量最终校验仍应由后端继续加固。
3. 塔服当前只保存 `DRAFT`；正式价格、服务端金额计算和付款闭环未完成前，不应开放正式下单。
4. 后端字典中的 `ANDROID` 历史值仍需清理；Web 已完全隐藏安卓部门，仅保留产品、设计、前端、后端。

## 可重复冒烟

后端和 Web 本地启动后，在仓库根目录执行：

```powershell
& .\Quanta-admin-web\scripts\portal-smoke.ps1
```

脚本只执行登录、页面打开和控制台检查，不执行业务写操作。

