import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import FeedbackDialog from '../components/FeedbackDialog.vue'

const ElDialogStub = {
  props: ['modelValue'],
  emits: ['update:modelValue'],
  template: '<section><slot name="header" /><slot /><slot name="footer" /></section>',
}

const ElButtonStub = {
  inheritAttrs: false,
  emits: ['click'],
  template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
}

const ElInputStub = {
  props: ['modelValue', 'type'],
  emits: ['update:modelValue'],
  template: '<textarea v-if="type === \'textarea\'" :value="modelValue" />',
}

function mountDialog(props = {}) {
  return mount(FeedbackDialog, {
    props: {
      modelValue: true,
      candidateName: '陈思思',
      roundId: 1,
      department: 'PRODUCT',
      options: [{ value: 'PRODUCT', label: '第一志愿 · 产品部' }],
      evaluations: [{ evaluationId: 1, interviewerName: '产品部经理', content: '面评内容' }],
      ...props,
    },
    global: {
      stubs: {
        ElDialog: ElDialogStub,
        ElButton: ElButtonStub,
        ElInput: ElInputStub,
        ElSelect: { template: '<select><slot /></select>' },
        ElOption: { template: '<option />' },
      },
    },
  })
}

describe('FeedbackDialog', () => {
  it('renders read-only feedback with Pass and Out', async () => {
    const wrapper = mountDialog({ mode: 'view' })

    expect(wrapper.find('textarea').exists()).toBe(false)
    expect(wrapper.text()).toContain('Pass')
    expect(wrapper.text()).toContain('Out')
    expect(wrapper.text()).not.toContain('Waiting')

    const actions = wrapper.findAll('[data-test^="feedback-result-"]')
    expect(actions.map((button) => button.text())).toEqual(['Pass', 'Out'])
    expect(actions[0].classes()).toContain('feedback-dialog__result--pass')
    expect(actions[1].classes()).toContain('feedback-dialog__result--out')

    await actions[0].trigger('click')
    expect(wrapper.emitted('select-result')).toEqual([['PASS']])
  })

  it('renders editable feedback without result controls', () => {
    const wrapper = mountDialog({ mode: 'edit' })

    expect(wrapper.find('textarea').exists()).toBe(true)
    expect(wrapper.text()).toContain('提交')
    expect(wrapper.text()).not.toContain('Pass')
  })
})
