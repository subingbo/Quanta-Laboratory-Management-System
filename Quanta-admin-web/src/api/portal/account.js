import { request } from '@/utils/request'

export function changePassword({ oldPassword, newPassword }) {
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    data: { oldPassword, newPassword },
  })
}

export function updateFreshmanEmail(email) {
  return request({
    url: '/system/user/profile/updateEmail',
    method: 'put',
    data: { email },
  })
}
