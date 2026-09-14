import { isKnownComponent } from './component-map'
import { mockRouters as localWebRoutes } from '@/mock/data/routers'

function containsSupportedPage(routes = []) {
  return routes.some((route) => {
    const isPage = route.component !== 'Layout' && isKnownComponent(route.component)
    return isPage || containsSupportedPage(route.children || [])
  })
}

function canAccessRoute(route, permissions = []) {
  const required = route.meta?.permission
  return !required || permissions.includes('*:*:*') || permissions.includes(required)
}

export function selectWebRoutes(backendRoutes = [], permissions = []) {
  if (containsSupportedPage(backendRoutes)) return backendRoutes
  return localWebRoutes.filter((route) => canAccessRoute(route, permissions))
}

