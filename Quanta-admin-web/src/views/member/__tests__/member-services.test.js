import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import MemberServices from '@/views/member/services/index.vue'
import MemberLibrary from '@/views/member/library/index.vue'
import MemberWorkstations from '@/views/member/workstations/index.vue'
import MemberClothing from '@/views/member/clothing/index.vue'
import { getMyServices } from '@/api/portal/services'
import { borrowBook, getBooks } from '@/api/portal/library'
import { createReservation, getWorkstations } from '@/api/portal/workstations'
import { createClothingOrder, getClothingItems } from '@/api/portal/clothing'
import { ElMessage } from 'element-plus'

vi.mock('@/api/portal/services', () => ({ getMyServices: vi.fn() }))
vi.mock('@/api/portal/library', () => ({ getBooks: vi.fn(), borrowBook: vi.fn() }))
vi.mock('@/api/portal/workstations', () => ({
  getWorkstations: vi.fn(),
  createReservation: vi.fn(),
}))
vi.mock('@/api/portal/clothing', () => ({
  getClothingItems: vi.fn(),
  createClothingOrder: vi.fn(),
}))
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn() },
}))

const workstation = {
  workstationId: 2,
  workstationCode: 'WS-A02',
  locationDesc: 'A 区',
  available: true,
}

beforeEach(() => {
  vi.clearAllMocks()
  getMyServices.mockResolvedValue({ reservations: [], borrows: [], orders: [] })
  getBooks.mockResolvedValue({
    rows: [{ bookId: 8, bookName: 'Vue.js 设计与实现', bookType: 'TEXTBOOK', available: true }],
    total: 1,
  })
  getWorkstations.mockResolvedValue({ rows: [workstation], total: 1 })
  getClothingItems.mockResolvedValue([{
    itemId: 4,
    itemName: 'Quanta 塔服',
    colors: ['黑色'],
    sizes: ['L'],
    price: null,
    priceLabel: '以实物通知为准',
  }])
})

describe('member self-service pages', () => {
  it('renders service history in three desktop tabs', async () => {
    const wrapper = mount(MemberServices)
    await flushPromises()

    expect(wrapper.get('[role="tablist"]').text()).toContain('工位预约')
    expect(wrapper.get('[role="tablist"]').text()).toContain('图书借阅')
    expect(wrapper.get('[role="tablist"]').text()).toContain('塔服订单')
    expect(wrapper.text()).toContain('暂无工位预约记录')
  })

  it('does not report a reservation until the backend succeeds', async () => {
    createReservation.mockRejectedValue(new Error('该时段已被预约'))
    const wrapper = mount(MemberWorkstations)
    await flushPromises()

    await wrapper.get('input[name="reserveDate"]').setValue('2026-09-16')
    await wrapper.get('input[name="reserveStartTime"]').setValue('07:00')
    await wrapper.get('input[name="reserveEndTime"]').setValue('12:00')
    await wrapper.get('[data-testid="reserve-button-2"]').trigger('click')
    await flushPromises()

    expect(createReservation).toHaveBeenCalledWith(expect.objectContaining({
      workstationId: 2,
      reserveStart: '2026-09-16T07:00:00',
      reserveEnd: '2026-09-16T12:00:00',
    }))
    expect(ElMessage.success).not.toHaveBeenCalled()
    expect(ElMessage.error).toHaveBeenCalledWith('该时段已被预约')
  })

  it('prevents duplicate workstation submissions and refreshes after success', async () => {
    let resolveReservation
    createReservation.mockReturnValue(new Promise((resolve) => { resolveReservation = resolve }))
    const wrapper = mount(MemberWorkstations)
    await flushPromises()

    const button = wrapper.get('[data-testid="reserve-button-2"]')
    await button.trigger('click')
    await button.trigger('click')
    expect(createReservation).toHaveBeenCalledOnce()

    resolveReservation({ code: 200 })
    await flushPromises()
    expect(getWorkstations).toHaveBeenCalledTimes(2)
    expect(ElMessage.success).toHaveBeenCalledWith('工位预约成功')
  })

  it('submits a selected book with a date-only due time', async () => {
    borrowBook.mockResolvedValue({ code: 200 })
    const wrapper = mount(MemberLibrary)
    await flushPromises()

    await wrapper.get('[data-testid="borrow-book-8"]').trigger('click')
    await wrapper.get('input[name="dueTime"]').setValue('2026-10-01')
    await wrapper.get('[data-testid="confirm-borrow"]').trigger('click')
    await flushPromises()

    expect(borrowBook).toHaveBeenCalledWith(8, '2026-10-01')
    expect(getBooks).toHaveBeenCalledTimes(2)
    expect(ElMessage.success).toHaveBeenCalledWith('图书借阅成功')
  })

  it('saves clothing only as a DRAFT without inventing a price', async () => {
    createClothingOrder.mockResolvedValue({ status: 'DRAFT' })
    const wrapper = mount(MemberClothing)
    await flushPromises()

    expect(wrapper.text()).toContain('以实物通知为准')
    expect(wrapper.get('[data-testid="save-order-draft"]').text()).toBe('保存订购草稿')
    await wrapper.get('[data-testid="save-order-draft"]').trigger('click')
    await flushPromises()

    expect(createClothingOrder).toHaveBeenCalledWith(
      expect.objectContaining({ itemId: 4, price: null }),
      { color: '黑色', size: 'L', quantity: 1 },
    )
    expect(ElMessage.success).toHaveBeenCalledWith('订购草稿已保存')
  })
})
