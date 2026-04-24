import Cookies from 'js-cookie'

const TokenKey = 'Quanta-Token'
const UserKey = 'Quanta-User'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getUser() {
  const raw = window.localStorage.getItem(UserKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setUser(user) {
  window.localStorage.setItem(UserKey, JSON.stringify(user || {}))
}

export function clearUser() {
  window.localStorage.removeItem(UserKey)
}
