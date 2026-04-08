<template>
  <div class="page-pay">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">支付确认</span>
    </header>

    <div v-if="loading" class="loading-wrap">加载中...</div>
    <template v-else-if="order">
      <div class="main">
        <section class="section order-info">
          <div class="row">
            <span class="label">订单号</span>
            <span class="value">{{ order.number }}</span>
          </div>
          <div class="row">
            <span class="label">订单金额</span>
            <span class="value price">¥{{ orderAmount }}</span>
          </div>
        </section>
      </div>

      <div class="footer">
        <button
          type="button"
          class="btn-pay"
          :disabled="paying"
          @click="confirmPay"
        >
          {{ paying ? '处理中...' : '确认支付' }}
        </button>
      </div>
    </template>
    <div v-else class="empty">订单不存在或已失效</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderAPI } from '@/api/order'
import { payOrderAPI } from '@/api/order'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const order = ref(null)
const loading = ref(true)
const paying = ref(false)
const backIconError = ref(false)

const orderAmount = computed(() => {
  if (!order.value || order.value.amount == null) return '0.00'
  return Number(order.value.amount).toFixed(2)
})

function goBack() {
  router.replace({ path: '/submit', query: route.query })
}

async function loadOrder() {
  const orderId = route.query.orderId
  if (!orderId) {
    loading.value = false
    return
  }
  try {
    const res = await getOrderAPI(Number(orderId))
    order.value = res.data
  } catch (e) {
    order.value = null
  }
  loading.value = false
}

async function confirmPay() {
  if (!order.value) return
  paying.value = true
  try {
    await payOrderAPI({
      orderNumber: order.value.number,
      payMethod: order.value.payMethod ?? 1,
    })
    showToast('支付成功')
    router.replace({
      path: '/order-success',
      query: { ...route.query, orderId: order.value.id, orderNumber: order.value.number },
    })
  } catch (e) {
    // toast 已在 request 中统一处理
  }
  paying.value = false
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.page-pay {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(92px + env(safe-area-inset-bottom, 0px));
  background: #f5f5f5;
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

.main {
  padding: 16px;
}
.section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.order-info .row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}
.order-info .label { color: #666; }
.order-info .value { color: #333; }
.order-info .value.price { font-size: 18px; font-weight: 600; color: #e94e3c; }

.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 12px 14px calc(10px + env(safe-area-inset-bottom, 0px));
  background: #fff;
  border-top: 1px solid #eee;
  z-index: 100;
}
.btn-pay {
  width: 100%;
  height: 48px;
  font-size: 16px;
  color: #fff;
  background: #10B981;
  border: none;
  border-radius: 24px;
  cursor: pointer;
}
.btn-pay:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
