import { describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AppHeader from '../components/AppHeader.vue'
import SidebarMenuItem from '../components/SidebarMenuItem.vue'

describe('admin navigation', () => {
  it('does not render static route badges as unread messages', () => {
    const wrapper = mount(SidebarMenuItem, {
      props: { route: { path: '/recruitment', meta: { title: '招新管理', badge: 2 } } },
      global: {
        stubs: {
          ElMenuItem: { template: '<div><slot name="title" /></div>' },
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.text()).toContain('招新管理')
    expect(wrapper.find('.menu-item__badge').exists()).toBe(false)
  })

  it('returns to the identity selector from the header', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: { template: '<div>身份选择</div>' } },
        { path: '/admin', component: { template: '<div />' }, meta: { title: '后台首页' } },
      ],
    })
    await router.push('/admin')
    await router.isReady()
    const wrapper = mount(AppHeader, { global: { plugins: [pinia, router, ElementPlus] } })

    await wrapper.get('[data-testid="back-to-identity"]').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/')
    wrapper.unmount()
  })
})
