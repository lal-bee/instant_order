<template>
  <div class="navbar-fixed">
    <div class="navbar" :style="{ paddingTop: safeTop }">
      <div class="logo-row">
        <button class="back-btn" type="button" @click="back" aria-label="返回">
          <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
          <span v-else class="back-fallback">‹</span>
        </button>
        <span class="logo-text">扫码点餐</span>
      </div>
    </div>
    <div class="info">
      <div class="info1">
        <span class="status">{{ status ? '营业中' : '打烊中' }}</span>
        <span class="price">堂食点餐</span>
      </div>
      <div class="info2">
        <span class="address">请扫码后在门店内点餐，当前订单仅支持堂食</span>
        <a href="tel:1999" class="phone-link">联系商家</a>
      </div>
    </div>
    <div class="blank"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStatusAPI } from '@/api/shop'

const props = defineProps({
  status: { type: Boolean, default: null }
})

const router = useRouter()
const statusLocal = ref(true)
const safeTop = ref('env(safe-area-inset-top, 0px)')
const backIconError = ref(false)

const status = computed(() => props.status !== null && props.status !== undefined ? props.status : statusLocal.value)

onMounted(async () => {
  if (props.status !== null && props.status !== undefined) return
  try {
    const res = await getStatusAPI()
    statusLocal.value = res.data === 1
  } catch (e) {
    statusLocal.value = false
  }
})

function back() {
  router.replace('/')
}
</script>

<style scoped>
.navbar-fixed {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  z-index: 999;
}

.navbar {
  background-image: url(~@/assets/images/navigator_bg.png);
  background-size: cover;
  display: flex;
  align-items: center;
  padding: 12px 14px;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.back-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.25);
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.back {
  width: 18px;
  height: 18px;
  display: block;
}

.back-fallback {
  color: #fff;
  font-size: 20px;
  line-height: 1;
  transform: translateY(-1px);
}

.logo-text {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.info {
  margin: 8px 10px;
  padding: 10px 12px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  font-size: 13px;
  color: #333;
}

.info1 {
  margin-bottom: 6px;
}

.status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #10B981;
  color: #fff;
  margin-right: 8px;
}

.price { color: #666; }

.info2 {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.address {
  color: #666;
  font-size: 12px;
  flex: 1;
  min-width: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.phone-link {
  color: #10B981;
  font-size: 12px;
  white-space: nowrap;
}

.blank {
  height: 12px;
  background: #fff;
}
</style>
