<template>
  <div class="page-submit">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">确认订单</span>
    </header>

    <div class="main">
      <section class="section">
        <h3 class="section-title">当前桌台</h3>
        <div class="table-context">{{ storeName || '-' }} / {{ tableNo || '-' }}</div>
      </section>
      <!-- 购物车清单 -->
      <section class="section cart-section">
        <h3 class="section-title">商品清单</h3>
        <div v-if="cartList.length === 0" class="empty-tip">购物车为空，请先点餐</div>
        <ul v-else class="cart-list">
          <li v-for="(item, index) in cartList" :key="index" class="cart-item">
            <div class="cart-item-name">{{ item.name }}</div>
            <div class="cart-item-meta">
              <span v-if="item.dishFlavor" class="flavor">{{ item.dishFlavor }}</span>
              <span class="price">¥{{ item.amount }} × {{ item.number }}</span>
            </div>
          </li>
        </ul>
      </section>

      <!-- 备注 -->
      <section class="section">
        <h3 class="section-title">备注</h3>
        <input
          v-model="remark"
          type="text"
          class="remark-input"
          placeholder="选填，如口味、忌口等"
          maxlength="200"
        />
      </section>

      <!-- 餐具 -->
      <section class="section">
        <h3 class="section-title">餐具数量</h3>
        <div class="tableware-opt">
          <label class="radio">
            <input v-model="tablewareStatus" type="radio" :value="1" />
            <span>按餐量提供</span>
          </label>
          <label class="radio">
            <input v-model="tablewareStatus" type="radio" :value="0" />
            <span>需要</span>
            <input
              v-model.number="tablewareNumber"
              type="number"
              min="0"
              max="20"
              class="tableware-num"
            />
            <span>份</span>
          </label>
        </div>
      </section>

      <!-- 支付方式 -->
      <section class="section">
        <h3 class="section-title">支付方式</h3>
        <div class="pay-opt">
          <label class="radio">
            <input v-model.number="payMethod" type="radio" :value="1" />
            <span>在线支付</span>
          </label>
        </div>
      </section>

      <section class="section">
        <h3 class="section-title">优惠券</h3>
        <div class="pay-opt">
          <select v-model.number="selectedUserCouponId" class="coupon-select">
            <option :value="0">不使用优惠券</option>
            <option v-for="item in couponList" :key="item.id" :value="item.id">
              {{ item.couponName }}（{{ item.couponType === 1 ? `满${item.thresholdAmount}减${item.discountAmount}` : `${item.discountRate}折` }}）
            </option>
          </select>
        </div>
      </section>
    </div>

    <!-- 底部：合计 + 提交 -->
    <div class="footer">
      <div class="total">
        <span class="label">原价 ¥{{ cartTotalPrice }}</span>
        <span class="label">优惠 ¥{{ discountPrice }}</span>
        <span class="price">应付 ¥{{ payAmount }}</span>
      </div>
      <button
        type="button"
        class="btn-submit"
        :disabled="cartList.length === 0 || submitting"
        @click="submitOrder"
      >
        {{ submitting ? '提交中...' : '提交订单' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCartAPI } from '@/api/cart'
import { submitOrderAPI } from '@/api/order'
import { getOrderAvailableCouponAPI } from '@/api/coupon'
import { showToast } from '@/utils/toast'
import { getStoreId, getTableId, getTableNo, getStoreName } from '@/utils/url'

const router = useRouter()
const route = useRoute()
const cartList = ref([])
const remark = ref('')
const tablewareStatus = ref(1) // 1 按餐量 0 具体数量
const tablewareNumber = ref(0)
const payMethod = ref(1) // 统一在线支付
const submitting = ref(false)
const storeName = ref('')
const tableNo = ref('')
const backIconError = ref(false)
const couponList = ref([])
const selectedUserCouponId = ref(0)

const cartTotalPrice = computed(() => {
  const total = cartList.value.reduce((acc, cur) => acc + (cur.amount || 0) * (cur.number || 0), 0)
  return (Math.round(total * 100) / 100).toFixed(2)
})
const selectedCoupon = computed(() => couponList.value.find(item => item.id === selectedUserCouponId.value))
const discountPrice = computed(() => {
  const amount = Number(cartTotalPrice.value)
  const coupon = selectedCoupon.value
  if (!coupon) return '0.00'
  if (coupon.couponType === 1) return Number(coupon.discountAmount || 0).toFixed(2)
  const discount = amount * ((10 - Number(coupon.discountRate || 10)) / 10)
  return Math.max(discount, 0).toFixed(2)
})
const payAmount = computed(() => {
  const amount = Number(cartTotalPrice.value) - Number(discountPrice.value)
  return Math.max(amount, 0).toFixed(2)
})

async function loadCart() {
  try {
    const res = await getCartAPI()
    cartList.value = res.data || []
    await loadAvailableCoupons()
  } catch (e) {
    cartList.value = []
    couponList.value = []
  }
}

async function loadAvailableCoupons() {
  const storeId = getStoreId()
  if (!storeId) return
  try {
    const res = await getOrderAvailableCouponAPI({
      storeId: Number(storeId),
      amount: Number(cartTotalPrice.value),
    })
    couponList.value = res.data || []
    if (!couponList.value.find(item => item.id === selectedUserCouponId.value)) {
      selectedUserCouponId.value = 0
    }
  } catch (e) {
    couponList.value = []
  }
}

function goBack() {
  const query = { ...route.query }
  if (!query.tableId) query.tableId = getTableId()
  router.replace({ path: '/order', query })
}

async function submitOrder() {
  if (cartList.value.length === 0) {
    showToast('购物车为空')
    return
  }
  // 门店号、桌号：优先来自当前 URL（扫码带参），否则来自 sessionStorage（扫码后登录再进本页）
  const storeId = getStoreId()
  const tableId = getTableId()
  const tableNoVal = getTableNo()
  if (!storeId || !tableId) {
    showToast('缺少门店号或座位号，请扫描餐桌二维码进入')
    return
  }
  submitting.value = true
  try {
    const res = await submitOrderAPI({
      storeId: Number(storeId),
      tableId: Number(tableId),
      tableNo: tableNoVal || undefined,
      payMethod: payMethod.value,
      remark: remark.value.trim(),
      tablewareStatus: tablewareStatus.value,
      tablewareNumber: tablewareStatus.value === 0 ? tablewareNumber.value : 0,
      amount: Number(cartTotalPrice.value),
      userCouponId: selectedUserCouponId.value || undefined,
    })
    submitting.value = false
    showToast('下单成功')
    const orderId = res.data?.id
    const orderNumber = res.data?.orderNumber
    const query = { ...route.query, orderId, orderNumber }
    if (!query.tableId) query.tableId = tableId
    router.replace({ path: '/pay', query })
  } catch (e) {
    submitting.value = false
  }
}

onMounted(() => {
  storeName.value = getStoreName()
  tableNo.value = getTableNo()
  loadCart()
})
</script>

<style scoped>
.page-submit {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
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

.main {
  padding: 12px;
}

.section {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.cart-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.cart-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.cart-item:last-child { border-bottom: none; }
.cart-item-name { font-size: 15px; color: #333; }
.cart-item-meta {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.cart-item-meta .flavor { margin-right: 8px; }
.cart-item-meta .price { color: #e94e3c; }

.empty-tip {
  font-size: 14px;
  color: #999;
  padding: 16px 0;
}

.remark-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.table-context {
  font-size: 14px;
  color: #065f46;
  background: #ecfdf5;
  border: 1px solid #10B981;
  border-radius: 6px;
  padding: 8px 10px;
}

.tableware-opt,
.pay-opt {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}
.radio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.tableware-num {
  width: 56px;
  padding: 4px 8px;
  font-size: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}
.coupon-select {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px env(safe-area-inset-bottom, 0px);
  background: #fff;
  border-top: 1px solid #eee;
  z-index: 100;
}
.footer .total .label { font-size: 14px; color: #666; margin-right: 8px; }
.footer .total .price { font-size: 18px; font-weight: 600; color: #e94e3c; }
.btn-submit {
  padding: 10px 28px;
  font-size: 15px;
  color: #fff;
  background: #10B981;
  border: none;
  border-radius: 24px;
  cursor: pointer;
}
.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
