# 学习资料与塔服订单设计

## 学习资料

- `/learning-materials` 替换占位页，包含拖拽/点击上传区和资料表格。
- 表格字段：文件名、分类、大小、上传者、上传时间、可见性、操作。
- 支持 PDF、Word、PPT、Figma、Markdown 等文件，单文件不超过 50MB。
- 分类按扩展名映射，上传资料默认“所有人可见”。
- 管理层持有 `material:write`，可上传、删除；经理层仅持有 `material:list`，页面只读。
- 删除前使用确认弹窗；成功后更新列表并提示。
- 后端接口缺失，暂定 `GET/POST /qt/materials`、`DELETE /qt/materials/{materialId}`，本地使用 Mock。

## 塔服订单

- `/clothing-orders` 替换占位页，展示订单表格和状态筛选。
- 字段：姓名、颜色、尺码、单价、下单时间、备注、状态、操作。
- 状态映射：`APPROVED` 已确认收款、`DRAFT` pending、`SUBMITTED` 待确认（已传图）、`REJECTED` 已驳回、`CANCELED` 已取消。
- `SUBMITTED` 行支持“查看凭证”和“确认收款”；确认前弹出二次确认框。
- 列表使用 `GET /system/order/detailList`，不传 `userId`。
- 确认收款暂定 `PUT /system/order/{orderId}/approve`，作为待后端补充接口。
- 管理层持有 `order:list`、`order:confirm`；经理层不下发塔服菜单。

## 验收

- 权限同时作用于动态菜单、按钮和 Mock 后端。
- 上传大小、删除确认、状态筛选、凭证预览和确认收款均可交互。
- 样式沿用现有紧凑 12px 表格体系并匹配设计稿。
- 测试、Lint、构建和浏览器验收通过。
