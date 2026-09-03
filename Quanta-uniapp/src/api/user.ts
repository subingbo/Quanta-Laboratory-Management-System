import request from '../utils/request'

export type ChangePasswordData = {
  oldPassword: string
  newPassword: string
}

export const submitChangePasswordApi = (data: ChangePasswordData) => {
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'PUT',
    data,
  })
}
