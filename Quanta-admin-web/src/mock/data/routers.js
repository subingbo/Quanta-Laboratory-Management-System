export const mockRouters = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: 'dashboard/index',
    meta: {
      title: '控制台',
      icon: 'DataAnalysis',
      group: '总览',
      badge: '',
      permission: 'qt:dashboard:stats',
    },
  },
  {
    path: '/members',
    name: 'Members',
    component: 'members/index',
    meta: {
      title: '成员管理',
      icon: 'UserFilled',
      group: '成员管理',
      permission: 'qt:member:list',
    },
  },
  {
    path: '/recruitment',
    name: 'Recruitment',
    component: 'recruitment/index',
    meta: {
      title: '招新管理',
      icon: 'Tickets',
      group: '招生端',
      badge: 2,
      permission: 'qt:interview:admin:list',
    },
  },
  {
    path: '/lecture-signups',
    name: 'LectureSignups',
    component: 'lecture-signups/index',
    meta: {
      title: '宣讲会报名',
      icon: 'Promotion',
      group: '招生端',
      permission: 'qt:activity:registrations',
    },
  },
  {
    path: '/sharing-signups',
    name: 'SharingSignups',
    component: 'sharing-signups/index',
    meta: {
      title: '精英分享会',
      icon: 'Sunny',
      group: '招生端',
      permission: 'qt:activity:registrations',
    },
  },
  {
    path: '/workstations',
    name: 'Workstations',
    component: 'workstations/index',
    meta: {
      title: '工位预约',
      icon: 'OfficeBuilding',
      group: '塔员端',
      permission: 'system:reservation:list',
    },
  },
  {
    path: '/learning-materials',
    name: 'LearningMaterials',
    component: 'learning-materials/index',
    meta: {
      title: '学习资料',
      icon: 'Notebook',
      group: '塔员端',
      permission: 'qt:material:list',
    },
  },
  {
    path: '/book-borrows',
    name: 'BookBorrows',
    component: 'book-borrows/index',
    meta: {
      title: '图书借阅',
      icon: 'Reading',
      group: '塔员端',
      permission: 'system:borrow:list',
    },
  },
  {
    path: '/clothing-orders',
    name: 'ClothingOrders',
    component: 'clothing-orders/index',
    meta: {
      title: '塔服订购',
      icon: 'ShoppingBag',
      group: '塔员端',
      badge: 1,
      permission: 'system:order:list',
    },
  },
]
