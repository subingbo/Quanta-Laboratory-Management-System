import { request } from '@/utils/request'

export async function getClothingOrders(params = {}) {
  const response = await request({ url: '/system/order/detailList', method: 'get', params })
  return { rows: response.rows || [], total: Number(response.total) || 0 }
}

export function approveClothingOrder(orderId) {
  return request({ url: `/system/order/${orderId}/approve`, method: 'put' })
}
