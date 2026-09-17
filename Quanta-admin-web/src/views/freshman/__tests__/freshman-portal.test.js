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

const elementMocks = vi.hoisted(() => ({ alert: vi.fn().mockResolvedValue('confirm') }))

vi.mock('element-plus', () => ({ ElMessageBox: { alert: elementMocks.alert } }))

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
  loadRecruitmentPhotoDraft: vi.fn(),
  saveRecruitmentPhotoDraft: vi.fn(),
  clearRecruitmentPhotoDraft: vi.fn(),
}))

describe('freshman recruitment components', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    recruitmentApi.getMyApplication.mockResolvedValue(null)
    recruitmentApi.getMyInterviewProcess.mockResolvedValue([])
    recruitmentApi.submitApplication.mockResolvedValue({ code: 200 })
    draftStorage.loadRecruitmentDraft.mockReturnValue(null)
    draftStorage.loadRecruitmentPhotoDraft.mockResolvedValue(null)
    draftStorage.saveRecruitmentPhotoDraft.mockResolvedValue(undefined)
    draftStorage.clearRecruitmentPhotoDraft.mockResolvedValue(undefined)
    elementMocks.alert.mockResolvedValue('confirm')
  })

  it('offers only active departments and submits a browser form model', async () => {
    const wrapper = mount(ApplicationForm, {
      props: { initialValue: emptyApplication() },
    })

    expect(wrapper.text()).toContain('全栈（后端）')
    expect(wrapper.text()).not.toContain('安卓')
    expect(wrapper.findAll('select[name="gender"] option:not([disabled])')).toHaveLength(2)
    expect(wrapper.findAll('select[name$="Choice"] option:not([disabled])')).toHaveLength(8)
    await wrapper.find('input[name="realName"]').setValue('新生小李')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toHaveLength(1)
    expect(wrapper.emitted('submit')[0][0].realName).toBe('新生小李')
  })

  it('renders a branded photo picker and previews a valid image', async () => {
    const createObjectURL = vi.fn(() => 'blob:photo-preview')
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: createObjectURL })
    const wrapper = mount(ApplicationForm, {
      props: { initialValue: emptyApplication() },
    })
    const input = wrapper.get('input[name="photoFile"]')
    const photo = new File(['photo'], 'quanta-photo.png', { type: 'image/png' })

    expect(wrapper.get('[data-testid="photo-upload-card"]').text()).toContain('选择照片')
    expect(input.classes()).toContain('sr-only')
    Object.defineProperty(input.element, 'files', { configurable: true, value: [photo] })
    await input.trigger('change')

    expect(createObjectURL).toHaveBeenCalledWith(photo)
    expect(wrapper.get('[data-testid="photo-file-name"]').text()).toBe('quanta-photo.png')
    expect(wrapper.get('img[alt="证件照预览"]').attributes('src')).toBe('blob:photo-preview')
  })

  it('rejects unsupported and oversized photo files in place', async () => {
    const wrapper = mount(ApplicationForm, {
      props: { initialValue: emptyApplication() },
    })
    const input = wrapper.get('input[name="photoFile"]')
    const unsupported = new File(['text'], 'notes.txt', { type: 'text/plain' })

    Object.defineProperty(input.element, 'files', { configurable: true, value: [unsupported] })
    await input.trigger('change')
    expect(wrapper.get('[data-testid="photo-error"]').text()).toContain('JPG 或 PNG')

    const webp = new File(['photo'], 'photo.webp', { type: 'image/webp' })
    Object.defineProperty(input.element, 'files', { configurable: true, value: [webp] })
    await input.trigger('change')
    expect(wrapper.get('[data-testid="photo-error"]').text()).toContain('JPG 或 PNG')

    const oversized = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'large.jpg', {
      type: 'image/jpeg',
    })
    Object.defineProperty(input.element, 'files', { configurable: true, value: [oversized] })
    await input.trigger('change')
    expect(wrapper.get('[data-testid="photo-error"]').text()).toContain('不能超过 5 MB')
  })

  it('falls back to the clean placeholder when a stored photo cannot load', async () => {
    const wrapper = mount(ApplicationForm, {
      props: { initialValue: { ...emptyApplication(), photoUrl: '/expired-photo.jpg' } },
    })

    await wrapper.get('img[alt="证件照预览"]').trigger('error')

    expect(wrapper.find('img[alt="证件照预览"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="photo-file-name"]').text()).toContain('暂时无法预览')
    expect(wrapper.get('[data-testid="photo-upload-card"]').text()).toContain('证件照预览')
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
    expect(draftStorage.clearRecruitmentPhotoDraft).toHaveBeenCalledOnce()
    expect(elementMocks.alert).toHaveBeenCalledWith('报名已成功提交，可在“查看进度”中查看后续安排。', '提交成功', expect.any(Object))
    expect(wrapper.get('.freshman-tabs button.active').text()).toBe('查看进度')
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
    expect(elementMocks.alert).toHaveBeenCalledWith('网络连接异常，请稍后重试', '提交失败', expect.objectContaining({ type: 'error' }))
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
    expect(elementMocks.alert).toHaveBeenCalledWith('报名已成功提交，可在“查看进度”中查看后续安排。', '提交成功', expect.any(Object))
  })

  it('saves the selected photo with the browser draft', async () => {
    const wrapper = mount(RecruitmentPage)
    await flushPromises()
    const photo = new File(['photo'], 'photo.png', { type: 'image/png' })
    const payload = { ...emptyApplication(), realName: '新生小李', photoFile: photo }

    wrapper.findComponent(ApplicationForm).vm.$emit('save-draft', payload)
    await flushPromises()

    expect(draftStorage.saveRecruitmentDraft).toHaveBeenCalledWith(payload)
    expect(draftStorage.saveRecruitmentPhotoDraft).toHaveBeenCalledWith(photo)
    expect(elementMocks.alert).toHaveBeenCalledWith('报名信息和证件照已保存在当前浏览器。', '草稿已保存', expect.any(Object))
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
