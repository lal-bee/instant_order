<template>
  <div class="page-order-detail">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">订单详情</span>
    </header>

    <div v-if="loading" class="loading-wrap">加载中...</div>
    <template v-else-if="order.id">
      <!-- 状态与操作 -->
      <div class="white-box">
        <div class="order-status">{{ statusList[order.status]?.name || '未知' }}</div>
        <div v-if="order.status === 1" class="time-box">
          <template v-if="countdownM <= 0 && countdownS <= 0">订单已超时</template>
          <template v-else>
            支付剩余时间 {{ String(countdownM).padStart(2, '0') }}:{{ String(countdownS).padStart(2, '0') }}
          </template>
        </div>
        <div class="btn-box">
          <button
            v-if="order.status <= 2"
            type="button"
            class="btn btn-cancel"
            @click="cancelOrder"
          >
            取消订单
          </button>
          <button
            v-if="order.status === 1 && (countdownM > 0 || countdownS > 0)"
            type="button"
            class="btn btn-primary"
            @click="toPay"
          >
            立即支付
          </button>
          <button v-if="order.status === 2" type="button" class="btn btn-primary" @click="pushOrder">
            催单
          </button>
          <button
            v-if="order.status === 2 || order.status === 6"
            type="button"
            class="btn btn-outline"
            @click="reOrder"
          >
            再来一单
          </button>
        </div>
      </div>

      <!-- 订单菜品 -->
      <div class="white-box">
        <div class="section-title">寒页餐厅</div>
        <div v-for="(obj, index) in (order.orderDetailList || [])" :key="index" class="dish-row">
          <img :src="obj.pic" class="dish-pic" alt="" />
          <div class="dish-info">
            <div class="dish-name">{{ obj.name }}</div>
            <div v-if="obj.dishFlavor" class="dish-flavor">{{ obj.dishFlavor }}</div>
            <div class="dish-meta">
              <span v-if="obj.number">x {{ obj.number }}</span>
              <span class="dish-price">￥{{ obj.amount }}</span>
            </div>
          </div>
        </div>
        <div class="row total-row">
          <span class="row-right">总价 ￥{{ order.amount }}</span>
        </div>
      </div>

      <div class="white-box link-row" @click="connectShop">联系商家</div>

      <!-- 备注/餐具/发票 -->
      <div class="white-box">
        <div class="row">
          <span class="row-left">备注</span>
          <span class="row-right">{{ order.remark || '-' }}</span>
        </div>
        <div class="row">
          <span class="row-left">餐具份数</span>
          <span class="row-right">{{ tablewareText }}</span>
        </div>
        <div class="row">
          <span class="row-left">发票</span>
          <span class="row-right">本店不支持线上发票，请致电商家提供</span>
        </div>
      </div>

      <div class="white-box">
        <div class="row">
          <span class="row-left">订单号</span>
          <span class="row-right">{{ order.number }}</span>
        </div>
        <div class="row">
          <span class="row-left">下单时间</span>
          <span class="row-right">{{ order.orderTime }}</span>
        </div>
        <div class="row">
          <span class="row-left">就餐信息</span>
          <span class="row-right">{{ order.address || '-' }}</span>
        </div>
      </div>
    </template>
    <div v-else class="empty">订单不存在或已失效</div>

    <PushMsg ref="pushMsgRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderAPI, cancelOrderAPI, reOrderAPI, urgeOrderAPI, payOrderAPI } from '@/api/order'
import { cleanCartAPI } from '@/api/cart'
import { useCountdownStore } from '@/stores/countdown'
import { showToast } from '@/utils/toast'
import PushMsg from '@/components/PushMsg.vue'

const router = useRouter()
const route = useRoute()
const order = ref({})
const loading = ref(true)
const pushMsgRef = ref(null)
const backIconError = ref(false)

const statusList = [
  { name: '全部订单' },
  { name: '等待支付' },
  { name: '已支付（待制作）' },
  { name: '制作中' },
  { name: '待取餐' },
  { name: '订单已完成' },
  { name: '订单已取消' },
]

