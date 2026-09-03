import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import LearningMaterialsView from '../index.vue'
import { useUserStore } from '@/stores/user'
import { resetMockMaterials } from '@/mock/data/materials'
import { setToken } from '@/utils/token'

function mountFor(token, permissions, roles) {
  setToken(token)
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useUserStore()
  store.permissions = permissions
  store.roles = roles
  return mount(LearningMaterialsView, { global: { plugins: [pinia, ElementPlus] } })
}

describe('learning materials view', () => {
  beforeEach(resetMockMaterials)

  it('shows upload and delete controls to management', async () => {
    const wrapper = mountFor(
      'mock-token-product-manager',
      ['qt:material:list', 'qt:material:add', 'qt:material:remove'],
      ['qt_mgmt'],
    )
    await flushPromises()
    expect(wrapper.text()).toContain('点击或拖拽文件到此处上传')
    expect(wrapper.text()).toContain('删除')
  })

  it('renders a read-only list for managers', async () => {
    const wrapper = mountFor('mock-token-product-interviewer', ['qt:material:list'], ['qt_manager'])
    await flushPromises()
    expect(wrapper.text()).toContain('React入门教程.pdf')
    expect(wrapper.text()).not.toContain('点击或拖拽文件到此处上传')
    expect(wrapper.text()).not.toContain('删除')
  })
})
