import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/portal/account'
import PortalSecurity from '../index.vue'

vi.mock('@/api/portal/account', () => ({ changePassword: vi.fn() }))
vi.mock('element-plus', () => ({ ElMessage: { success: vi.fn(), error: vi.fn() } }))

describe('portal account security', () => {
  beforeEach(() => vi.clearAllMocks())

  it('validates confirmation before calling the backend', async () => {
    const wrapper = mount(PortalSecurity)
    await wrapper.get('input[name="oldPassword"]').setValue('old-secret')
    await wrapper.get('input[name="newPassword"]').setValue('new-secret')
    await wrapper.get('input[name="confirmPassword"]').setValue('different')
    await wrapper.get('form').trigger('submit')

    expect(changePassword).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('两次输入的新密码不一致')
  })

  it('reports success only after the real request succeeds', async () => {
    changePassword.mockResolvedValue({ code: 200 })
    const wrapper = mount(PortalSecurity)
    await wrapper.get('input[name="oldPassword"]').setValue('old-secret')
    await wrapper.get('input[name="newPassword"]').setValue('new-secret')
    await wrapper.get('input[name="confirmPassword"]').setValue('new-secret')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(changePassword).toHaveBeenCalledWith({ oldPassword: 'old-secret', newPassword: 'new-secret' })
    expect(ElMessage.success).toHaveBeenCalledWith('密码修改成功')
    expect(wrapper.get('input[name="oldPassword"]').element.value).toBe('')
  })

  it('keeps failure visible and never reports success', async () => {
    changePassword.mockRejectedValue(new Error('当前密码错误'))
    const wrapper = mount(PortalSecurity)
    await wrapper.get('input[name="oldPassword"]').setValue('wrong-old')
    await wrapper.get('input[name="newPassword"]').setValue('new-secret')
    await wrapper.get('input[name="confirmPassword"]').setValue('new-secret')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('当前密码错误')
    expect(ElMessage.success).not.toHaveBeenCalled()
  })
})
