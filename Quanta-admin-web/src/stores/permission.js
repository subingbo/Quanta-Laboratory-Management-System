import { ref } from 'vue'
import { defineStore } from 'pinia'
import { menus } from '@/router/menus'
import { transformRoutes } from '@/router/route-transformer'

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])
  const initialized = ref(false)
  const initializing = ref(null)
  const routeRemovers = []

  async function generateRoutes() {
    if (initialized.value) return routes.value
    if (initializing.value) return initializing.value

    routes.value = transformRoutes(menus)
    return routes.value
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

