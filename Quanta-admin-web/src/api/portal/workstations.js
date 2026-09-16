import { request } from '@/utils/request'

function formatDateTime(value) {
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    const date = [
      value.getFullYear(),
      String(value.getMonth() + 1).padStart(2, '0'),
      String(value.getDate()).padStart(2, '0'),
    ].join('-')
    const time = [
      String(value.getHours()).padStart(2, '0'),
      String(value.getMinutes()).padStart(2, '0'),
      String(value.getSeconds()).padStart(2, '0'),
    ].join(':')
    return `${date} ${time}`
  }

  const match = String(value || '').match(
    /^(\d{4}-\d{2}-\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?/,
  )
  if (!match) throw new Error('预约时间格式无效')
  return `${match[1]} ${match[2]}:${match[3]}:${match[4] || '00'}`
}

function mapWorkstation(row = {}) {
  return {
    workstationId: Number(row.workstationId),
    workstationCode: row.workstationCode || '',
    locationDesc: row.locationDesc || '',
    capacity: Number(row.capacity || 0),
    status: row.status || '',
    available: row.status === '0',
  }
}

export async function getWorkstations(params = {}) {
  const response = await request({
    url: '/qt/workstation/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 100, ...params },
  })
  return {
    rows: (response.rows || []).map(mapWorkstation),
    total: Number(response.total || 0),
  }
}

export function createReservation({ workstationId, reserveStart, reserveEnd, purpose = '' }) {
  const normalizedStart = formatDateTime(reserveStart)
  const normalizedEnd = formatDateTime(reserveEnd)
  if (normalizedEnd <= normalizedStart) throw new Error('预约结束时间必须晚于开始时间')
  return request({
    url: '/qt/reservation',
    method: 'post',
    data: {
      workstationId: Number(workstationId),
      reserveStart: normalizedStart,
      reserveEnd: normalizedEnd,
      purpose,
    },
  })
}
