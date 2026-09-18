import { readFileSync } from 'node:fs'
import { join } from 'node:path'
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

  it('uses the Quanta logo for the browser and admin sidebar', () => {
    const html = readFileSync(join(process.cwd(), 'index.html'), 'utf8')
    const sidebar = readFileSync(join(process.cwd(), 'src/layout/components/AppSidebar.vue'), 'utf8')

    expect(html).toContain('type="image/png"')
    expect(html).toContain('href="/quanta-favicon.png"')
    expect(sidebar).toContain("'/quanta-logo.jpg'")
    expect(sidebar).toContain('alt="Quanta 社团 Logo"')
  })

})
