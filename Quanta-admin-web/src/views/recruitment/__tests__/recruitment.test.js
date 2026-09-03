import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus, { ElTooltip } from 'element-plus'
import RecruitmentView from '../index.vue'
import { resetMockRecruitment } from '@/mock/data/recruitment'
import { useUserStore } from '@/stores/user'
import { setToken } from '@/utils/token'

describe('recruitment view', () => {
  beforeEach(() => {
    resetMockRecruitment()
    setToken('mock-token-product-manager')
  })

  it('renders the board and a department-scoped first-round list', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const store = useUserStore()
    store.user = { userId: 11, deptCode: 'PRODUCT', dept: { deptName: '产品部', deptCode: 'PRODUCT' } }
    store.roles = ['qt_mgmt']
    store.permissions = [
      'qt:interview:admin:list',
      'qt:interview:admin:evaluate',
      'qt:interview:admin:export',
    ]
    const wrapper = mount(RecruitmentView, { global: { plugins: [pinia, ElementPlus] } })
    await flushPromises()

    expect(wrapper.text()).toContain('招聘看板')
    expect(wrapper.text()).toContain('投递总数')

    await wrapper.get('[role="tablist"] button:nth-child(2)').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('陈思思')
    expect(wrapper.text()).toContain('产品部')

    await wrapper.get('[role="tablist"] button:nth-child(3)').trigger('click')
    await flushPromises()
    expect(wrapper.findComponent(ElTooltip).props('content')).toBe('导出的内容是当前页面筛选结果')
    expect(wrapper.findComponent(ElTooltip).props('disabled')).toBe(false)
  })
})
