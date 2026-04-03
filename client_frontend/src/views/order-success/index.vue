<template>
  <div class="page-order-success">
    <div class="success-icon">✓</div>
    <h1 class="title">支付成功</h1>
    <p class="desc">您的订单已提交，请等待商家接单</p>
    <div v-if="orderNumber || orderId" class="order-id">订单号：{{ orderNumber || orderId }}</div>

    <div class="actions">
      <button type="button" class="btn btn-secondary" @click="goOrderList">查看订单</button>
      <button type="button" class="btn btn-primary" @click="goOrder">继续点餐</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.query.orderId)
const orderNumber = computed(() => route.query.orderNumber)

function goOrderList() {
  router.replace({ path: '/order-list', query: route.query })
}

function goOrder() {
  router.replace({ path: '/order', query: route.query })
}
</script>

<style scoped>
.page-order-success {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: #fff;
}

.success-icon {
  width: 64px;
  height: 64px;
  line-height: 64px;
  text-align: center;
  font-size: 36px;
  color: #fff;
  background: #10B981;
  border-radius: 50%;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

.order-id {
  font-size: 13px;
  color: #999;
  margin-bottom: 40px;
}

.actions {
  display: flex;
  gap: 16px;
  width: 100%;
  max-width: 320px;
}

.btn {
  flex: 1;
  height: 44px;
  font-size: 15px;
  border: none;
  border-radius: 22px;
  cursor: pointer;
}

.btn-secondary {
  color: #10B981;
  background: #fff;
  border: 1px solid #10B981;
}

.btn-primary {
  color: #fff;
  background: #10B981;
}
</style>
