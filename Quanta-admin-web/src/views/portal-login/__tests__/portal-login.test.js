import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import PortalLogin from '../index.vue'
import { useUserStore } from '@/stores/user'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth', () => ({
  getCaptcha: vi.fn().mockResolvedValue({ captchaEnabled: false }),
  registerFreshman: vi.fn(),
  login: vi.fn(),
  getInfo: vi.fn(),
  getRouters: vi.fn(),
  logout: vi.fn(),
}))

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
  beforeEach(() => {
    vi.clearAllMocks()
    authApi.getCaptcha.mockResolvedValue({ captchaEnabled: false })
  })

  it('labels the freshman username as student number and exposes registration only there', async () => {
    const freshman = await mountLogin('freshman')
    expect(freshman.wrapper.text()).toContain('学号')
    expect(freshman.wrapper.get('input[autocomplete="username"]').attributes('placeholder')).toBe('请输入学号')
    expect(freshman.wrapper.find('[data-testid="register-mode"]').exists()).toBe(true)

    const member = await mountLogin('member')
    expect(member.wrapper.text()).toContain('用户名')
    expect(member.wrapper.find('[data-testid="register-mode"]').exists()).toBe(false)
  })

  it('validates freshman registration fields before calling the backend', async () => {
    const { wrapper } = await mountLogin('freshman')
    await wrapper.get('[data-testid="register-mode"]').trigger('click')
    await wrapper.get('input[name="studentNo"]').setValue('2024100319')
    await wrapper.get('input[name="email"]').setValue('bad-email')
    await wrapper.get('input[name="registerPassword"]').setValue('secret123')
    await wrapper.get('input[name="confirmPassword"]').setValue('different')
    await wrapper.get('[data-testid="register-submit"]').trigger('click')
    await flushPromises()

    expect(authApi.registerFreshman).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('请输入11位学号')
    expect(wrapper.text()).toContain('请输入正确的邮箱')
    expect(wrapper.text()).toContain('两次输入的密码不一致')
  })

  it('registers a freshman then returns to login with the student number filled', async () => {
    authApi.registerFreshman.mockResolvedValue({ code: 200 })
    const { wrapper } = await mountLogin('freshman')
    await wrapper.get('[data-testid="register-mode"]').trigger('click')
    await wrapper.get('input[name="studentNo"]').setValue('20241003193')
    await wrapper.get('input[name="email"]').setValue('freshman@example.com')
    await wrapper.get('input[name="registerPassword"]').setValue('secret123')
    await wrapper.get('input[name="confirmPassword"]').setValue('secret123')
    await wrapper.get('[data-testid="register-submit"]').trigger('click')
    await flushPromises()

    expect(authApi.registerFreshman).toHaveBeenCalledWith(expect.objectContaining({
      studentNo: '20241003193',
      email: 'freshman@example.com',
      password: 'secret123',
    }))
    expect(wrapper.get('input[autocomplete="username"]').element.value).toBe('20241003193')
    expect(wrapper.text()).toContain('新生登录')
  })

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
