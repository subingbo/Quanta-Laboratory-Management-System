import axios from 'axios'
import { getToken } from './token'
import { notifyUnauthorized } from './unauthorized'
import { mockRequest } from '@/mock'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token && config.headers?.isToken !== false) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function createRequestError(message, code, payload) {
  const error = new Error(message || '请求失败，请稍后重试')
  error.code = code
  error.payload = payload
  return error
}

export function normalizeRuoYiResponse(payload) {
  if (!payload || typeof payload !== 'object') return payload

  const code = Number(payload.code ?? 200)
  if (code !== 200) {
    throw createRequestError(payload.msg, code, payload)
  }
  return payload
}

export function isMockEnabled() {
  return String(import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false'
}

export async function request(config) {
  try {
    const response = isMockEnabled()
      ? await mockRequest(config)
      : await service.request(config)
    const payload = isMockEnabled() ? response : response.data
    return normalizeRuoYiResponse(payload)
  } catch (rawError) {
    const code = Number(
      rawError?.code ?? rawError?.response?.data?.code ?? rawError?.response?.status,
    )
    const isBusinessError = Number.isFinite(code) && rawError?.payload
    const error = isBusinessError
      ? rawError
      : createRequestError(
          rawError?.response?.data?.msg || rawError?.message || '网络连接异常',
          code || 0,
          rawError?.response?.data,
        )

    if (Number(error.code) === 401) {
      await notifyUnauthorized(error)
    }
    throw error
  }
}

export { service }
