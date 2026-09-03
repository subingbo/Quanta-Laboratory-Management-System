import { describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import LoginView from '../index.vue'
import { useUserStore } from '@/stores/user'

async function mountLogin() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', component: LoginView },
      { path: '/dashboard', component: { template: '<div>dashboard</div>' } },
    ],
  })
  await router.push('/login')
  await router.isReady()
  const wrapper = mount(LoginView, {
    global: { plugins: [pinia, router, ElementPlus] },
  })
  return { wrapper, router, userStore: useUserStore() }
}

describe('login view', () => {
  it('does not submit an empty form', async () => {
    const { wrapper, userStore } = await mountLogin()
    const loginSpy = vi.spyOn(userStore, 'login')

    await wrapper.find('.login-card__submit').trigger('click')
    await flushPromises()

    expect(loginSpy).not.toHaveBeenCalled()
  })

  it('logs in and enters the dashboard', async () => {
    const { wrapper, router, userStore } = await mountLogin()
    vi.spyOn(userStore, 'login').mockResolvedValue({ code: 200, token: 'test-token' })

    await wrapper.find('input[autocomplete="username"]').setValue('admin')
    await wrapper.find('input[autocomplete="current-password"]').setValue('quanta123')
    await wrapper.find('.login-card__submit').trigger('click')
    await flushPromises()

    expect(userStore.login).toHaveBeenCalledOnce()
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })
})

