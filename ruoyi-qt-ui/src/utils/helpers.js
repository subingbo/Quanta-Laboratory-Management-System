export function rowsOf(res) {
  return (res && res.rows) || []
}

export function today() {
  return formatDate(new Date())
}

export function plusDays(days) {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

export function formatDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function parseOptions(value, fallback) {
  if (!value) {
    return fallback
  }
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) && parsed.length ? parsed : fallback
  } catch (e) {
    return value.split(/[,\s，、]+/).filter(Boolean)
  }
}

export function statusText(status) {
  const map = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CANCELED: '已取消',
    DELETED: '已删除',
    APPLIED: '已报名',
    APPROVED: '已通过',
    REJECTED: '未通过',
    PENDING: '待审核',
    FINISHED: '已完成',
    BORROWED: '借阅中',
    RETURNED: '已归还',
    OVERDUE: '已逾期',
    SUBMITTED: '已提交',
    PROCESSING: '面试中',
    OFFERED: '已录取',
    PASS: '通过',
    FAIL: '未通过',
    WAITING: '候补'
  }
  return map[status] || status || '未设置'
}

export function deptLabel(code) {
  const map = {
    BACKEND: '后端',
    PRODUCT: '产品',
    DESIGN: '设计',
    FRONTEND: '前端',
    ANDROID: '安卓'
  }
  return map[code] || code || ''
}
