import type { LibraryBook, BorrowBookResult } from '../utils/libraryMock'
import { calculateBookDueDate, type BookCategory } from '../utils/libraryRules'
import request from '../utils/request'

/**
 * 图书借阅 —— 真实后端对接层。
 * 页面契约与 utils/libraryMock 保持一致(LibraryBook / BorrowBookResult),
 * 数据源切换为 /qt/book + /qt/borrow。
 */

interface QtBookRow {
  bookId: number
  bookName?: string
  author?: string
  publisher?: string
  publishDate?: string
  totalCount?: number
  availableCount?: number
  locationDesc?: string
  status?: string // "0"可借 "1"停借
  bookType?: string // 图书类型(如 TEXTBOOK / GENERAL)
}

const pad3 = (value: number) => String(value).padStart(3, '0')
const dateOnly = (value: Date) =>
  `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`

const toLibraryBook = (row: QtBookRow): LibraryBook => {
  const stopped = String(row.status) === '1'
  const available = !stopped && Number(row.availableCount ?? 0) > 0
  const category: BookCategory = String(row.bookType).toUpperCase() === 'TEXTBOOK' ? 'textbook' : 'general'
  return {
    id: String(row.bookId),
    code: pad3(row.bookId),
    cabinet: row.locationDesc || '',
    category,
    required: false, // 后端暂无"必读"标记,默认非必读
    status: available ? 'available' : 'borrowed',
  }
}

export const getLibraryBooks = async (): Promise<LibraryBook[]> => {
  const res = await request<{ rows?: QtBookRow[] }>({ url: '/qt/book/list?pageNum=1&pageSize=500', method: 'GET' })
  return (res?.rows || []).map(toLibraryBook)
}

const fetchBook = async (bookId: string): Promise<LibraryBook> => {
  const res = await request<{ data?: QtBookRow }>({ url: `/qt/book/${bookId}`, method: 'GET' })
  return toLibraryBook(res?.data || { bookId: Number(bookId) })
}

export const borrowBook = async (bookId: string, now = new Date()): Promise<BorrowBookResult> => {
  const book = await fetchBook(bookId)
  const borrowedOn = dateOnly(now)
  const dueOn = dateOnly(calculateBookDueDate(now, book.category))
  const categoryLabel = book.category === 'textbook' ? '教材类' : '非教材类'
  await request({
    url: '/qt/borrow',
    method: 'POST',
    data: { bookId: Number(bookId), dueTime: `${dueOn} 23:59:59` },
  })
  return { book, borrowedOn, dueOn, categoryLabel }
}