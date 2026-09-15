# Quanta 门户 Web 迁移 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Quanta uniapp 的新生端和塔员端功能迁移进现有管理端 Web 工程，形成桌面优先、移动可用、三身份隔离的统一 Web 应用。

**Architecture:** 在 `Quanta-admin-web` 内新增公共入口、新生门户和塔员门户三个路由域，继续复用现有管理后台与统一请求层。门户 API 在边界处把真实后端响应转换成页面模型；只有电子名片使用明确登记的浏览器本地存储，首页轮播使用静态素材。

**Tech Stack:** Vue 3、Vue Router、Pinia、Element Plus、Axios、Vitest、Vue Test Utils、Vite 8、Playwright CLI

**Spec:** `docs/superpowers/specs/2026-09-15-Quanta门户Web迁移设计.md`

## Global Constraints

- 完整保留 `Quanta-uniapp`，迁移过程中不删除、不覆盖其页面和素材。
- 正式体验以桌面 Web 为主；手机端必须保证导航、表单和关键操作可用。
- 生产应用部署在站点根路径 `/`，管理后台入口为 `/admin/*`。
- 新生登录固定提交 `loginType: '0'`；塔员和管理登录固定提交 `loginType: '1'`。
- 安卓部门已取缔，不得在任何新页面、选项或映射中增加 `ANDROID` 或“安卓”。
- 业务接口统一使用后端最新 `/qt/*` 路径；普通错误不得自动回退 Mock。
- 写操作仅在真实后端返回业务成功后显示成功。
- 保留现有管理后台视觉与功能，所有阶段都必须通过管理端回归测试和生产构建。
- 不提交用户已有的 `Quanta-uniapp/src/manifest.json` 改动、`.local/` 或 `.superpowers/` 临时文件。

---

### Task 1: 建立根路径部署与三身份路由骨架

**Files:**
- Create: `Quanta-admin-web/src/router/portal-routes.js`
- Create: `Quanta-admin-web/src/router/__tests__/portal-routes.test.js`
- Create: `Quanta-admin-web/src/layout/PortalLayout.vue`
- Create: `Quanta-admin-web/src/views/entry/index.vue`
- Create: `Quanta-admin-web/src/views/portal-placeholder/index.vue`
- Modify: `Quanta-admin-web/src/router/routes.js`
- Modify: `Quanta-admin-web/vite.config.js`

**Interfaces:**
- Produces: `portalRoutes: RouteRecordRaw[]`，包含 `/freshman/*` 与 `/member/*` 路由元数据。
- Produces: `portalAudience` 路由元字段，值为 `'freshman' | 'member' | 'admin'`。
- Produces: 公开入口 `/`、三类登录路径和兼容路径 `/login`。

- [ ] **Step 1: 写路由与生产基路径失败测试**

```js
import { describe, expect, it } from 'vitest'
import viteConfig from '../../../vite.config.js'
import { staticRoutes } from '../routes'
import { portalRoutes } from '../portal-routes'

describe('portal route contract', () => {
  it('exposes the three confirmed route domains', () => {
    expect(staticRoutes.find((route) => route.path === '/')).toBeTruthy()
    expect(staticRoutes.find((route) => route.path === '/admin/login')).toBeTruthy()
    expect(portalRoutes.map((route) => route.path)).toEqual(['/freshman', '/member'])
  })

  it('builds production assets from the site root', () => {
    expect(viteConfig({ mode: 'production' }).base).toBe('/')
  })
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/router/__tests__/portal-routes.test.js`

Expected: FAIL，因为 `portal-routes.js` 尚不存在，生产 `base` 仍是 `/admin/`。

- [ ] **Step 3: 实现路由目录和可构建占位页**

