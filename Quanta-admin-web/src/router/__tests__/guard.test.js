import { beforeEach, describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { pinia } from '@/stores'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import { staticRoutes } from '../routes'
import { setupRouterGuard } from '../guard'

function createTestRouter() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: staticRoutes,
  })
  setupRouterGuard(router)
  return router
}

describe('global route guard', () => {
  let userStore
  let permissionStore

  beforeEach(() => {
    userStore = useUserStore(pinia)
    permissionStore = usePermissionStore(pinia)
    userStore.reset()
    permissionStore.resetRoutes()
  })

  it('redirects unauthenticated visitors and keeps the intended path', async () => {
    const router = createTestRouter()
    await router.push('/members')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/members')
  })

  it('restores user info and installs dynamic routes after login', async () => {
    const router = createTestRouter()
    await userStore.login({ username: 'admin', password: 'quanta123' })
    await router.push('/dashboard')

    expect(router.currentRoute.value.name).toBe('Dashboard')
    expect(userStore.displayName).toBe('李明华')
    expect(permissionStore.initialized).toBe(true)
    expect(router.hasRoute('Members')).toBe(true)
  })

  it('sends a logged-in user to 403 when page permission is missing', async () => {
    const router = createTestRouter()
    router.addRoute('Root', {
      path: '/admin-only',
      name: 'AdminOnly',
      component: { template: '<div>admin only</div>' },
      meta: { permission: 'system:secret:manage' },
    })
    await userStore.login({ username: 'viewer', password: 'quanta123' })
    await router.push('/admin-only')

    expect(router.currentRoute.value.path).toBe('/403')
  })
})

