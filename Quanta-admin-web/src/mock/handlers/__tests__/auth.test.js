import { describe, expect, it } from 'vitest'
import { authHandlers } from '../auth'

function handler(method, path) {
  return authHandlers.find((item) => item.method === method && item.path === path)
}

function config(token) {
  return { headers: { Authorization: `Bearer ${token}` } }
}

describe('auth mock handlers', () => {
  it('returns the confirmed manager menu', () => {
    const response = handler('get', '/getRouters').handle(
      config('mock-token-product-interviewer'),
    )

    expect(response.code).toBe(200)
    expect(response.data.map((route) => route.name)).toEqual([
      'Dashboard',
      'Members',
      'Recruitment',
      'LearningMaterials',
      'BookBorrows',
    ])
  })
})