```js
import PortalLayout from '@/layout/PortalLayout.vue'
import PortalPlaceholder from '@/views/portal-placeholder/index.vue'

export const portalRoutes = [
  {
    path: '/freshman',
    component: PortalLayout,
    meta: { portalAudience: 'freshman' },
    children: [
      { path: '', redirect: '/freshman/home' },
      { path: 'home', name: 'FreshmanHome', component: PortalPlaceholder, meta: { title: '新生首页', portalAudience: 'freshman' } },
      { path: 'recruitment', name: 'FreshmanRecruitment', component: PortalPlaceholder, meta: { title: '加入我们', portalAudience: 'freshman' } },
      { path: 'events', name: 'FreshmanEvents', component: PortalPlaceholder, meta: { title: 'Quanta 活动', portalAudience: 'freshman' } },
    ],
  },
  {
    path: '/member',
    component: PortalLayout,
    meta: { portalAudience: 'member' },
    children: [
      { path: '', redirect: '/member/home' },
      { path: 'home', name: 'MemberHome', component: PortalPlaceholder, meta: { title: '塔员首页', portalAudience: 'member' } },
      { path: 'directory', name: 'MemberDirectory', component: PortalPlaceholder, meta: { title: '通讯录', portalAudience: 'member' } },
      { path: 'profile', name: 'MemberProfile', component: PortalPlaceholder, meta: { title: '个人中心', portalAudience: 'member' } },
      { path: 'services', name: 'MemberServices', component: PortalPlaceholder, meta: { title: '我的服务', portalAudience: 'member' } },
      { path: 'library', name: 'MemberLibrary', component: PortalPlaceholder, meta: { title: '图书借阅', portalAudience: 'member' } },
      { path: 'workstations', name: 'MemberWorkstations', component: PortalPlaceholder, meta: { title: '工位预约', portalAudience: 'member' } },
      { path: 'clothing', name: 'MemberClothing', component: PortalPlaceholder, meta: { title: '塔服订购', portalAudience: 'member' } },
    ],
  },
]
```

在 `vite.config.js` 中设置 `base: '/'`。在 `routes.js` 中把 `/` 改为身份选择页，把现有管理后台根路由移动到 `/admin`，添加 `/admin/login`，并让 `/login` 重定向 `/admin/login`。占位页使用 `route.meta.title` 展示明确页面标题，保证这一提交可以构建。

- [ ] **Step 4: 运行路由测试、全量测试和构建**

Run: `cd Quanta-admin-web && npm run test:run -- src/router/__tests__/portal-routes.test.js && npm run test:run && npm run build`

Expected: 路由测试通过；将现有守卫测试中的管理首页预期从 `/dashboard` 统一更新为 `/admin/dashboard` 后，全量测试与构建通过。

- [ ] **Step 5: 提交路由骨架**

```bash
git add Quanta-admin-web/src/router Quanta-admin-web/src/layout/PortalLayout.vue Quanta-admin-web/src/views/entry Quanta-admin-web/src/views/portal-placeholder Quanta-admin-web/vite.config.js
git commit -m "feat: add unified portal route structure"
```

---

### Task 2: 实现身份感知的登录与路由守卫

**Files:**
- Create: `Quanta-admin-web/src/utils/session-audience.js`
- Create: `Quanta-admin-web/src/utils/__tests__/session-audience.test.js`
- Create: `Quanta-admin-web/src/views/portal-login/index.vue`
- Create: `Quanta-admin-web/src/views/portal-login/portal-login.css`
- Modify: `Quanta-admin-web/src/views/entry/index.vue`
- Modify: `Quanta-admin-web/src/stores/user.js`
- Modify: `Quanta-admin-web/src/stores/__tests__/user.test.js`
- Modify: `Quanta-admin-web/src/router/guard.js`
- Modify: `Quanta-admin-web/src/router/__tests__/guard.test.js`
- Modify: `Quanta-admin-web/src/router/routes.js`

**Interfaces:**
- Produces: `getAudience(): 'freshman' | 'member' | 'admin' | ''`。
- Produces: `setAudience(audience)` 与 `clearAudience()`。
- Produces: `userStore.login(credentials, audience)`，自动加入正确 `loginType`。
- Consumes: `route.meta.portalAudience` from Task 1。

- [ ] **Step 1: 写身份持久化、登录参数和隔离测试**

```js
it('submits the correct loginType for each audience', async () => {
  authApi.login.mockResolvedValue({ token: 'token' })
  const store = useUserStore(pinia)
  await store.login({ username: 'freshman', password: 'secret' }, 'freshman')
  expect(authApi.login).toHaveBeenCalledWith(expect.objectContaining({ loginType: '0' }))
  await store.login({ username: 'member', password: 'secret' }, 'member')
  expect(authApi.login).toHaveBeenLastCalledWith(expect.objectContaining({ loginType: '1' }))
})

it('redirects a freshman away from member routes', async () => {
  setAudience('freshman')
  setToken('token')
  const router = createTestRouter()
  await router.push('/member/home')
  expect(router.currentRoute.value.path).toBe('/freshman/home')
})
```

- [ ] **Step 2: 运行定向测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/utils/__tests__/session-audience.test.js src/stores/__tests__/user.test.js src/router/__tests__/guard.test.js`

Expected: FAIL，因为 audience 尚未持久化，守卫也不认识门户身份。

- [ ] **Step 3: 实现 audience 会话与三类登录页**

```js
const AUDIENCE_KEY = 'quanta_session_audience'
const allowed = new Set(['freshman', 'member', 'admin'])

