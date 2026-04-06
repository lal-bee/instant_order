<template>
  <div class="page-submit">
    <header class="detail-header">
      <img class="back" src="/icon/back.png" alt="返回" @click="goBack" />
      <span class="title">确认订单</span>
    </header>

    <div class="main">
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
            <span>微信支付</span>
          </label>
          <label class="radio">
            <input v-model.number="payMethod" type="radio" :value="2" />
            <span>支付宝</span>
          </label>
        </div>
      </section>
    </div>

    <!-- 底部：合计 + 提交 -->
    <div class="footer">
      <div class="total">
        <span class="label">合计</span>
        <span class="price">¥{{ cartTotalPrice }}</span>
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
import { showToast } from '@/utils/toast'
import { getStoreId, getTableId } from '@/utils/url'

const router = useRouter()
const route = useRoute()
const cartList = ref([])
const remark = ref('')
const tablewareStatus = ref(1) // 1 按餐量 0 具体数量
const tablewareNumber = ref(0)
const payMethod = ref(1) // 1 微信 2 支付宝
const submitting = ref(false)

const cartTotalPrice = computed(() => {
  const total = cartList.value.reduce((acc, cur) => acc + (cur.amount || 0) * (cur.number || 0), 0)
  return (Math.round(total * 100) / 100).toFixed(2)
})

async function loadCart() {
  try {
    const res = await getCartAPI()
    cartList.value = res.data || []
  } catch (e) {
    cartList.value = []
  }
}

function goBack() {
  const query = { ...route.query }
  if (!query.storeId) query.storeId = getStoreId()
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
  if (!storeId || !tableId) {
    showToast('缺少门店号或座位号，请扫描餐桌二维码进入')
    return
  }
  submitting.value = true
  try {
    const res = await submitOrderAPI({
      storeId: Number(storeId),
      tableId: String(tableId),
      payMethod: payMethod.value,
      remark: remark.value.trim(),
      deliveryStatus: 1,
      estimatedDeliveryTime: null,
      tablewareStatus: tablewareStatus.value,
      tablewareNumber: tablewareStatus.value === 0 ? tablewareNumber.value : 0,
      packAmount: 0,
      amount: Number(cartTotalPrice.value),
    })
    submitting.value = false
    showToast('下单成功')
    const orderId = res.data?.id
    const orderNumber = res.data?.orderNumber
    const query = { ...route.query, orderId, orderNumber }
    if (!query.storeId) query.storeId = storeId
    if (!query.tableId) query.tableId = tableId
    router.replace({ path: '/pay', query })
  } catch (e) {
    submitting.value = false
  }
}

onMounted(() => {
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
.detail-header .back {
  position: absolute;
  left: 12px;
  top: calc(env(safe-area-inset-top, 0px) + 10px);
  width: 24px;
  height: 24px;
  cursor: pointer;
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
