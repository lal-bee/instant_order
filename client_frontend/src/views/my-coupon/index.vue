<template>
  <div class="page">
    <header class="title">我的优惠券</header>
    <div class="tabs">
      <button :class="['tab', statusType===1?'active':'']" @click="change(1)">未使用</button>
      <button :class="['tab', statusType===2?'active':'']" @click="change(2)">已使用</button>
      <button :class="['tab', statusType===3?'active':'']" @click="change(3)">已过期</button>
    </div>
    <div v-if="loading" class="tip">加载中...</div>
    <div v-else-if="list.length===0" class="tip">暂无优惠券</div>
    <div v-else class="list">
      <div v-for="item in list" :key="item.id" class="card">
        <div class="name">{{ item.couponName }}</div>
        <div class="desc">{{ item.couponType === 1 ? `满${item.thresholdAmount}减${item.discountAmount}` : `${item.discountRate}折` }}</div>
        <div class="desc">有效期：{{ item.startTime }} ~ {{ item.endTime }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getMyCouponAPI } from '@/api/coupon'

const statusType = ref(1)
const list = ref([])
const loading = ref(true)

const load = async () => {
  loading.value = true
  try {
    const res = await getMyCouponAPI(statusType.value)
    list.value = res.data || []
  } catch (e) {
    list.value = []
  }
  loading.value = false
}

const change = (type) => {
  statusType.value = type
  load()
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #f6f7f9; padding: 16px; box-sizing: border-box; }
.title { font-size: 18px; font-weight: 600; margin-bottom: 14px; }
.tabs { display: flex; gap: 8px; margin-bottom: 12px; }
.tab { flex: 1; height: 34px; border: 1px solid #ddd; background: #fff; border-radius: 8px; }
.tab.active { background: #10B981; color: #fff; border-color: #10B981; }
.tip { text-align: center; color: #999; padding: 30px 0; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card { background: #fff; border-radius: 10px; padding: 14px; }
.name { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.desc { font-size: 13px; color: #666; margin-bottom: 4px; }
</style>
