import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus, { ElPagination, ElSelect, ElTooltip } from 'element-plus'
import RecruitmentView from '../index.vue'
import { mockRecruitmentApplications, resetMockRecruitment } from '@/mock/data/recruitment'
import { useUserStore } from '@/stores/user'
import { setToken } from '@/utils/token'

describe('recruitment view', () => {
  beforeEach(() => {
    resetMockRecruitment()
    setToken('mock-token-product-manager')
  })

  function appendCandidates(count) {
    for (let index = 0; index < count; index += 1) {
      mockRecruitmentApplications.push({
        applicationId: 9000 + index,
        userId: 9000 + index,
        realName: `批量候选${index}`,
        studentNo: `20259${String(index).padStart(4, '0')}`,
        major: '软件工程',
        className: '测试班',
        email: `candidate${index}@stu.edu`,
        phone: '13800000000',
        appliedAt: '2026-09-19 20:00',
        applicationStatus: 'SUBMITTED',
        tracks: [
          { choiceOrder: 1, department: 'PRODUCT', rounds: { 1: { status: 'PENDING', evaluations: [] }, 2: { status: 'PENDING', advanced: false, evaluations: [] } } },
          { choiceOrder: 2, department: 'FRONTEND', rounds: { 1: { status: 'PENDING', evaluations: [] }, 2: { status: 'PENDING', advanced: false, evaluations: [] } } },
        ],
      })
    }
  }

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

  it('paginates each interview round and finds keyword matches beyond the first server page', async () => {
    appendCandidates(105)
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
    const pagination = wrapper.findComponent(ElPagination)
    expect(pagination.props('currentPage')).toBe(1)
    expect(pagination.props('pageSize')).toBe(10)
    expect(pagination.props('total')).toBeGreaterThan(100)

    pagination.vm.$emit('current-change', 2)
    await flushPromises()
    expect(wrapper.findComponent(ElPagination).props('currentPage')).toBe(2)

    await wrapper.get('[data-testid="recruitment-keyword"]').setValue('批量候选104')
    await wrapper.get('[data-testid="recruitment-search"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('批量候选104')
    expect(wrapper.text()).toContain('当前显示 1 / 1 人')
  })
})
