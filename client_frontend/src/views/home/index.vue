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
      <div v-if="tableContext.storeName || tableContext.tableNo" class="table-context">
        <div class="ctx-line"><span class="ctx-label">当前门店：</span>{{ tableContext.storeName || '-' }}</div>
        <div class="ctx-line"><span class="ctx-label">当前桌号：</span>{{ tableContext.tableNo || '-' }}</div>
      </div>
      <div class="cta-box">
        <div class="cta-title">准备好开吃了吗？</div>
        <div class="cta-subtitle">{{ tableContext.tableId ? '已识别桌号，点击继续点餐' : '请先扫描餐桌二维码后再开始点餐' }}</div>
        <button class="btn-order" type="button" :disabled="!tableContext.tableId" @click="toOrderPage">
          {{ tableContext.tableId ? '点击开始点餐' : '请先扫码进入' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTableInfoAPI } from '@/api/table'
import { showToast } from '@/utils/toast'
import { getTableId, getStoreId, getStoreName, getTableNo, persistTableContext } from '@/utils/url'

const router = useRouter()
const currentSlide = ref(0)
const tableContext = reactive({
  tableId: '',
  storeId: '',
  storeName: '',
  tableNo: '',
})
let timer = null

onMounted(async () => {
  timer = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % 3
  }, 2000)
  tableContext.tableId = getTableId()
  tableContext.storeId = getStoreId()
  tableContext.storeName = getStoreName()
  tableContext.tableNo = getTableNo()
  initTableContext()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function toOrderPage() {
  if (!tableContext.tableId) {
    showToast('请先扫描餐桌二维码')
    return
  }
  const query = {}
  if (tableContext.tableId) query.tableId = tableContext.tableId
  router.push({ path: '/order', query })
}

async function initTableContext() {
  const token = localStorage.getItem('token')
  const tableId = getTableId()
  if (!tableId || !token) return
  try {
    const res = await getTableInfoAPI(tableId)
    const data = res.data || {}
    persistTableContext(data)
    tableContext.tableId = String(data.tableId || tableId)
    tableContext.storeId = String(data.storeId || '')
    tableContext.storeName = data.storeName || ''
    tableContext.tableNo = data.tableNo || ''
  } catch (e) {
    tableContext.storeId = ''
    tableContext.storeName = ''
    tableContext.tableNo = ''
    showToast(e?.message || '二维码无效或餐桌不可用')
  }
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
  padding: calc(22px + env(safe-area-inset-top, 0px)) 16px calc(36px + env(safe-area-inset-bottom, 0px));
  box-sizing: border-box;
  gap: 22px;
}

/* 轮播：H5 用 div+transform 替代 uni swiper */
.swiper-wrap {
  width: 100%;
  max-width: 460px;
  height: clamp(180px, 50vw, 250px);
  overflow: hidden;
  position: relative;
  border-radius: 14px;
  flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(17, 24, 39, 0.12);
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
  bottom: 12px;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 420px;
  gap: 14px;
}

.table-context {
  width: 100%;
  padding: 12px 14px;
  border-radius: 8px;
  background: #ecfdf5;
  border: 1px solid #10B981;
  color: #065f46;
  font-size: 14px;
  box-sizing: border-box;
}

.ctx-line + .ctx-line {
  margin-top: 4px;
}

.ctx-label {
  font-weight: 600;
}

.cta-box {
  width: 100%;
  padding: 18px 16px 18px;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fffb 100%);
  border: 1px solid #d1fae5;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.12);
  box-sizing: border-box;
}

.cta-title {
  font-size: 18px;
  line-height: 1.3;
  color: #064e3b;
  font-weight: 600;
}

.cta-subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}

.btn-order {
  margin-top: 16px;
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(90deg, #10B981 0%, #059669 100%);
  cursor: pointer;
  line-height: 1;
  transition: transform 0.12s ease, box-shadow 0.2s ease, filter 0.2s ease;
  box-shadow: 0 8px 18px rgba(16, 185, 129, 0.28);
}

.btn-order:hover {
  filter: brightness(1.02);
}

.btn-order:active {
  transform: translateY(1px);
}

.btn-order:disabled {
  cursor: not-allowed;
  background: #9ca3af;
  box-shadow: none;
}
</style>
