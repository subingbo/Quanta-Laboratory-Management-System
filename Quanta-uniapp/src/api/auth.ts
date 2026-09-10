import { LoginData, LoginResponse } from '../types/auth'
import request from '../utils/request'

/** 演示登录开关:仅在 .env 显式设置 VITE_USE_MOCK=true 时启用,联调/生产均直连真实 /login */
const USE_MOCK_LOGIN = String(import.meta.env.VITE_USE_MOCK ?? 'false') === 'true'

export const loginApi = (data: LoginData): Promise<LoginResponse> => {
  if (USE_MOCK_LOGIN) {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ code: 200, msg: '演示登录成功', token: `mock-${data.loginType}-${data.username}` }), 260)
    })
  }
  return request<LoginResponse>({
    url: '/login',
    method: 'POST',
    data,
  })
}


export const towerMemberLoginApi = loginApi
