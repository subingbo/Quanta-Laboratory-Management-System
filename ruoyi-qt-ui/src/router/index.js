import Vue from 'vue'
import Router from 'vue-router'
import { getToken } from '@/utils/auth'

Vue.use(Router)

const routes = [
  { path: '/login', component: () => import('@/views/Login'), meta: { public: true, title: 'Quanta 登录' } },
  { path: '/', redirect: '/home' },
  { path: '/home', component: () => import('@/views/Home'), meta: { title: 'Quanta' } },
  { path: '/contacts', component: () => import('@/views/Contacts'), meta: { title: '通讯录' } },
  { path: '/services', component: () => import('@/views/Services'), meta: { title: '服务' } },
  { path: '/mine', component: () => import('@/views/Mine'), meta: { title: '我的' } },
  { path: '/services/reservation', component: () => import('@/views/Reservation'), meta: { title: '工位预约' } },
  { path: '/services/books', component: () => import('@/views/Books'), meta: { title: '图书借阅' } },
  { path: '/services/clothing', component: () => import('@/views/Clothing'), meta: { title: '塔服订购' } },
  { path: '/services/my', component: () => import('@/views/MyServices'), meta: { title: '我的服务' } },
  { path: '/account', component: () => import('@/views/AccountSecurity'), meta: { title: '账号安全' } },
  { path: '/card', component: () => import('@/views/CardEditor'), meta: { title: '创建名片' } },
  { path: '/join', component: () => import('@/views/Join'), meta: { title: 'Join us' } },
  { path: '/interview/results', component: () => import('@/views/InterviewResults'), meta: { title: '面试' } },
  { path: '*', redirect: '/home' }
]

const router = new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'Quanta'
  if (to.meta.public || getToken()) {
    next()
    return
  }
  next({ path: '/login', query: { redirect: to.fullPath } })
})

export default router
