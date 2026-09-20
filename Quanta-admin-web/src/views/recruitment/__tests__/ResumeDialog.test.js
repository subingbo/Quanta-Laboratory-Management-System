import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ResumeDialog from '../components/ResumeDialog.vue'

const ElDialogStub = {
  props: ['modelValue'],
  template: '<section><slot name="header" /><slot /></section>',
}

describe('ResumeDialog', () => {
  function mountDialog(props = {}) {
    return mount(ResumeDialog, {
      props: {
        modelValue: true,
        roundId: 1,
        application: { applicationId: 8, realName: '陈思思', choices: [] },
        ...props,
      },
      global: { plugins: [ElementPlus], stubs: { ElDialog: ElDialogStub } },
    })
  }

  it('renders normalized choices and existing contact fields', () => {
    const wrapper = mount(ResumeDialog, {
      props: {
        modelValue: true,
        application: {
          realName: '陈思思',
          studentNo: '20241003193',
          major: '电子商务',
          className: '2024级1班',
          email: 'student@example.com',
          phone: '13800138000',
          resumeUrl: 'https://example.com/resume.pdf?token=x',
          resumeFileName: '陈思思-简历.pdf',
          choices: [
            { choiceOrder: 2, department: 'DESIGN' },
            { choiceOrder: 1, department: 'PRODUCT' },
          ],
        },
      },
      global: { stubs: { ElDialog: ElDialogStub } },
    })

    expect(wrapper.text()).toContain('第一志愿产品部')
    expect(wrapper.text()).toContain('第二志愿设计部')
    expect(wrapper.text()).toContain('student@example.com')
    expect(wrapper.text()).toContain('13800138000')
    const preview = wrapper.get('[data-testid="resume-preview"]')
    const download = wrapper.get('[data-testid="resume-download"]')
    expect(preview.attributes('href')).toBe('https://example.com/resume.pdf?token=x')
    expect(preview.attributes('target')).toBe('_blank')
    expect(download.attributes('download')).toBe('陈思思-简历.pdf')
  })

  it('shows a clear fallback when contact information is absent', () => {
    const wrapper = mount(ResumeDialog, {
      props: { modelValue: true, application: { realName: '陈思思', choices: [] } },
      global: { stubs: { ElDialog: ElDialogStub } },
    })

    expect(wrapper.text()).toContain('邮箱暂无')
    expect(wrapper.text()).toContain('联系方式暂无')
    expect(wrapper.text()).toContain('暂无 PDF 简历')
  })

  it('shows the interview-time picker only in the first round', () => {
    const firstRoundWrapper = mountDialog({ roundId: 1 })
    const secondRoundWrapper = mountDialog({ roundId: 2 })

    expect(firstRoundWrapper.find('[data-test="interview-time-picker"]').exists()).toBe(true)
    expect(secondRoundWrapper.find('[data-test="interview-time-picker"]').exists()).toBe(false)
  })

  it('emits the selected first-round interview time', async () => {
    const wrapper = mountDialog()
    const radioGroup = wrapper.findComponent({ name: 'ElRadioGroup' })

    radioGroup.vm.$emit('update:modelValue', '2026-09-22 18:30:00')
    await wrapper.vm.$nextTick()
    await wrapper.get('[data-test="save-interview-time"]').trigger('click')

    expect(wrapper.emitted('save-interview-time')).toEqual([['2026-09-22 18:30:00']])
  })

  it('restores the saved first-round interview time when reopened', () => {
    const wrapper = mountDialog({
      application: {
        applicationId: 8,
        realName: '陈思思',
        choices: [],
        firstRoundInterviewTime: '2026-09-23 18:30:00',
      },
    })

    expect(wrapper.findComponent({ name: 'ElRadioGroup' }).props('modelValue')).toBe(
      '2026-09-23 18:30:00',
    )
  })
})
