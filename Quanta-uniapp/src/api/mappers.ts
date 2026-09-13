import { API_BASE_URL } from '../config/runtime'
import type { UserProfile, UserRole } from '../types/session'
import type { SysUserDto } from './contracts'

const DEPARTMENT_LABELS: Record<string, string> = {
  PRODUCT: '产品部',
  DESIGN: '设计部',
  FRONTEND: '全栈(前端)',
  BACKEND: '全栈(后端)',
  ANDROID: '安卓组',
}

export const departmentLabel = (value = '') => DEPARTMENT_LABELS[value] || value

export const resolveApiAssetUrl = (path = '') => {
  if (!path || /^(https?:|data:|\/static\/)/.test(path)) return path
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

export const mapSysUserProfile = (user: SysUserDto, role: UserRole): UserProfile => ({
  id: String(user.userId ?? user.memberNo ?? user.userName ?? ''),
  account: user.userName || user.memberNo || '',
  name: user.nickName || user.userName || '',
  role,
  department: departmentLabel(user.memberDepartment),
  batch: user.memberCohort || undefined,
  avatar: resolveApiAssetUrl(user.avatar),
})
