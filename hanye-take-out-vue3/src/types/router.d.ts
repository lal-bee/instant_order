import 'vue-router'
import type { RoleEnum } from '@/constants/permission'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    roles?: RoleEnum[]
    hidden?: boolean
    activeMenu?: string
  }
}
