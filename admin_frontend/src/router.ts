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
    path: 'dish',
    name: 'dish',
    component: () => import('./views/headquarters/dish.vue'),
    meta: { title: '标准菜品管理', icon: 'Dish', roles: [RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'setmeal',
    name: 'setmeal',
    component: () => import('./views/headquarters/setmeal.vue'),
    meta: { title: '标准套餐管理', icon: 'User', roles: [RoleEnum.STORE_MANAGER, RoleEnum.EMPLOYEE] },
  },
  {
    path: 'headquarters',
    name: 'headquarters',
    component: () => import('./views/headquarters/index.vue'),
    redirect: '/headquarters/list',
    meta: { title: '总店管理', icon: 'OfficeBuilding', roles: [RoleEnum.CHAIRMAN] },
    children: [
      {
        path: 'list',
        name: 'headquarters_list',
        component: () => import('./views/headquarters/list.vue'),
        meta: { title: '总店列表', icon: 'OfficeBuilding', roles: [RoleEnum.CHAIRMAN] },
      },
      {
        path: 'category',
        name: 'headquarters_category',
        component: () => import('./views/headquarters/category.vue'),
        meta: { title: '标准菜品分类管理', icon: 'Grid', roles: [RoleEnum.CHAIRMAN] },
      },
      {
        path: 'category/add',
        name: 'headquarters_category_add',
        component: () => import('./views/category/add.vue'),
        meta: {
          title: '新增分类',
          roles: [RoleEnum.CHAIRMAN],
          hidden: true,
          activeMenu: '/headquarters/category',
        },
      },
      {
        path: 'category/update',
        name: 'headquarters_category_update',
        component: () => import('./views/category/update.vue'),
        meta: {
          title: '编辑分类',
          roles: [RoleEnum.CHAIRMAN],
          hidden: true,
          activeMenu: '/headquarters/category',
        },
      },
      {
        path: 'dish',
        name: 'headquarters_dish',
        component: () => import('./views/headquarters/dish.vue'),
        meta: { title: '标准菜品管理', icon: 'Dish', roles: [RoleEnum.CHAIRMAN] },
      },
      {
        path: 'dish/add',
        name: 'headquarters_dish_add',
        component: () => import('./views/dish/add.vue'),
        meta: {
          title: '新增菜品',
          roles: [RoleEnum.CHAIRMAN],
          hidden: true,
          activeMenu: '/headquarters/dish',
        },
      },
      {
        path: 'setmeal',
        name: 'headquarters_setmeal',
        component: () => import('./views/headquarters/setmeal.vue'),
        meta: { title: '标准套餐管理', icon: 'User', roles: [RoleEnum.CHAIRMAN] },
      },
      {
        path: 'setmeal/add',
        name: 'headquarters_setmeal_add',
        component: () => import('./views/setmeal/add.vue'),
        meta: {
          title: '新增套餐',
          roles: [RoleEnum.CHAIRMAN],
          hidden: true,
          activeMenu: '/headquarters/setmeal',
        },
      },
    ],
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
    meta: { title: '门店菜单配置', icon: 'Tickets', roles: [RoleEnum.CHAIRMAN, RoleEnum.STORE_MANAGER] },
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
  // 兼容旧地址，统一跳转到总店模块下
  { path: 'category', redirect: '/headquarters/category', meta: { hidden: true } },
  { path: 'category/add', redirect: '/headquarters/category/add', meta: { hidden: true } },
  { path: 'category/update', redirect: '/headquarters/category/update', meta: { hidden: true } },
  { path: 'dish/add', redirect: '/headquarters/dish/add', meta: { hidden: true } },
  { path: 'setmeal/add', redirect: '/headquarters/setmeal/add', meta: { hidden: true } },
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
