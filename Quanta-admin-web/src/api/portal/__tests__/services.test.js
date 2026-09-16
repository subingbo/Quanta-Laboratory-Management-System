import { describe, expect, it, vi } from 'vitest'
import { request } from '@/utils/request'
import { getMyServices } from '../services'

vi.mock('@/utils/request', () => ({ request: vi.fn() }))

describe('member services API', () => {
  it('loads only current-user detail lists with Axios GET params', async () => {
    request
      .mockResolvedValueOnce({
        rows: [
          {
            reservationId: 1,
            workstationId: 2,
            workstationCode: 'WS-A02',
            reserveStart: '2026-09-16 07:00:00',
            reserveEnd: '2026-09-16 12:00:00',
            status: 'PENDING',
          },
        ],
      })
      .mockResolvedValueOnce({ rows: [{ borrowId: 3, bookId: 8, status: 'BORROWED' }] })
      .mockResolvedValueOnce({ rows: [{ orderId: 5, itemId: 4, status: 'DRAFT' }] })

    const result = await getMyServices()

    const params = { pageNum: 1, pageSize: 500 }
    expect(request.mock.calls.map(([config]) => config)).toEqual([
      { url: '/qt/reservation/detailList', method: 'get', params },
      { url: '/qt/borrow/detailList', method: 'get', params },
      { url: '/qt/order/detailList', method: 'get', params },
    ])
    expect(result).toEqual({
      reservations: [expect.objectContaining({ reservationId: 1, workstationId: 2 })],
      borrows: [expect.objectContaining({ borrowId: 3, bookId: 8 })],
      orders: [expect.objectContaining({ orderId: 5, itemId: 4, status: 'DRAFT' })],
    })
  })
})
