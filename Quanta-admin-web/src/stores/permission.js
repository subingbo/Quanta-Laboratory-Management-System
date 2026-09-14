import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getRouters } from '@/api/auth'
import { transformRoutes } from '@/router/route-transformer'
import { selectWebRoutes } from '@/router/route-source'

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])
  const initialized = ref(false)
  const initializing = ref(null)
  const routeRemovers = []

  async function generateRoutes(permissions = []) {
    if (initialized.value) return routes.value
    if (initializing.value) return initializing.value

    initializing.value = getRouters()
      .then((response) => {
        routes.value = transformRoutes(selectWebRoutes(response.data || [], permissions))
        return routes.value
      })
      .finally(() => {
        initializing.value = null
      })

    return initializing.value
  }

  function installRoutes(router) {
    if (initialized.value) return
    routes.value.forEach((route) => {
      if (route.name && router.hasRoute(route.name)) router.removeRoute(route.name)
      routeRemovers.push(router.addRoute('Root', route))
    })
    initialized.value = true
  }

  function resetRoutes() {
    while (routeRemovers.length) {
      routeRemovers.pop()?.()
    }
    routes.value = []
    initialized.value = false
    initializing.value = null
  }

  return {
    routes,
    initialized,
    initializing,
    generateRoutes,
    installRoutes,
    resetRoutes,
  }
})
