<template>
  <div class="page">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">会员中心</span>
    </header>

    <div class="card">
      <div class="status-line">
        <span class="label">当前状态</span>
        <span :class="['status', memberInfo.isMember === 1 ? 'active' : 'inactive']">
          {{ memberInfo.isMember === 1 ? '会员中' : memberInfo.memberStatusText || '非会员' }}
        </span>
      </div>
      <div class="line">
        <span class="label">会员等级</span>
        <span class="value">{{ levelText(memberInfo.memberLevel) }}</span>
      </div>
      <div class="line">
        <span class="label">到期时间</span>
        <span class="value">{{ memberInfo.memberExpireTime || '长期有效' }}</span>
      </div>
    </div>

    <div class="card">
      <div class="section-title">会员权益说明</div>
      <div class="desc">1. 会员可领取并使用“会员专享券”。</div>
      <div class="desc">2. 会员到期后将不能领取和使用会员专享券。</div>
      <div class="desc">3. 会员等级由后台统一维护，本页仅做展示。</div>
    </div>

    <div class="card">
      <div class="section-title">开通会员（演示版）</div>
      <div class="plan-list">
        <div class="plan-item">
          <div class="plan-name">普通会员（月卡）</div>
          <div class="plan-price">¥9.9 / 月</div>
          <button class="open-btn" :disabled="opening" @click="openPlan(1)">立即开通</button>
        </div>
        <div class="plan-item premium">
          <div class="plan-name">高级会员（终身）</div>
          <div class="plan-price">¥99 / 终身</div>
          <button class="open-btn" :disabled="opening" @click="openPlan(2)">立即开通</button>
        </div>
      </div>
      <div class="tip">说明：毕设演示版，不接支付网关，点击后直接开通。</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMemberInfoAPI, openMemberByPlanAPI } from '@/api/member'
import { showToast } from '@/utils/toast'

const router = useRouter()
const backIconError = ref(false)
const opening = ref(false)
const memberInfo = reactive({
  isMember: 0,
  memberLevel: 0,
  memberExpireTime: '',
  memberStatusText: '',
})

function levelText(level) {
  if (level === 2) return '高级会员'
  if (level === 1) return '普通会员'
  return '普通用户'
}

async function loadMemberInfo() {
  const res = await getMemberInfoAPI()
  const data = res.data || {}
  memberInfo.isMember = data.isMember ?? 0
  memberInfo.memberLevel = data.memberLevel ?? 0
  memberInfo.memberExpireTime = data.memberExpireTime ?? ''
  memberInfo.memberStatusText = data.memberStatusText ?? ''
}

async function openPlan(planType) {
  if (opening.value) return
  opening.value = true
  try {
    await openMemberByPlanAPI(planType)
    showToast('开通成功')
    await loadMemberInfo()
  } finally {
    opening.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.replace('/my')
}

onMounted(() => {
  loadMemberInfo()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding: calc(52px + env(safe-area-inset-top, 0px)) 12px 20px;
  box-sizing: border-box;
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

.card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
}

.status-line,
.line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.line:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
}

.status {
  font-size: 13px;
  padding: 2px 10px;
  border-radius: 999px;
}

.status.active {
  background: #dcfce7;
  color: #15803d;
}

.status.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.desc {
  font-size: 13px;
  color: #666;
  line-height: 1.7;
}

.plan-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.plan-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  background: #fafafa;
}

.plan-item.premium {
  border-color: #f59e0b;
  background: #fffbeb;
}

.plan-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.plan-price {
  margin-top: 4px;
  margin-bottom: 10px;
  font-size: 16px;
  color: #111827;
  font-weight: 700;
}

.open-btn {
  width: 100%;
  height: 36px;
  border: none;
  border-radius: 18px;
  background: #0ea5e9;
  color: #fff;
  font-size: 14px;
}

.open-btn:disabled {
  opacity: 0.6;
}

.tip {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
