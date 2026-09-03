import { beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import WorkstationsView from '../index.vue'
import { setToken } from '@/utils/token'

describe('workstation records view', () => {
  beforeEach(() => setToken('mock-token-product-manager'))

  it('renders compact read-only reservation records', async () => {
    const wrapper = mount(WorkstationsView, { global: { plugins: [ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('工位预约记录')
    expect(wrapper.text()).toContain('A-03')
    expect(wrapper.text()).toContain('已预约')
    expect(wrapper.text()).not.toContain('新增预约')
  })
})