export function getAudience() {
  const value = localStorage.getItem(AUDIENCE_KEY) || ''
  return allowed.has(value) ? value : ''
}

export function setAudience(value) {
  if (!allowed.has(value)) throw new Error('未知登录身份')
  localStorage.setItem(AUDIENCE_KEY, value)
}

export function clearAudience() {
  localStorage.removeItem(AUDIENCE_KEY)
}
```

`userStore.login(credentials, audience)` 对新生写入 `'0'`，对塔员和管理写入 `'1'`；成功后持久化 audience。退出和 401 同时清理 Token 与 audience。公共入口展示三个桌面卡片，分别进入 `/login/freshman`、`/login/member`、`/admin/login`。新生与塔员登录复用 `portal-login/index.vue`，管理登录继续复用现有美化页面。

- [ ] **Step 4: 实现守卫并执行测试**

守卫使用 `to.meta.public` 判断公开页面；未登录时根据目标 audience 选择登录页；登录后发现 audience 不匹配时跳回该会话的门户首页。`/admin/*` 还必须通过原有 permission 校验。401 返回 `loginPathFor(getAudience())`。

Run: `cd Quanta-admin-web && npm run test:run -- src/utils/__tests__/session-audience.test.js src/stores/__tests__/user.test.js src/router/__tests__/guard.test.js && npm run lint`

Expected: 所有身份、401 和开放重定向测试通过。

- [ ] **Step 5: 提交身份认证**

```bash
git add Quanta-admin-web/src/utils/session-audience.js Quanta-admin-web/src/utils/__tests__/session-audience.test.js Quanta-admin-web/src/views/entry Quanta-admin-web/src/views/portal-login Quanta-admin-web/src/stores Quanta-admin-web/src/router
git commit -m "feat: add audience-aware portal authentication"
```

---

### Task 3: 完成桌面优先的门户设计系统与布局

**Files:**
- Create: `Quanta-admin-web/src/styles/portal-variables.css`
- Create: `Quanta-admin-web/src/styles/portal.css`
- Create: `Quanta-admin-web/src/layout/components/PortalHeader.vue`
- Create: `Quanta-admin-web/src/layout/components/PortalUserMenu.vue`
- Create: `Quanta-admin-web/src/layout/__tests__/PortalLayout.test.js`
- Modify: `Quanta-admin-web/src/layout/PortalLayout.vue`
- Modify: `Quanta-admin-web/src/main.js`
- Modify: `Quanta-admin-web/src/views/entry/index.vue`
- Modify: `Quanta-admin-web/src/views/portal-login/index.vue`

**Interfaces:**
- Produces: CSS tokens `--portal-orange`, `--portal-ink`, `--portal-surface`, `--portal-radius-lg`。
- Produces: `PortalHeader`，根据 `audience` 渲染新生或塔员导航。
- Produces: `.portal-container`、`.portal-grid`、`.portal-card` 响应式布局类。

- [ ] **Step 1: 写布局与移动导航失败测试**

```js
it('renders desktop navigation and a mobile menu trigger', () => {
  const wrapper = mount(PortalLayout, {
    global: { plugins: [router, pinia], stubs: { RouterView: true } },
  })
  expect(wrapper.find('[data-testid="portal-desktop-nav"]').exists()).toBe(true)
  expect(wrapper.find('[data-testid="portal-mobile-menu"]').exists()).toBe(true)
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/layout/__tests__/PortalLayout.test.js`

Expected: FAIL，因为门户头部组件和测试标记尚不存在。

- [ ] **Step 3: 实现门户布局与样式变量**

```css
:root {
  --portal-orange: #ff6600;
  --portal-orange-soft: #fff0dc;
  --portal-ink: #1f2937;
  --portal-muted: #6b7280;
  --portal-surface: #ffffff;
  --portal-radius-lg: 24px;
  --portal-shadow: 0 18px 50px rgb(255 102 0 / 10%);
}

.portal-container { width: min(1180px, calc(100% - 48px)); margin: 0 auto; }
.portal-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px; }
@media (max-width: 760px) {
  .portal-container { width: min(100% - 28px, 680px); }
  .portal-grid { grid-template-columns: 1fr; }
}
```

PortalLayout 使用顶部固定品牌导航和 `<RouterView />`；桌面显示横向导航，手机显示折叠菜单。入口页和门户登录页统一使用相同 Token、Logo、圆角和焦点样式。

- [ ] **Step 4: 运行布局测试、可访问性静态检查和构建**

Run: `cd Quanta-admin-web && npm run test:run -- src/layout/__tests__/PortalLayout.test.js && npm run lint && npm run build`

Expected: 测试、lint 和构建通过；不引入新的 UI 依赖。

- [ ] **Step 5: 提交门户设计系统**

```bash
git add Quanta-admin-web/src/styles Quanta-admin-web/src/layout Quanta-admin-web/src/views/entry Quanta-admin-web/src/views/portal-login Quanta-admin-web/src/main.js
git commit -m "feat: add responsive Quanta portal shell"
```

---

### Task 4: 迁移新生端真实 API 与草稿存储

**Files:**
- Create: `Quanta-admin-web/src/api/portal/activities.js`
- Create: `Quanta-admin-web/src/api/portal/recruitment.js`
- Create: `Quanta-admin-web/src/api/portal/account.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/activities.test.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/recruitment.test.js`
- Create: `Quanta-admin-web/src/utils/recruitment-draft.js`
- Create: `Quanta-admin-web/src/utils/__tests__/recruitment-draft.test.js`

**Interfaces:**
- Produces: `getPublishedActivities()`、`getMyActivitySignup(activityId)`、`signupActivity(activityId, remark)`。
- Produces: `getMyApplication()`、`getMyInterviewProcess()`、`submitApplication(form)`。
- Produces: `loadRecruitmentDraft()`、`saveRecruitmentDraft(form)`、`clearRecruitmentDraft()`。
- Produces: `changePassword({ oldPassword, newPassword })`。

- [ ] **Step 1: 写真实接口路径、映射和草稿生命周期测试**

```js
it('uses the hardened qt activity endpoints', async () => {
  request.mockResolvedValueOnce({ rows: [] })
  await getPublishedActivities()
  expect(request).toHaveBeenCalledWith(expect.objectContaining({ url: '/qt/activity/list' }))
})

it('clears a saved draft only after successful submission', async () => {
  saveRecruitmentDraft({ realName: '小李' })
  expect(loadRecruitmentDraft().realName).toBe('小李')
  clearRecruitmentDraft()
  expect(loadRecruitmentDraft()).toBeNull()
})
```

- [ ] **Step 2: 运行定向测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__/activities.test.js src/api/portal/__tests__/recruitment.test.js src/utils/__tests__/recruitment-draft.test.js`

Expected: FAIL，因为门户 API 和草稿模块尚不存在。

- [ ] **Step 3: 移植并浏览器化新生 API**

活动使用 `GET /qt/activity/list`、`GET /qt/signup/detailList`、`POST /qt/signup`。招新使用 `GET /qt/interview/my`、`GET /qt/interview/myResults` 和 `POST /qt/interview/apply`。报名提交统一使用浏览器 FormData：

```js
export function submitApplication(form) {
  const data = new FormData()
  Object.entries(toApplicationPayload(form)).forEach(([key, value]) => data.append(key, value ?? ''))
  if (form.photoFile instanceof File) data.append('photoFile', form.photoFile)
  return request({ url: '/qt/interview/apply', method: 'post', data })
}
```

部门映射只允许 `PRODUCT`、`DESIGN`、`FRONTEND`、`BACKEND`。从 uniapp 迁移面试状态映射，但邀请状态只读，不提供前端伪接受或拒绝动作。密码修改使用 `PUT /system/user/profile/updatePwd`。

- [ ] **Step 4: 运行 API 单测**

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__ src/utils/__tests__/recruitment-draft.test.js`

Expected: 所有接口路径、表单键、部门映射、状态映射和草稿测试通过。

- [ ] **Step 5: 提交新生 API**

```bash
git add Quanta-admin-web/src/api/portal Quanta-admin-web/src/utils/recruitment-draft.js Quanta-admin-web/src/utils/__tests__/recruitment-draft.test.js
git commit -m "feat: add freshman portal API adapters"
```

---

### Task 5: 迁移新生首页、招新与活动页面

**Files:**
- Create: `Quanta-admin-web/src/views/freshman/home/index.vue`
- Create: `Quanta-admin-web/src/views/freshman/home/home.css`
- Create: `Quanta-admin-web/src/views/freshman/recruitment/index.vue`
- Create: `Quanta-admin-web/src/views/freshman/recruitment/components/ApplicationForm.vue`
- Create: `Quanta-admin-web/src/views/freshman/recruitment/components/InterviewTimeline.vue`
- Create: `Quanta-admin-web/src/views/freshman/recruitment/recruitment.css`
- Create: `Quanta-admin-web/src/views/freshman/events/index.vue`
- Create: `Quanta-admin-web/src/views/freshman/events/events.css`
- Create: `Quanta-admin-web/src/views/freshman/__tests__/freshman-portal.test.js`
- Modify: `Quanta-admin-web/src/router/portal-routes.js`

**Interfaces:**
- Consumes: Task 4 activity、recruitment、draft 和 account API。
- Produces: 可导航的新生首页、加入我们、报名/进度页和活动页。

- [ ] **Step 1: 写新生关键流程失败测试**

```js
it('offers only active departments and submits a browser file', async () => {
  const wrapper = mount(ApplicationForm, { props: { initialValue: emptyApplication() } })
  expect(wrapper.text()).toContain('全栈（后端）')
  expect(wrapper.text()).not.toContain('安卓')
  await wrapper.find('input[name="realName"]').setValue('新生小李')
  await wrapper.find('form').trigger('submit')
  expect(wrapper.emitted('submit')).toHaveLength(1)
})

it('renders interview invitations as read-only status', () => {
  const wrapper = mount(InterviewTimeline, { props: { processes: invitedFixture } })
  expect(wrapper.text()).toContain('等待面试')
  expect(wrapper.text()).not.toContain('接受邀请')
  expect(wrapper.text()).not.toContain('拒绝邀请')
})
```

- [ ] **Step 2: 运行页面测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/views/freshman/__tests__/freshman-portal.test.js`

Expected: FAIL，因为页面组件尚不存在。

- [ ] **Step 3: 实现桌面优先的新生页面**

首页以 Quanta 品牌横幅、招新进度卡和活动卡片为主。招新页使用“了解 Quanta / 填写报名 / 查看进度”桌面标签结构；报名表双栏排版，手机端单列；照片使用原生文件选择和预览。活动页并列展示宣讲会与精英分享会，报名按钮请求期间禁用，成功后重新查询报名记录。

```vue
<form class="application-grid" @submit.prevent="$emit('submit', model)">
  <label>姓名<input v-model.trim="model.realName" name="realName" required /></label>
  <label>第一志愿<select v-model="model.firstChoice" required><option v-for="item in departments" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
  <label class="application-grid__wide">个人介绍<textarea v-model.trim="model.selfIntro" required /></label>
  <button class="portal-primary-button" :disabled="submitting">{{ submitting ? '提交中…' : '提交报名' }}</button>
</form>
```

- [ ] **Step 4: 运行新生测试和生产构建**

Run: `cd Quanta-admin-web && npm run test:run -- src/views/freshman src/api/portal && npm run lint && npm run build`

Expected: 新生页面与 API 测试通过，构建产物包含 freshman 分包。

- [ ] **Step 5: 提交新生门户**

```bash
git add Quanta-admin-web/src/views/freshman Quanta-admin-web/src/router/portal-routes.js
git commit -m "feat: migrate freshman experience to web"
```

---

### Task 6: 迁移塔员资料、通讯录、通知和学习资料

**Files:**
- Create: `Quanta-admin-web/src/api/portal/members.js`
- Create: `Quanta-admin-web/src/api/portal/content.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/members.test.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/content.test.js`
- Create: `Quanta-admin-web/src/utils/business-card-storage.js`
- Create: `Quanta-admin-web/src/utils/__tests__/business-card-storage.test.js`
- Create: `Quanta-admin-web/src/views/member/home/index.vue`
- Create: `Quanta-admin-web/src/views/member/directory/index.vue`
- Create: `Quanta-admin-web/src/views/member/profile/index.vue`
- Create: `Quanta-admin-web/src/views/member/profile/components/BusinessCardEditor.vue`
- Create: `Quanta-admin-web/src/views/member/profile/components/BusinessCardPreview.vue`
- Create: `Quanta-admin-web/src/views/member/__tests__/member-profile.test.js`
- Modify: `Quanta-admin-web/src/router/portal-routes.js`

**Interfaces:**
- Produces: `getMemberDirectory(params)`、`getCurrentProfile()`。
- Produces: `getTopNotices()`、`markNoticeRead(id)`、`getPortalMaterials()`、`downloadPortalMaterial(material)`。
- Produces: `loadBusinessCard(memberId)`、`saveBusinessCard(card)`。

- [ ] **Step 1: 写通讯录映射、内容接口和名片隔离测试**

```js
it('maps active lab members from /qt/member/list', async () => {
  request.mockResolvedValue({ rows: [{ userId: 7, nickName: '李同学', memberDepartment: 'PRODUCT', memberStatus: 'ACTIVE' }] })
  expect(await getMemberDirectory()).toEqual([expect.objectContaining({ id: 7, name: '李同学', isActive: true })])
})

it('stores business cards per member in this browser', () => {
  saveBusinessCard({ memberId: 'QT021', bio: 'Keep learning.' })
  expect(loadBusinessCard('QT021')).toMatchObject({ bio: 'Keep learning.' })
  expect(loadBusinessCard('QT022')).toBeNull()
})
```

- [ ] **Step 2: 运行定向测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__/members.test.js src/api/portal/__tests__/content.test.js src/utils/__tests__/business-card-storage.test.js`

Expected: FAIL，因为模块尚不存在。

- [ ] **Step 3: 实现真实内容 API 和本地名片存储**

通讯录使用 `GET /qt/member/list`；个人资料使用 `GET /getInfo`；通知使用 `/system/notice/listTop` 与 `/system/notice/markRead`；资料使用 `GET /qt/materials` 与记录中的 `downloadUrl`，下载复用现有 `requestBlob`。电子名片按 `memberId` 存入 `quanta_portal_business_cards_v1`，简介清理首尾空格并限制 800 字。

- [ ] **Step 4: 实现并测试塔员内容页面**

塔员首页展示欢迎横幅、个人概览、活动、通知和常用功能。通讯录提供关键词、届次和部门筛选。个人页展示后端真实资料，并把“电子名片为本浏览器资料”写在编辑器旁。资料下载和通知标记已读必须显示真实错误。

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__ src/utils/__tests__/business-card-storage.test.js src/views/member/__tests__/member-profile.test.js && npm run build`

Expected: API、存储、页面测试与构建通过。

- [ ] **Step 5: 提交塔员内容模块**

```bash
git add Quanta-admin-web/src/api/portal Quanta-admin-web/src/utils/business-card-storage.js Quanta-admin-web/src/utils/__tests__/business-card-storage.test.js Quanta-admin-web/src/views/member Quanta-admin-web/src/router/portal-routes.js
git commit -m "feat: migrate member profile and content portal"
```

---

### Task 7: 建立塔员图书、工位、塔服和服务记录 API

**Files:**
- Create: `Quanta-admin-web/src/api/portal/library.js`
- Create: `Quanta-admin-web/src/api/portal/workstations.js`
- Create: `Quanta-admin-web/src/api/portal/clothing.js`
- Create: `Quanta-admin-web/src/api/portal/services.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/library.test.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/workstations.test.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/clothing.test.js`
- Create: `Quanta-admin-web/src/api/portal/__tests__/services.test.js`

**Interfaces:**
- Produces: `getBooks(params)`、`borrowBook(bookId, dueTime)`。
- Produces: `getWorkstations(params)`、`createReservation(payload)`。
- Produces: `getClothingItems()`、`createClothingOrder(item, selection)`。
- Produces: `getMyServices()`，返回 `{ reservations, borrows, orders }`。

- [ ] **Step 1: 写四组真实 `/qt` 接口失败测试**

```js
it.each([
  [getBooks, '/qt/book/list'],
  [getWorkstations, '/qt/workstation/list'],
  [getClothingItems, '/qt/item/list'],
])('%# calls the hardened endpoint', async (call, url) => {
  request.mockResolvedValue({ rows: [] })
  await call()
  expect(request).toHaveBeenLastCalledWith(expect.objectContaining({ url }))
})

it('loads only current-user service detail lists', async () => {
  request.mockResolvedValue({ rows: [] })
  await getMyServices()
  expect(request.mock.calls.map(([config]) => config.url)).toEqual([
    '/qt/reservation/detailList', '/qt/borrow/detailList', '/qt/order/detailList',
  ])
})
```

- [ ] **Step 2: 运行 API 测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__/library.test.js src/api/portal/__tests__/workstations.test.js src/api/portal/__tests__/clothing.test.js src/api/portal/__tests__/services.test.js`

Expected: FAIL，因为服务 API 尚不存在。

- [ ] **Step 3: 移植并规范化服务接口**

图书列表和借阅使用 `/qt/book/list`、`POST /qt/borrow`；工位使用 `/qt/workstation/list`、`POST /qt/reservation`；塔服使用 `/qt/item/list`、`POST /qt/order`；我的服务使用三个 `/detailList`。日期统一为后端 `yyyy-MM-dd HH:mm:ss`，金额由后端字段提供；缺少价格时显示“以实物通知为准”，不得硬编码 45 元。

```js
export function createReservation({ workstationId, reserveStart, reserveEnd }) {
  return request({
    url: '/qt/reservation',
    method: 'post',
    data: { workstationId: Number(workstationId), reserveStart, reserveEnd },
  })
}
```

- [ ] **Step 4: 运行 API 测试**

Run: `cd Quanta-admin-web && npm run test:run -- src/api/portal/__tests__/library.test.js src/api/portal/__tests__/workstations.test.js src/api/portal/__tests__/clothing.test.js src/api/portal/__tests__/services.test.js`

Expected: 请求方法、路径、参数和视图模型映射全部通过。

- [ ] **Step 5: 提交塔员服务 API**

```bash
git add Quanta-admin-web/src/api/portal
git commit -m "feat: add member self-service API adapters"
```

---

### Task 8: 迁移塔员服务记录、图书、工位和塔服页面

**Files:**
- Create: `Quanta-admin-web/src/views/member/services/index.vue`
- Create: `Quanta-admin-web/src/views/member/library/index.vue`
- Create: `Quanta-admin-web/src/views/member/workstations/index.vue`
- Create: `Quanta-admin-web/src/views/member/clothing/index.vue`
- Create: `Quanta-admin-web/src/views/member/services/services.css`
- Create: `Quanta-admin-web/src/views/member/__tests__/member-services.test.js`
- Modify: `Quanta-admin-web/src/router/portal-routes.js`

**Interfaces:**
- Consumes: Task 7 的四个 API 模块。
- Produces: 桌面端塔员自助服务完整页面。

- [ ] **Step 1: 写服务页面关键交互失败测试**

```js
it('does not report a reservation until the backend succeeds', async () => {
  createReservation.mockRejectedValue(new Error('该时段已被预约'))
  const wrapper = mount(MemberWorkstations)
  await flushPromises()
  await wrapper.get('[data-testid="reserve-button"]').trigger('click')
  await flushPromises()
  expect(ElMessage.success).not.toHaveBeenCalled()
  expect(ElMessage.error).toHaveBeenCalledWith('该时段已被预约')
})

it('renders service history in three desktop tabs', async () => {
  const wrapper = mount(MemberServices)
  await flushPromises()
  expect(wrapper.text()).toContain('工位预约')
  expect(wrapper.text()).toContain('图书借阅')
  expect(wrapper.text()).toContain('塔服订单')
})
```

- [ ] **Step 2: 运行页面测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/views/member/__tests__/member-services.test.js`

Expected: FAIL，因为服务页面尚不存在。

- [ ] **Step 3: 实现四个桌面服务页面**

我的服务使用三个标签页和状态标签。图书页提供关键词、类别、可借状态与分页，借阅前显示到期日期确认。工位页以日期切换、工位卡片和时间段选择组成，提交后重新获取当天数据。塔服页展示效果图、颜色、尺码和“以实物通知为准”，提交后进入服务记录。

所有按钮使用 `submitting` 防重复请求；空状态显示下一步建议；窄屏下表格转换为卡片或安全横向滚动。

- [ ] **Step 4: 运行页面、API 和构建验证**

Run: `cd Quanta-admin-web && npm run test:run -- src/views/member src/api/portal && npm run lint && npm run build`

Expected: 塔员服务页面、全部门户 API、lint 和构建通过。

- [ ] **Step 5: 提交塔员自助服务页面**

```bash
git add Quanta-admin-web/src/views/member Quanta-admin-web/src/router/portal-routes.js
git commit -m "feat: migrate member self-service pages to web"
```

---

### Task 9: 登记门户 Web Mock 边界并完成管理后台回归

**Files:**
- Create: `docs/Quanta门户Web端接口Mock清单.md`
- Create: `Quanta-admin-web/src/config/portal-local-data.js`
- Create: `Quanta-admin-web/src/config/__tests__/portal-local-data.test.js`
- Modify: `Quanta-admin-web/README.md`
- Modify: `docs/Quanta管理端Web接口Mock清单.md`

**Interfaces:**
- Produces: `portalLocalData`，只登记 `business-card` 与 `home-banners`。
- Documents: 门户真实接口、局部数据、取消条件和复测日期。

- [ ] **Step 1: 写白名单边界失败测试**

```js
it('keeps the portal local-data registry explicit and minimal', () => {
  expect(portalLocalData.map((item) => item.key)).toEqual(['business-card', 'home-banners'])
  expect(portalLocalData.some((item) => item.key.includes('workstation'))).toBe(false)
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `cd Quanta-admin-web && npm run test:run -- src/config/__tests__/portal-local-data.test.js`

Expected: FAIL，因为门户局部数据清单尚不存在。

- [ ] **Step 3: 创建代码白名单和中文清单**

```js
export const portalLocalData = [
  { key: 'business-card', storage: 'localStorage', reason: '后端暂无电子名片接口' },
  { key: 'home-banners', storage: 'static-assets', reason: '品牌展示素材不属于业务接口' },
]
```

`docs/Quanta门户Web端接口Mock清单.md` 必须写明：电子名片不是跨设备数据；首页轮播是静态素材；工位、图书、塔服、招新和活动均使用真实接口；接口失败不会自动展示 Mock。同步更新 README 的三身份入口、开发命令、生产根路径与真实接口说明。

- [ ] **Step 4: 运行管理端与门户全量回归**

Run: `cd Quanta-admin-web && npm run test:run && npm run lint && npm run build`

Expected: 所有测试通过，现有管理后台页面和访问指标页面均包含在构建产物中。

- [ ] **Step 5: 提交文档与白名单**

```bash
git add docs/Quanta门户Web端接口Mock清单.md docs/Quanta管理端Web接口Mock清单.md Quanta-admin-web/src/config Quanta-admin-web/README.md
git commit -m "docs: record portal web integration boundaries"
```

---

### Task 10: 完成真实后端冒烟、浏览器验收与上线配置

**Files:**
- Create: `Quanta-admin-web/scripts/portal-smoke.ps1`
- Create: `docs/Quanta门户Web端验收报告.md`
- Modify: `deploy/aliyun/run.sh`
- Modify: `docs/云托管部署文档.md`

**Interfaces:**
- Consumes: 完成后的根路径 SPA、真实后端和三个测试账号类别。
- Produces: 可重复执行的桌面端浏览器冒烟测试和中文验收报告。

- [ ] **Step 1: 写浏览器冒烟脚本**

```powershell
param([string]$BaseUrl = 'http://127.0.0.1:5173')

playwright-cli -s=quanta-portal close
playwright-cli -s=quanta-portal open "$BaseUrl/"
playwright-cli -s=quanta-portal run-code "async page => {
  await page.getByRole('link', { name: '新生入口' }).waitFor()
  await page.getByRole('link', { name: '塔员入口' }).waitFor()
  await page.getByRole('link', { name: '管理入口' }).waitFor()
  await page.goto('$BaseUrl/login')
  await page.waitForURL('**/admin/login')
  return page.url()
}"
```

- [ ] **Step 2: 启动真实后端与 Web，先运行公开路由冒烟**

Run: `cd Quanta-admin-web && npm run dev -- --host 127.0.0.1`

Run: `playwright-cli -s=quanta-portal open http://127.0.0.1:5173/`

