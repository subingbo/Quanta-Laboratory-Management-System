import { describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import PortalLogin from '../index.vue'
import { useUserStore } from '@/stores/user'

async function mountLogin(audience = 'freshman', redirect = '') {
  const pinia = createPinia()
  setActivePinia(pinia)
  const path = audience === 'freshman' ? '/login/freshman' : '/login/member'
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path, component: PortalLogin, meta: { portalAudience: audience } },
      { path: '/freshman/home', component: { template: '<div />' } },
      { path: '/member/home', component: { template: '<div />' } },
    ],
  })
  await router.push({ path, query: redirect ? { redirect } : {} })
  await router.isReady()
  const wrapper = mount(PortalLogin, { global: { plugins: [pinia, router, ElementPlus] } })
  return { wrapper, router, store: useUserStore() }
}

describe('portal login', () => {
  it('submits the audience selected by the route', async () => {
    const { wrapper, store } = await mountLogin('freshman')
    const login = vi.spyOn(store, 'login').mockResolvedValue({ token: 'token' })
    vi.spyOn(store, 'fetchUserInfo').mockResolvedValue({})

    await wrapper.get('input[autocomplete="username"]').setValue('qt_fresh')
    await wrapper.get('input[autocomplete="current-password"]').setValue('admin123')
    await wrapper.get('.el-button').trigger('click')
    await flushPromises()

    expect(login).toHaveBeenCalledWith(expect.objectContaining({ username: 'qt_fresh' }), 'freshman')
  })

  it('rejects an external redirect and enters the audience home', async () => {
    const { wrapper, router, store } = await mountLogin('member', '//evil.example')
    vi.spyOn(store, 'login').mockResolvedValue({ token: 'token' })
    vi.spyOn(store, 'fetchUserInfo').mockResolvedValue({})

    await wrapper.get('input[autocomplete="username"]').setValue('qt_member')
    await wrapper.get('input[autocomplete="current-password"]').setValue('admin123')
    await wrapper.get('.el-button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/member/home')
  })
})
