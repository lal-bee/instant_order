<template>
  <div class="page">
    <header class="title">领券中心</header>
    <div v-if="loading" class="tip">加载中...</div>
    <div v-else-if="list.length === 0" class="tip">暂无可领取优惠券</div>
    <div v-else class="list">
      <div v-for="item in list" :key="item.id" class="card">
        <div class="name">{{ item.name }}</div>
        <div class="desc">{{ item.couponType === 1 ? `满${item.thresholdAmount}减${item.discountAmount}` : `${item.discountRate}折` }}</div>
        <div class="desc">{{ item.publishType === 1 ? '全局券' : `门店券-${item.storeName || '-'}` }}</div>
        <div class="desc">有效期：{{ item.startTime }} ~ {{ item.endTime }}</div>
        <button class="btn" @click="receive(item.id)">立即领取</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getReceiveCouponListAPI, receiveCouponAPI } from '@/api/coupon'
import { showToast } from '@/utils/toast'
import { getStoreId } from '@/utils/url'

const list = ref([])
const loading = ref(true)

const load = async () => {
  loading.value = true
  try {
    const storeId = getStoreId()
    const res = await getReceiveCouponListAPI(Number(storeId))
    list.value = res.data || []
  } catch (e) {
    list.value = []
  }
  loading.value = false
}

const receive = async (id) => {
  await receiveCouponAPI(id)
  showToast('领取成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #f6f7f9; padding: 16px; box-sizing: border-box; }
.title { font-size: 18px; font-weight: 600; margin-bottom: 14px; }
.tip { text-align: center; color: #999; padding: 30px 0; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card { background: #fff; border-radius: 10px; padding: 14px; }
.name { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.desc { font-size: 13px; color: #666; margin-bottom: 4px; }
.btn { margin-top: 8px; width: 100%; height: 36px; border: none; border-radius: 18px; color: #fff; background: #10B981; }
</style>
