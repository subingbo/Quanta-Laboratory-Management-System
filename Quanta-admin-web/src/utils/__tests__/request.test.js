import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getInfo, login } from '@/api/auth'
import { removeToken, setToken } from '@/utils/token'
import { setUnauthorizedHandler } from '@/utils/unauthorized'

describe('request and mock adapter', () => {
  beforeEach(() => {
    removeToken()
    setUnauthorizedHandler(null)
  })

  it('returns a RuoYi-shaped login response', async () => {
    const response = await login({ username: 'admin', password: 'quanta123' })
    expect(response).toMatchObject({ code: 200, token: 'mock-token-admin' })
  })

  it('rejects invalid credentials with the business message', async () => {
    await expect(login({ username: 'admin', password: 'wrong' })).rejects.toMatchObject({
      code: 500,
      message: '用户名或密码错误',
    })
  })

  it('notifies the app when a token is invalid', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    setToken('invalid-token')

    await expect(getInfo()).rejects.toMatchObject({ code: 401 })
    expect(handler).toHaveBeenCalledOnce()
  })
})

