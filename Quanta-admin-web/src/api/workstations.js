import { request } from '@/utils/request'

export async function getReservationRecords(params = {}) {
  const response = await request({
    url: '/system/reservation/detailList',
    method: 'get',
    params,
  })
  return { rows: response.rows || [], total: Number(response.total) || 0 }
}
