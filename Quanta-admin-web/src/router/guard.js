import NProgress from 'nprogress'
import { pinia } from '@/stores'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { hasAnyPermission } from '@/composables/usePermission'
import { setUnauthorizedHandler } from '@/utils/unauthorized'
import { safeInternalRedirect } from '@/utils/redirect'

const publicPaths = new Set(['/login', '/403'])

export function setupRouterGuard(router) {
  NProgress.configure({ showSpinner: false })

  setUnauthorizedHandler(async () => {
    const userStore = useUserStore(pinia)
    const permissionStore = usePermissionStore(pinia)
    const redirect = router.currentRoute.value.fullPath
    userStore.reset()
    permissionStore.resetRoutes()
    if (router.currentRoute.value.path !== '/login') {
      await router.replace({
        path: '/login',
        query: redirect === safeInternalRedirect(redirect, '') ? { redirect } : {},
      })
    }
  })

  router.beforeEach(async (to) => {
    NProgress.start()
    const userStore = useUserStore(pinia)
    const permissionStore = usePermissionStore(pinia)

    if (publicPaths.has(to.path)) {
      if (to.path === '/login' && userStore.token) return '/dashboard'
      return true
    }

    if (!userStore.token) {
      return {
        path: '/login',
        query:
          to.fullPath === safeInternalRedirect(to.fullPath, '')
            ? { redirect: to.fullPath }
            : {},
      }
    }

    try {
      if (!userStore.user) await userStore.fetchUserInfo()

      if (!permissionStore.initialized) {
        await permissionStore.generateRoutes(userStore.permissions)
        permissionStore.installRoutes(router)
        return { path: to.fullPath, replace: true }
      }

      const required = to.meta?.permission
      if (required && !hasAnyPermission(required, userStore.permissions)) {
        return '/403'
      }
      return true
    } catch {
      userStore.reset()
      permissionStore.resetRoutes()
      return {
        path: '/login',
        query:
          to.fullPath === safeInternalRedirect(to.fullPath, '')
            ? { redirect: to.fullPath }
            : {},
      }
    }
  })

  router.afterEach((to) => {
    const title = to.meta?.title
    document.title = title
      ? `${title} - Quanta 后台管理系统`
      : 'Quanta 后台管理系统'
    NProgress.done()
  })

  router.onError(() => NProgress.done())
}
