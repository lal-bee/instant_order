import type { RouteRecordRaw } from 'vue-router'
import { DEFAULT_ROLE, RoleEnum, ROLE_ALIAS_MAP, type RoleLike } from '@/constants/permission'

type RouteMetaLike = {
  roles?: RoleEnum[]
}

const normalizeRoleToken = (rawRole: string): RoleEnum => {
  const upper = rawRole.trim().toUpperCase()
  return ROLE_ALIAS_MAP[upper] ?? DEFAULT_ROLE
}

export const normalizeRole = (role: RoleLike): RoleEnum => {
  if (typeof role === 'number') {
    return normalizeRoleToken(String(role))
  }
  if (typeof role === 'string') {
    return normalizeRoleToken(role)
  }
  if (role && typeof role === 'object' && 'toString' in role) {
    return normalizeRoleToken(String(role))
  }
  return DEFAULT_ROLE
}

export const hasRole = (userRole: RoleLike, allowedRoles?: RoleEnum[]): boolean => {
  if (!allowedRoles || allowedRoles.length === 0) {
    return true
  }
  const normalizedUserRole = normalizeRole(userRole)
  return allowedRoles.includes(normalizedUserRole)
}

export const canAccessRoute = (userRole: RoleLike, route: Pick<RouteRecordRaw, 'meta'>): boolean => {
  const meta = (route.meta || {}) as RouteMetaLike
  return hasRole(userRole, meta.roles)
}

export const filterRoutesByRole = (routes: RouteRecordRaw[], userRole: RoleLike): RouteRecordRaw[] => {
  return routes
    .filter((route) => canAccessRoute(userRole, route))
    .map((route) => {
      const clonedRoute: RouteRecordRaw = { ...route }
      if (route.children?.length) {
        clonedRoute.children = filterRoutesByRole(route.children, userRole)
      }
      return clonedRoute
    })
}

export const isChairman = (role: RoleLike): boolean => normalizeRole(role) === RoleEnum.CHAIRMAN
export const isStoreManager = (role: RoleLike): boolean => normalizeRole(role) === RoleEnum.STORE_MANAGER
export const isEmployee = (role: RoleLike): boolean => normalizeRole(role) === RoleEnum.EMPLOYEE
