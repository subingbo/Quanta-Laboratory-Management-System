import type { LoginResponse } from '../types/auth'
import type { UserProfile, UserRole } from '../types/session'
import { clearSession, setSession } from '../stores/user'
import { homeForRole } from '../utils/authGuard'

type LoginResponseWithUser = LoginResponse & { user?: Partial<UserProfile> }

export const completeLogin = (response: LoginResponseWithUser, role: UserRole, account: string) => {
  const fallbackProfile: UserProfile = {
    id: account,
    account,
    name: role === 'tower' ? '方东升' : account,
    role,
    department: role === 'tower' ? '产品部' : undefined,
    batch: role === 'tower' ? '20th' : undefined,
  }
  const profile: UserProfile = { ...fallbackProfile, ...(response.user || {}), role }
  setSession(response.token, role, profile)
  return profile
}

export const logout = () => {
  clearSession()
  uni.reLaunch({ url: '/pages/login/select-role' })
}

export const goToRoleHome = (role: UserRole) => {
  uni.reLaunch({ url: homeForRole(role) })
}
