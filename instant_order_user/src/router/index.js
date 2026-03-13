import { createRouter, createWebHistory } from 'vue-router'
import { persistScanParams, getStoreId, getTableId } from '@/utils/url'
import { saveScanParamsAPI } from '@/api/user'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页', auth: false } },
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { title: '登录', auth: false } },
  { path: '/order', name: 'Order', component: () => import('@/views/order/index.vue'), meta: { title: '点餐', auth: false } },
  { path: '/dish-detail', name: 'DishDetail', component: () => import('@/views/dish-detail/index.vue'), meta: { title: '商品详情', auth: false } },
  { path: '/submit', name: 'Submit', component: () => import('@/views/submit/index.vue'), meta: { title: '提交订单', auth: true } },
  { path: '/remark', name: 'Remark', component: () => import('@/views/remark/index.vue'), meta: { title: '备注', auth: true } },
  { path: '/pay', name: 'Pay', component: () => import('@/views/pay/index.vue'), meta: { title: '支付确认', auth: true } },
  { path: '/order-success', name: 'OrderSuccess', component: () => import('@/views/order-success/index.vue'), meta: { title: '下单成功', auth: true } },
  { path: '/order-list', name: 'OrderList', component: () => import('@/views/order-list/index.vue'), meta: { title: '订单列表', auth: true } },
  { path: '/order-detail', name: 'OrderDetail', component: () => import('@/views/order-detail/index.vue'), meta: { title: '订单详情', auth: true } },
  { path: '/my', name: 'My', component: () => import('@/views/my/index.vue'), meta: { title: '我的', auth: true } },
  { path: '/address', name: 'Address', component: () => import('@/views/address/index.vue'), meta: { title: '地址管理', auth: true } },
  { path: '/address-edit', name: 'AddressEdit', component: () => import('@/views/address-edit/index.vue'), meta: { title: '编辑地址', auth: true } },
  { path: '/update-my', name: 'UpdateMy', component: () => import('@/views/update-my/index.vue'), meta: { title: '信息设置', auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 设置页面标题；需要登录的页面无 token 时跳转登录；扫码参数持久化
router.beforeEach((to, from, next) => {
  if (to.meta && to.meta.title) {
    document.title = to.meta.title + ' - 扫码点餐'
  }
  // 扫码点餐：URL 中带门店号、桌号时写入 sessionStorage，登录/跳转后提交订单仍可用
  if (to.query && to.query.storeId && to.query.tableId) {
    persistScanParams(to.query)
  }
  const needAuth = to.meta.auth === true
  const token = localStorage.getItem('token')
  // 正式发布后：有 token 且有 storeId/tableId 时，同步到 Redis，下单时从 Redis 取
  if (token) {
    const sid = getStoreId()
    const tid = getTableId()
    if (sid && tid) {
      saveScanParamsAPI(Number(sid), tid).catch(() => {})
    }
  }
  if (needAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
