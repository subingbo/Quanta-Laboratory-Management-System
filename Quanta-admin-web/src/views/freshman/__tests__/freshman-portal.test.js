import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as recruitmentApi from '@/api/portal/recruitment'
import * as activityApi from '@/api/portal/activities'
import * as draftStorage from '@/utils/recruitment-draft'
import ApplicationForm, {
  emptyApplication,
} from '@/views/freshman/recruitment/components/ApplicationForm.vue'
import InterviewTimeline from '@/views/freshman/recruitment/components/InterviewTimeline.vue'
import RecruitmentPage from '@/views/freshman/recruitment/index.vue'
import EventsPage from '@/views/freshman/events/index.vue'

vi.mock('@/api/portal/recruitment', () => ({
  recruitmentDepartments: [
    { value: 'PRODUCT', label: '产品' },
    { value: 'DESIGN', label: '设计' },
    { value: 'FRONTEND', label: '全栈（前端）' },
    { value: 'BACKEND', label: '全栈（后端）' },
  ],
  getMyApplication: vi.fn(),
  getMyInterviewProcess: vi.fn(),
  submitApplication: vi.fn(),
}))

vi.mock('@/api/portal/activities', () => ({
  getPublishedActivities: vi.fn(),
  getMyActivitySignup: vi.fn(),
  signupActivity: vi.fn(),
}))

vi.mock('@/utils/recruitment-draft', () => ({
  loadRecruitmentDraft: vi.fn(),
  saveRecruitmentDraft: vi.fn(),
  clearRecruitmentDraft: vi.fn(),
}))

describe('freshman recruitment components', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    recruitmentApi.getMyApplication.mockResolvedValue(null)
    recruitmentApi.getMyInterviewProcess.mockResolvedValue([])
    recruitmentApi.submitApplication.mockResolvedValue({ code: 200 })
    draftStorage.loadRecruitmentDraft.mockReturnValue(null)
  })

  it('offers only active departments and submits a browser form model', async () => {
    const wrapper = mount(ApplicationForm, {
      props: { initialValue: emptyApplication() },
    })

    expect(wrapper.text()).toContain('全栈（后端）')
    expect(wrapper.text()).not.toContain('安卓')
    expect(wrapper.findAll('select[name$="Choice"] option:not([disabled])')).toHaveLength(8)
    await wrapper.find('input[name="realName"]').setValue('新生小李')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toHaveLength(1)
    expect(wrapper.emitted('submit')[0][0].realName).toBe('新生小李')
  })

  it('renders interview invitations as read-only status', () => {
    const wrapper = mount(InterviewTimeline, {
      props: {
        processes: [
          {
            departmentCode: 'FRONTEND',
            departmentName: '全栈（前端）',
            stages: [
              { key: 'first', title: '一面', status: 'passed' },
              { key: 'second', title: '二面', status: 'invited' },
              { key: 'offer', title: '录用', status: 'locked' },
            ],
          },
        ],
      },
    })

    expect(wrapper.text()).toContain('等待面试')
    expect(wrapper.text()).not.toContain('接受邀请')
    expect(wrapper.text()).not.toContain('拒绝邀请')
    expect(wrapper.findAll('button')).toHaveLength(0)
  })

  it('clears the draft only after the backend accepts an application', async () => {
    const wrapper = mount(RecruitmentPage)
    await flushPromises()
    const payload = { ...emptyApplication(), realName: '新生小李' }

    wrapper.findComponent(ApplicationForm).vm.$emit('submit', payload)
    await flushPromises()

    expect(recruitmentApi.submitApplication).toHaveBeenCalledWith(payload)
    expect(draftStorage.clearRecruitmentDraft).toHaveBeenCalledOnce()
    expect(wrapper.text()).toContain('报名提交成功')
  })

  it('retains the draft when application submission fails', async () => {
    recruitmentApi.submitApplication.mockRejectedValueOnce(new Error('网络异常'))
    const wrapper = mount(RecruitmentPage)
    await flushPromises()
    const payload = { ...emptyApplication(), realName: '新生小李' }

    wrapper.findComponent(ApplicationForm).vm.$emit('submit', payload)
    await flushPromises()

    expect(draftStorage.clearRecruitmentDraft).not.toHaveBeenCalled()
    expect(draftStorage.saveRecruitmentDraft).toHaveBeenCalledWith(payload)
    expect(wrapper.text()).toContain('网络异常')
  })

  it('restores a newer local draft over the last server application', async () => {
    recruitmentApi.getMyApplication.mockResolvedValueOnce({
      ...emptyApplication(),
      realName: '服务端旧姓名',
      firstChoice: 'PRODUCT',
    })
    draftStorage.loadRecruitmentDraft.mockReturnValueOnce({
      realName: '草稿新姓名',
      firstChoice: 'FRONTEND',
    })

    const wrapper = mount(RecruitmentPage)
    await flushPromises()

    expect(wrapper.findComponent(ApplicationForm).props('initialValue')).toEqual(
      expect.objectContaining({ realName: '草稿新姓名', firstChoice: 'FRONTEND' }),
    )
  })

  it('keeps a successful submission successful when the process refresh fails', async () => {
    recruitmentApi.getMyInterviewProcess
      .mockResolvedValueOnce([])
      .mockRejectedValueOnce(new Error('进度刷新失败'))
    const wrapper = mount(RecruitmentPage)
    await flushPromises()
    const payload = { ...emptyApplication(), realName: '新生小李' }

    wrapper.findComponent(ApplicationForm).vm.$emit('submit', payload)
    await flushPromises()

    expect(draftStorage.clearRecruitmentDraft).toHaveBeenCalledOnce()
    expect(draftStorage.saveRecruitmentDraft).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('报名提交成功')
  })
})

describe('freshman activities', () => {
  it('disables signup while requesting and refreshes the signup after success', async () => {
    let resolveSignup
    activityApi.getPublishedActivities.mockResolvedValue([
      {
        activityId: 7,
        activityType: 'LECTURE',
        title: '新生宣讲会',
        activityStart: '2026-09-20',
        locationDesc: '教学楼 A101',
        status: 'PUBLISHED',
      },
    ])
    activityApi.getMyActivitySignup
      .mockResolvedValueOnce(null)
      .mockResolvedValueOnce({ activityId: 7, status: 'APPLIED' })
    activityApi.signupActivity.mockReturnValue(
      new Promise((resolve) => {
        resolveSignup = resolve
      }),
    )
    const wrapper = mount(EventsPage)
    await flushPromises()
    const button = wrapper.get('[data-testid="signup-7"]')

    await button.trigger('click')
    expect(button.attributes('disabled')).toBeDefined()
    expect(button.text()).toContain('报名中')

    resolveSignup({ code: 200 })
    await flushPromises()

    expect(activityApi.getMyActivitySignup).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('已报名')
  })
})
