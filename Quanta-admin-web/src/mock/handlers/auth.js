import { mockAccounts, findAccountByToken } from '../data/accounts'
import { mockRouters } from '../data/routers'

function requireAccount(config) {
  const authorization = config.headers?.Authorization || config.headers?.authorization || ''
  return findAccountByToken(authorization)
}

function hasPermission(account, permission) {
  return account.permissions.includes('*:*:*') || account.permissions.includes(permission)
}

export const authHandlers = [
  {
    method: 'post',
    path: '/login',
    handle(config) {
      const { username, password } = config.data || {}
      const account = mockAccounts.find((item) => item.username === username)
      if (!account || account.password !== password) {
        return { code: 500, msg: '用户名或密码错误' }
      }
      return { code: 200, msg: '登录成功', token: account.token }
    },
  },
  {
    method: 'get',
    path: '/getInfo',
    handle(config) {
      const account = requireAccount(config)
      if (!account) return { code: 401, msg: '登录状态已失效' }
      return {
        code: 200,
        msg: '操作成功',
        user: account.user,
        roles: account.roles,
        permissions: account.permissions,
      }
    },
  },
  {
    method: 'get',
    path: '/getRouters',
    handle(config) {
      const account = requireAccount(config)
      if (!account) return { code: 401, msg: '登录状态已失效' }
      const routes = mockRouters.filter((route) => hasPermission(account, route.meta.permission))
      return { code: 200, msg: '操作成功', data: routes }
    },
  },
  {
    method: 'post',
    path: '/logout',
    handle() {
      return { code: 200, msg: '退出成功' }
    },
  },
  {
    method: 'get',
    path: '/captchaImage',
    handle() {
      return { code: 200, msg: '操作成功', captchaEnabled: false }
    },
  },
]