const countdownStore = useCountdownStore()
const countdownM = ref(0)
const countdownS = ref(0)
let countdownTimer = null

const tablewareText = computed(() => {
  const n = order.value.tablewareNumber
  if (n === -1) return '无需餐具'
  if (n === 0) return '商家根据餐量提供'
  return String(n)
})

function startCountdown() {
  if (order.value.status !== 1 || !order.value.orderTime) return
  const orderTime = new Date(order.value.orderTime).getTime()
  const deadline = orderTime + 15 * 60 * 1000
  function tick() {
    const now = Date.now()
    if (now >= deadline) {
      countdownM.value = 0
      countdownS.value = 0
      if (countdownTimer) clearInterval(countdownTimer)
      return
    }
    const rest = Math.floor((deadline - now) / 1000)
    countdownM.value = Math.floor(rest / 60)
    countdownS.value = rest % 60
  }
  tick()
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(tick, 1000)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (countdownStore.timer) {
    clearInterval(countdownStore.timer)
    countdownStore.timer = null
  }
})

async function loadOrder() {
  const orderId = route.query.orderId
  if (!orderId) {
    loading.value = false
    return
  }
  try {
    const res = await getOrderAPI(Number(orderId))
    order.value = res.data || {}
    if (order.value.status === 1) startCountdown()
  } catch (e) {
    order.value = {}
  }
  loading.value = false
}

function goBack() {
  router.replace('/order-list')
}

async function cancelOrder() {
  try {
    await cancelOrderAPI(order.value.id)
    showToast('订单已取消')
    await loadOrder()
  } catch (e) {
    showToast('取消失败，订单状态已变更，请联系商家')
    await loadOrder()
  }
}

async function pushOrder() {
  try {
    await urgeOrderAPI(order.value.id)
    pushMsgRef.value?.openPopup()
  } catch (e) {}
}

async function reOrder() {
  try {
    await cleanCartAPI()
    await reOrderAPI(order.value.id)
    showToast('已加入购物车')
    router.replace({ path: '/order' })
  } catch (e) {}
}

function connectShop() {
  window.location.href = 'tel:1999'
}

async function toPay() {
  try {
    await payOrderAPI({
      orderNumber: order.value.number,
      payMethod: 1,
    })
    showToast('支付成功')
    router.replace({
      path: '/pay',
      query: {
        orderId: order.value.id,
        orderNumber: order.value.number,
        orderAmount: order.value.amount,
        orderTime: order.value.orderTime,
      },
    })
  } catch (e) {}
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.page-order-detail {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  background: #f8f8f8;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: calc(44px + env(safe-area-inset-top, 0px));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: env(safe-area-inset-top, 0px) 12px 0;
  background: #fff;
  border-bottom: 1px solid #eee;
  z-index: 100;
}
.detail-header .back-btn {
  position: absolute;
  left: 12px;
  top: calc(env(safe-area-inset-top, 0px) + 10px);
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.08);
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.detail-header .back {
  width: 18px;
  height: 18px;
}
.detail-header .back-fallback {
  font-size: 20px;
  line-height: 1;
  color: #111827;
  transform: translateY(-1px);
}
.detail-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.loading-wrap,
.empty {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.white-box {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}
.order-status {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}
.time-box {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}
.btn-box {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}
.btn {
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #ccc;
  background: #fff;
  color: #333;
}
.btn-primary {
  background: #22ccff;
  border-color: #22ccff;
  color: #fff;
}
.btn-outline {
  color: #22ccff;
  border-color: #22ccff;
}
.btn-cancel {
  color: #333;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.dish-row {
  display: flex;
  margin-bottom: 12px;
}
.dish-pic {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
  margin-right: 12px;
}
.dish-info {
  flex: 1;
  min-width: 0;
}
.dish-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.dish-flavor {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.dish-meta {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}
.dish-price {
  color: #e94e3c;
  margin-left: 8px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.row:last-child {
  border-bottom: none;
}
.row-left {
  color: #333;
}
.row-right {
  color: #666;
  font-size: 13px;
  max-width: 62%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.total-row .row-right {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.link-row {
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}
</style>
