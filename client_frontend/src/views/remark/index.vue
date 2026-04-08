<template>
  <div class="page-remark">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">订单备注</span>
    </header>
    <div class="content">
      <p class="tip">该页面暂未启用，请在确认订单页填写备注。</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const backIconError = ref(false)

function goBack() {
  router.back()
}
</script>

<style scoped>
.page-remark {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  background: #f6f7f9;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: calc(44px + env(safe-area-inset-top, 0px));
  padding: env(safe-area-inset-top, 0px) 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.back-btn {
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

.back {
  width: 18px;
  height: 18px;
}

.back-fallback {
  font-size: 20px;
  line-height: 1;
  color: #111827;
  transform: translateY(-1px);
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.content {
  padding: 14px;
}

.tip {
  margin: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}
</style>
