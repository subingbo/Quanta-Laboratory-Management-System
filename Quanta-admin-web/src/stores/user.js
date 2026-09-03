import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/token'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(null)
  const roles = ref([])
  const permissions = ref([])

  const isAuthenticated = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.nickName || user.value?.userName || '未登录')
  const departmentCode = computed(
    () => user.value?.deptCode || user.value?.dept?.deptCode || user.value?.memberDepartmentCode || '',
  )
  const departmentName = computed(
    () => user.value?.dept?.deptName || user.value?.memberDepartment || '',
  )

  async function login(credentials) {
    const response = await authApi.login(credentials)
    token.value = response.token
    setToken(response.token)
    return response
  }

  async function fetchUserInfo() {
    const response = await authApi.getInfo()
    user.value = response.user || null
    roles.value = response.roles || []
    permissions.value = response.permissions || []
    return response
  }

  async function logout() {
    try {
      if (token.value) await authApi.logout()
    } finally {
      reset()
    }
  }

  function reset() {
    token.value = ''
    user.value = null
    roles.value = []
    permissions.value = []
    removeToken()
  }

  return {
    token,
    user,
    roles,
    permissions,
    isAuthenticated,
    displayName,
    departmentCode,
    departmentName,
    login,
    fetchUserInfo,
    logout,
    reset,
  }
})
