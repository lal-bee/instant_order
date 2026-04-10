import { createRouter, createWebHistory } from 'vue-router'
import { persistTableIdFromQuery, buildLoginRouteQuery } from '@/utils/url'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页', auth: true } },
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { title: '登录', auth: false } },
  { path: '/register', name: 'Register', component: () => import('@/views/register/index.vue'), meta: { title: '注册', auth: false } },
  { path: '/order', name: 'Order', component: () => import('@/views/order/index.vue'), meta: { title: '点餐', auth: true } },
  { path: '/dish-detail', name: 'DishDetail', component: () => import('@/views/dish-detail/index.vue'), meta: { title: '商品详情', auth: true } },
  { path: '/submit', name: 'Submit', component: () => import('@/views/submit/index.vue'), meta: { title: '提交订单', auth: true } },
  { path: '/remark', name: 'Remark', component: () => import('@/views/remark/index.vue'), meta: { title: '备注', auth: true } },
  { path: '/pay', name: 'Pay', component: () => import('@/views/pay/index.vue'), meta: { title: '支付确认', auth: true } },
  { path: '/order-success', name: 'OrderSuccess', component: () => import('@/views/order-success/index.vue'), meta: { title: '下单成功', auth: true } },
  { path: '/order-list', name: 'OrderList', component: () => import('@/views/order-list/index.vue'), meta: { title: '订单列表', auth: true } },
  { path: '/order-detail', name: 'OrderDetail', component: () => import('@/views/order-detail/index.vue'), meta: { title: '订单详情', auth: true } },
  { path: '/my', name: 'My', component: () => import('@/views/my/index.vue'), meta: { title: '我的', auth: true } },
  { path: '/member-center', name: 'MemberCenter', component: () => import('@/views/member-center/index.vue'), meta: { title: '会员中心', auth: true } },
  { path: '/coupon-center', name: 'CouponCenter', component: () => import('@/views/coupon-center/index.vue'), meta: { title: '领券中心', auth: true } },
  { path: '/my-coupon', name: 'MyCoupon', component: () => import('@/views/my-coupon/index.vue'), meta: { title: '我的优惠券', auth: true } },
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
  // 扫码点餐：二维码仅带 tableId，先写入 sessionStorage 供后续页面使用
  if (to.query && to.query.tableId) {
    persistTableIdFromQuery(to.query)
  }
  const needAuth = to.meta.auth === true
  const token = localStorage.getItem('token')
  if (needAuth && !token) {
    next({
      path: '/login',
      query: buildLoginRouteQuery({
        tableId: to.query?.tableId,
        redirect: to.fullPath,
      }),
    })
  } else {
    next()
  }
})

export default router
