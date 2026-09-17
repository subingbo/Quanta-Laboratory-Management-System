import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
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

})
