import { request } from '@/utils/request'

export function changePassword({ oldPassword, newPassword }) {
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    data: { oldPassword, newPassword },
  })
}
