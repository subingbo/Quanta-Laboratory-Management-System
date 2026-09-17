import { describe, expect, it, vi } from 'vitest'
import { request } from '@/utils/request'
import { changePassword, updateFreshmanEmail } from '../account'

vi.mock('@/utils/request', () => ({ request: vi.fn() }))

describe('portal account API', () => {
  it('changes the password through the real profile endpoint', async () => {
    request.mockResolvedValue({ code: 200 })

    await changePassword({ oldPassword: 'old-secret', newPassword: 'new-secret' })

    expect(request).toHaveBeenCalledWith({
      url: '/system/user/profile/updatePwd',
      method: 'put',
      data: { oldPassword: 'old-secret', newPassword: 'new-secret' },
    })
  })

  it('updates only the current freshman email through the dedicated endpoint', async () => {
    request.mockResolvedValue({ code: 200 })

    await updateFreshmanEmail('freshman@example.com')

    expect(request).toHaveBeenCalledWith({
      url: '/system/user/profile/updateEmail',
      method: 'put',
      data: { email: 'freshman@example.com' },
    })
  })
})