Expected: 身份选择页可见，三个入口可进入，控制台无资源 404。

- [ ] **Step 3: 按身份逐页验收真实数据流**

使用 `qt_fresh / admin123` 验证新生登录、报名、进度和活动；使用 `qt_member / admin123` 验证塔员登录、通讯录、资料、通知、服务记录、图书、工位和塔服；使用 `admin / admin123` 验证 `/admin/dashboard`、成员、招新和访问指标。不得用破坏性写操作验证生产数据；报名、活动、借阅、预约和下单写操作只在本地测试库执行，并在报告中记录测试对象和结果。

验收报告逐页记录：路由、账号身份、接口路径、结果、是否使用本地数据、截图或错误说明。电子名片明确标为浏览器本地存储。

- [ ] **Step 4: 更新部署脚本并执行最终验证**

部署配置必须把 `/`、`/freshman/*`、`/member/*` 和 `/admin/*` 回退到同一 `index.html`，同时优先转发 `/qt/*`、`/system/*`、`/getInfo`、`/getRouters`、`/login` 和 `/logout` 到后端。

Run: `cd Quanta-admin-web && npm run test:run && npm run lint && npm run build`

Run: `mvn -pl ruoyi-framework,ruoyi-qt -am test -DskipTests=false`

Expected: Web 全量测试、lint、构建和后端相关模块测试全部通过；`dist/index.html` 资源路径以 `/assets/` 开头。

- [ ] **Step 5: 提交验收与上线配置**

```bash
git add Quanta-admin-web/scripts/portal-smoke.ps1 docs/Quanta门户Web端验收报告.md docs/云托管部署文档.md deploy/aliyun/run.sh
git commit -m "test: verify unified Quanta web portals"
```
