import { LoginData, LoginResponse } from '../types/auth'
import request from '../utils/request'

export const loginApi = (data: LoginData): Promise<LoginResponse> => {
  // 后端接口尚未提供时，仅在 H5/小程序开发服务中启用可替换的演示登录。
  // 生产构建中 import.meta.env.DEV 为 false，仍调用真实 /login。
  if (import.meta.env.DEV) {
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
