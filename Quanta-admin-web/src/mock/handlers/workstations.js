import { findAccountByToken } from '../data/accounts'
import { mockReservationRecords } from '../data/workstations'

export const workstationHandlers = [{
  method: 'get',
  path: '/qt/reservation/detailList',
  handle(config) {
    const authorization = config.headers?.Authorization || config.headers?.authorization || ''
    const account = findAccountByToken(authorization)
    if (!account) return { code: 401, msg: '登录状态已失效' }
    if (!account.permissions.includes('*:*:*') && !account.permissions.includes('qt:reservation:list')) {
      return { code: 403, msg: '没有查看工位预约记录的权限' }
    }
    return { code: 200, msg: '操作成功', rows: mockReservationRecords, total: mockReservationRecords.length }
  },
}]
