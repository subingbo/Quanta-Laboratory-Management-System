import { beforeEach, describe, expect, it, vi } from 'vitest'
import { request } from '@/utils/request'
import { createReservation, getWorkstations } from '../workstations'

vi.mock('@/utils/request', () => ({ request: vi.fn() }))

describe('member workstation API', () => {
  beforeEach(() => request.mockReset())

  it('loads workstations with Axios GET params', async () => {
    request.mockResolvedValue({
      rows: [
        {
          workstationId: '2',
          workstationCode: 'WS-A02',
          locationDesc: 'A 区',
          capacity: '1',
          status: '0',
        },
      ],
      total: 1,
    })

    await expect(getWorkstations({ status: '0' })).resolves.toEqual({
      rows: [expect.objectContaining({ workstationId: 2, capacity: 1, available: true })],
      total: 1,
    })
    expect(request).toHaveBeenCalledWith({
      url: '/qt/workstation/list',
      method: 'get',
      params: { pageNum: 1, pageSize: 100, status: '0' },
    })
  })

  it('submits reservation times as yyyy-MM-dd HH:mm:ss', async () => {
    request.mockResolvedValue({ code: 200 })

    await createReservation({
      workstationId: '2',
      reserveStart: '2026-09-16T07:00',
      reserveEnd: '2026-09-16T12:00:00',
      purpose: '学习',
    })

    expect(request).toHaveBeenCalledWith({
      url: '/qt/reservation',
      method: 'post',
      data: {
        workstationId: 2,
        reserveStart: '2026-09-16 07:00:00',
        reserveEnd: '2026-09-16 12:00:00',
        purpose: '学习',
      },
    })
  })
})
