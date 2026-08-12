import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

export function register(data) {
  return request({
    url: '/register',
    method: 'post',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    data
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function getCodeImg() {
  return request({
    url: '/captchaImage',
    method: 'get',
    headers: { isToken: false },
    timeout: 20000
  })
}

export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}
