<template>
  <div class="page-home">
    <!-- 轮播：替代小程序 swiper，用普通 div + 定时切换 -->
    <div class="swiper-wrap">
      <div class="swiper-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
        <div class="swiper-item"><img src="/images/swp1.png" alt="" /></div>
        <div class="swiper-item"><img src="/images/swp2.png" alt="" /></div>
        <div class="swiper-item"><img src="/images/swp3.png" alt="" /></div>
      </div>
      <div class="dots">
        <span
          v-for="(_, i) in 3"
          :key="i"
          class="dot"
          :class="{ active: i === currentSlide }"
          @click="currentSlide = i"
        />
      </div>
    </div>
    <div class="home-section">
      <img class="home-img" src="/images/home.png" alt="点餐" />
      <button class="btn-order" type="button" @click="toOrderPage">点击开始点餐</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreId, getTableId } from '@/utils/url'

const router = useRouter()
const currentSlide = ref(0)
let timer = null

onMounted(() => {
  timer = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % 3
  }, 2000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function toOrderPage() {
  const query = {}
  const storeId = getStoreId()
  const tableId = getTableId()
  if (storeId) query.storeId = storeId
  if (tableId) query.tableId = tableId
  router.push({ path: '/order', query })
}
</script>

<style scoped>
.page-home {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding: calc(16px + env(safe-area-inset-top, 0px)) 14px calc(28px + env(safe-area-inset-bottom, 0px));
  box-sizing: border-box;
}

/* 轮播：H5 用 div+transform 替代 uni swiper */
.swiper-wrap {
  width: 100%;
  max-width: 452px;
  height: clamp(150px, 44vw, 210px);
  overflow: hidden;
  position: relative;
  border-radius: 8px;
  flex-shrink: 0;
}

.swiper-track {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.swiper-item {
  flex: 0 0 100%;
  height: 100%;
}

.swiper-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dots {
  position: absolute;
  bottom: 10px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: background 0.2s;
}

.dot.active {
  background: #10B981;
}

.home-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.home-img {
  width: 160px;
  height: 160px;
  display: block;
  object-fit: contain;
}

.btn-order {
  margin-top: 20px;
  width: min(100%, 320px);
  height: 46px;
  border: none;
  border-radius: 23px;
  font-size: 16px;
  color: #fff;
  background: #10B981;
  cursor: pointer;
  line-height: 1;
}
</style>
