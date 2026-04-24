import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import { getToken, removeToken, clearUser } from './auth'
import router from '@/router'

export const isRelogin = { show: false }

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 10000
})

service.interceptors.request.use(config => {
  const isToken = (config.headers || {}).isToken === false
  if (getToken() && !isToken) {
    config.headers.Authorization = 'Bearer ' + getToken()
  }
  return config
}, error => Promise.reject(error))

service.interceptors.response.use(response => {
  const data = response.data
  if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
    return data
  }
  const code = data.code || 200
  const msg = data.msg || '系统接口异常'
  if (code === 401) {
    if (!isRelogin.show) {
      isRelogin.show = true
      MessageBox.confirm('登录状态已过期，请重新登录。', '系统提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        isRelogin.show = false
        removeToken()
        clearUser()
        router.replace('/login')
      }).catch(() => {
        isRelogin.show = false
      })
    }
    return Promise.reject(new Error('登录状态已过期'))
  }
  if (code !== 200) {
    Message.error(msg)
    return Promise.reject(new Error(msg))
  }
  return data
}, error => {
  let message = error.message || '系统接口异常'
  if (message === 'Network Error') {
    message = '后端接口连接异常'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时'
  }
  Message.error(message)
  return Promise.reject(error)
})

export default service
