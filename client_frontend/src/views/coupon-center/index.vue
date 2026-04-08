<template>
  <div class="page">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">领券中心</span>
    </header>
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
import { useRouter } from 'vue-router'
import { getReceiveCouponListAPI, receiveCouponAPI } from '@/api/coupon'
import { showToast } from '@/utils/toast'
import { getStoreId } from '@/utils/url'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const backIconError = ref(false)

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
  try {
    await receiveCouponAPI(id)
    showToast('领取成功')
    load()
  } catch (e) {
    // 错误提示已在 request 拦截器内统一 toast，这里仅阻止未捕获异常
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.replace('/my')
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #f6f7f9; padding: calc(52px + env(safe-area-inset-top, 0px)) 16px 16px; box-sizing: border-box; }
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
.detail-header .back { width: 18px; height: 18px; }
.detail-header .back-fallback { font-size: 20px; line-height: 1; color: #111827; transform: translateY(-1px); }
.detail-header .title { font-size: 16px; font-weight: 600; color: #333; }
.tip { text-align: center; color: #999; padding: 30px 0; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card { background: #fff; border-radius: 10px; padding: 14px; }
.name { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.desc { font-size: 13px; color: #666; margin-bottom: 4px; }
.btn { margin-top: 8px; width: 100%; height: 36px; border: none; border-radius: 18px; color: #fff; background: #10B981; }
</style>
