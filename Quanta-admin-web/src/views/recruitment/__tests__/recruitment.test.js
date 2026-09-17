import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus, { ElSelect, ElTooltip } from 'element-plus'
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

  it('filters the loaded interview list by student name or number', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const store = useUserStore()
    store.user = { userId: 1, deptCode: 'PRODUCT' }
    store.roles = ['ceo']
    store.permissions = ['qt:interview:admin:list', 'qt:interview:admin:evaluate']
    const wrapper = mount(RecruitmentView, { global: { plugins: [pinia, ElementPlus] } })
    await flushPromises()

    await wrapper.get('[role="tablist"] button:nth-child(2)').trigger('click')
    await flushPromises()
    expect(wrapper.get('[data-testid="recruitment-filters"]').exists()).toBe(true)

    await wrapper.get('[data-testid="recruitment-keyword"]').setValue('2025004')
    await wrapper.get('[data-testid="recruitment-search"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('陈思思')
    expect(wrapper.text()).not.toContain('李小明')

    await wrapper.get('[data-testid="recruitment-reset"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('李小明')

    wrapper.findComponent(ElSelect).vm.$emit('update:modelValue', 'BACKEND')
    await wrapper.get('[data-testid="recruitment-search"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('李小明')
    expect(wrapper.text()).not.toContain('陈思思')
  })
})
