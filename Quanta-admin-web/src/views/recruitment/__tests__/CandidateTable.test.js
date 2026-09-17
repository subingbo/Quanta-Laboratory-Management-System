import { describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import CandidateTable from '../components/CandidateTable.vue'
import { useUserStore } from '@/stores/user'

const candidate = {
  applicationId: 1,
  name: '测试候选人',
  studentNo: '2026001',
  major: '软件工程',
  className: '2026级1班',
  appliedAt: '2026-08-26 10:00',
  choices: [
    {
      choiceOrder: 1,
      department: 'PRODUCT',
      rounds: {
        1: { status: 'PASS', evaluations: [] },
        2: { status: 'PENDING', advanced: true, evaluations: [] },
      },
    },
    {
      choiceOrder: 2,
      department: 'DESIGN',
      rounds: {
        1: { status: 'FAIL', evaluations: [] },
        2: { status: 'PENDING', advanced: false, evaluations: [] },
      },
    },
  ],
}

function mountTable({ roles, permissions, roundId = 1, rows = [candidate], user = { userId: 21, deptCode: 'PRODUCT' } }) {
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useUserStore()
  store.user = user
  store.roles = roles
  store.permissions = permissions

  return mount(CandidateTable, {
    props: { rows, roundId },
    global: {
      plugins: [pinia, ElementPlus],
      stubs: {
        ElTable: { template: '<div><slot /></div>' },
        ElTableColumn: {
          data: () => ({ testRow: rows[0] }),
          template: '<div><slot name="header" /><slot :row="testRow" /></div>',
        },
      },
    },
  })
}

describe('CandidateTable', () => {
  it('marks Pass and Out statuses as plain text results', () => {
    const wrapper = mountTable({
      roles: ['ceo'],
      permissions: ['qt:interview:admin:list'],
    })

    const results = wrapper.findAll('.candidate-choice__status')
    expect(results).toHaveLength(2)
    expect(results.map((result) => result.text())).toEqual(['Pass', 'Out'])
    expect(results.every((result) => result.classes().includes('is-plain-result'))).toBe(true)
    expect(results[0].classes()).toContain('status-tag--success')
    expect(results[1].classes()).toContain('status-tag--danger')
  })

  it('renders the management first-round actions', () => {
    const wrapper = mountTable({
      roles: ['qt_mgmt'],
      permissions: [
        'qt:interview:admin:list',
        'qt:interview:admin:evaluate',
      ],
    })

    expect(wrapper.text()).toContain('阅览简历')
    expect(wrapper.text()).toContain('查看面评')
    expect(wrapper.text()).toContain('编辑面评')
    expect(wrapper.text()).not.toContain('评定')
  })

  it('keeps manager first-round actions read-only', () => {
    const wrapper = mountTable({
      roles: ['qt_manager'],
      permissions: ['qt:interview:admin:list', 'qt:interview:admin:evaluate'],
    })

    expect(wrapper.text()).toContain('阅览简历')
    expect(wrapper.text()).toContain('编辑面评')
    expect(wrapper.text()).not.toContain('已评')
    expect(wrapper.text()).not.toContain('查看面评')
    expect(wrapper.text()).not.toContain('是否录用')
  })

  it('recognizes the real memberDepartment field when showing feedback actions', () => {
    const wrapper = mountTable({
      roles: ['qt_manager'],
      permissions: ['qt:interview:admin:list', 'qt:interview:admin:evaluate'],
      user: { userId: 21, memberDepartment: '产品部' },
    })

    expect(wrapper.text()).toContain('编辑面评')
  })

  it('renders the management second-round action only', () => {
    const wrapper = mountTable({
      roles: ['qt_mgmt'],
      permissions: ['qt:interview:admin:offer'],
      roundId: 2,
    })

    expect(wrapper.text()).toContain('是否录用')
    expect(wrapper.text()).not.toContain('阅览简历')
    expect(wrapper.text()).not.toContain('编辑面评')
  })

  it('renders the manager second-round action only', () => {
    const evaluatedCandidate = structuredClone(candidate)
    evaluatedCandidate.choices[0].rounds[2].evaluations = [
      { interviewerId: 21, content: '二面面评' },
    ]
    const wrapper = mountTable({
      roles: ['qt_manager'],
      permissions: ['qt:interview:admin:evaluate'],
      roundId: 2,
      rows: [evaluatedCandidate],
    })

    expect(wrapper.text()).toContain('编辑面评')
    expect(wrapper.text()).toContain('已评')
    expect(wrapper.text()).not.toContain('阅览简历')
    expect(wrapper.text()).not.toContain('是否录用')
  })
})
