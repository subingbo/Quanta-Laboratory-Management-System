import PortalLayout from '@/layout/PortalLayout.vue'
import PortalPlaceholder from '@/views/portal-placeholder/index.vue'

export const portalRoutes = [
  {
    path: '/freshman',
    component: PortalLayout,
    meta: { portalAudience: 'freshman' },
    children: [
      { path: '', redirect: '/freshman/home' },
      {
        path: 'home',
        name: 'FreshmanHome',
        component: PortalPlaceholder,
        meta: { title: '新生首页', portalAudience: 'freshman' },
      },
      {
        path: 'recruitment',
        name: 'FreshmanRecruitment',
        component: PortalPlaceholder,
        meta: { title: '加入我们', portalAudience: 'freshman' },
      },
      {
        path: 'events',
        name: 'FreshmanEvents',
        component: PortalPlaceholder,
        meta: { title: 'Quanta 活动', portalAudience: 'freshman' },
      },
      {
        path: 'security',
        name: 'FreshmanSecurity',
        component: PortalPlaceholder,
        meta: { title: '账号安全', portalAudience: 'freshman' },
      },
    ],
  },
  {
    path: '/member',
    component: PortalLayout,
    meta: { portalAudience: 'member' },
    children: [
      { path: '', redirect: '/member/home' },
      {
        path: 'home',
        name: 'MemberHome',
        component: PortalPlaceholder,
        meta: { title: '塔员首页', portalAudience: 'member' },
      },
      {
        path: 'directory',
        name: 'MemberDirectory',
        component: PortalPlaceholder,
        meta: { title: '通讯录', portalAudience: 'member' },
      },
      {
        path: 'profile',
        name: 'MemberProfile',
        component: PortalPlaceholder,
        meta: { title: '个人中心', portalAudience: 'member' },
      },
      {
        path: 'services',
        name: 'MemberServices',
        component: PortalPlaceholder,
        meta: { title: '我的服务', portalAudience: 'member' },
      },
      {
        path: 'library',
        name: 'MemberLibrary',
        component: PortalPlaceholder,
        meta: { title: '图书借阅', portalAudience: 'member' },
      },
      {
        path: 'workstations',
        name: 'MemberWorkstations',
        component: PortalPlaceholder,
        meta: { title: '工位预约', portalAudience: 'member' },
      },
      {
        path: 'clothing',
        name: 'MemberClothing',
        component: PortalPlaceholder,
        meta: { title: '塔服订购', portalAudience: 'member' },
      },
      {
        path: 'materials',
        name: 'MemberMaterials',
        component: PortalPlaceholder,
        meta: { title: '学习资料', portalAudience: 'member' },
      },
      {
        path: 'security',
        name: 'MemberSecurity',
        component: PortalPlaceholder,
        meta: { title: '账号安全', portalAudience: 'member' },
      },
    ],
  },
]
