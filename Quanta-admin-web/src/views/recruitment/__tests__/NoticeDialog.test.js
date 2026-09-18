import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import NoticeDialog from '../components/NoticeDialog.vue'

describe('NoticeDialog', () => {
  it('requires a QR image for an admission email', async () => {
    const wrapper = mount(NoticeDialog, {
      props: { modelValue: true, preview: { result: 'PASS', offeredDepartment: 'FRONTEND', subject: '录用通知', content: '固定模板' } },
      global: { stubs: {
        ElDialog: { template: '<section><slot name="header"/><slot/><slot name="footer"/></section>' },
        ElInput: { props: ['modelValue'], template: '<textarea :value="modelValue" />' },
        ElButton: { emits: ['click'], template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot/></button>' },
      } },
    })
    await wrapper.get('[data-test="send-notice"]').trigger('click')
    expect(wrapper.text()).toContain('录用邮件必须添加群二维码')
    expect(wrapper.emitted('submit')).toBeUndefined()
  })
})
