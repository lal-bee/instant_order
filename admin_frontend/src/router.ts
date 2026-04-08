import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/store'
import { RoleEnum } from '@/constants/permission'
import { canAccessRoute, normalizeRole } from '@/utils/permission'

export const layoutChildrenRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'dashboard',
    component: () => import('./views/dashboard/index.vue'),
    meta: { title: '控制台', icon: 'PieChart', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'statistics',
    name: 'statistics',
    component: () => import('./views/statistics/index.vue'),
    meta: { title: '数据统计', icon: 'Memo', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'order',
    name: 'order',
    component: () => import('./views/order/index.vue'),
    meta: { title: '订单管理', icon: 'Collection', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'coupon',
    name: 'coupon',
    component: () => import('./views/coupon/index.vue'),
    meta: { title: '营销管理 / 优惠券管理', icon: 'Discount', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'category',
    name: 'category',
    component: () => import('./views/category/index.vue'),
    meta: { title: '标准菜品分类管理', icon: 'Grid', roles: [RoleEnum.CHAIRMAN] },
  },
  {
    path: 'dish',
    name: 'dish',
    component: () => import('./views/dish/index.vue'),
    meta: { title: '标准菜品管理', icon: 'Dish', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'setmeal',
    name: 'setmeal',
    component: () => import('./views/setmeal/index.vue'),
    meta: { title: '标准套餐管理', icon: 'User', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'dish/add',
    name: 'dish_add',
    component: () => import('./views/dish/add.vue'),
    meta: {
      title: '新增菜品',
      roles: [RoleEnum.CHAIRMAN],
      hidden: true,
      activeMenu: '/dish',
    },
  },
  {
    path: 'setmeal/add',
    name: 'setmeal_add',
    component: () => import('./views/setmeal/add.vue'),
    meta: {
      title: '新增套餐',
      roles: [RoleEnum.CHAIRMAN],
      hidden: true,
      activeMenu: '/setmeal',
    },
  },
  {
    path: 'category/add',
    name: 'category_add',
    component: () => import('./views/category/add.vue'),
    meta: {
      title: '新增分类',
      roles: [RoleEnum.CHAIRMAN],
      hidden: true,
      activeMenu: '/category',
    },
  },
  {
    path: 'category/update',
    name: 'category_update',
    component: () => import('./views/category/update.vue'),
    meta: {
      title: '编辑分类',
      roles: [RoleEnum.CHAIRMAN],
      hidden: true,
      activeMenu: '/category',
    },
  },
  {
    path: 'store',
    name: 'store',
    component: () => import('./views/store/index.vue'),
    meta: { title: '分店管理', icon: 'Shop', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER] },
  },
  {
    path: 'store-menu',
    name: 'store_menu',
    component: () => import('./views/store-menu/index.vue'),
    meta: { title: '门店菜单配置', icon: 'Tickets', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'employee',
    name: 'employee',
    component: () => import('./views/employee/index.vue'),
    meta: { title: '员工管理', icon: 'Setting', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'employee/add',
    name: 'employee_add',
    component: () => import('./views/employee/add.vue'),
    meta: { title: '新增员工', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER], hidden: true, activeMenu: '/employee' },
  },
  {
    path: 'employee/update',
    name: 'employee_update',
    component: () => import('./views/employee/update.vue'),
    meta: { title: '修改员工', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE], hidden: true, activeMenu: '/employee' },
  },
]

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('./views/layout/index.vue'),
    redirect: '/dashboard',
    children: layoutChildrenRoutes,
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('./views/login/index.vue'),
    meta: { hidden: true },
  },
  {
    path: '/reg',
    name: 'reg',
    component: () => import('./views/reg/index.vue'),
    meta: { hidden: true },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
    meta: { hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

const whiteList = ['/login', '/reg']

router.beforeEach((to, _from, next) => {
  const userInfoStore = useUserInfoStore()
  const token = userInfoStore.userInfo?.token

  if (!token && !whiteList.includes(to.path)) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (token && to.path === '/login') {
    next('/dashboard')
    return
  }

  if (!token) {
    next()
    return
  }

  const role = normalizeRole(
    userInfoStore.userInfo?.role ??
      (userInfoStore.userInfo as any)?.roleName ??
      (userInfoStore.userInfo as any)?.userRole,
  )

  if (!canAccessRoute(role, to)) {
    ElMessage.warning('无权限访问该页面')
    next('/dashboard')
    return
  }

  next()
})

export default router
