import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '../user'
import { getToken } from '@/utils/token'

vi.mock('@/api/auth', () => ({
  login: vi.fn().mockResolvedValue({ code: 200, token: 'test-token' }),
  getInfo: vi.fn().mockResolvedValue({
    code: 200,
    user: { userName: 'tester', nickName: '测试员' },
    roles: ['qt_manager'],
    permissions: ['qt:member:list'],
  }),
  logout: vi.fn().mockResolvedValue({ code: 200 }),
}))

describe('user store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('persists token after login and stores user permissions', async () => {
    const store = useUserStore()
    await store.login({ username: 'tester', password: 'secret' })
    await store.fetchUserInfo()

    expect(store.token).toBe('test-token')
    expect(getToken()).toBe('test-token')
    expect(store.displayName).toBe('测试员')
    expect(store.permissions).toEqual(['qt:member:list'])
  })

  it('clears the complete session', async () => {
    const store = useUserStore()
    await store.login({ username: 'tester', password: 'secret' })
    await store.fetchUserInfo()
    store.reset()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(store.roles).toEqual([])
    expect(store.permissions).toEqual([])
    expect(getToken()).toBe('')
  })
})
