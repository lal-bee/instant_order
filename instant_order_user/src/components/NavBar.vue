<template>
  <div class="navbar-fixed">
    <div class="navbar" :style="{ paddingTop: safeTop }">
      <div class="logo-row">
        <img class="back" src="/icon/back.png" alt="返回" @click="back" />
        <img class="brand" src="/images/logo.png" alt="logo" />
        <span class="logo-text">扫码点餐</span>
      </div>
    </div>
    <div class="info">
      <div class="info1">
        <span class="status">{{ status ? '营业中' : '打烊中' }}</span>
        <span class="price">配送费6元</span>
      </div>
      <div class="info2">
        <span class="address">餐厅地址：广州市番禺区亚运城广场</span>
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
const safeTop = ref('20px')

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
  left: 0;
  width: 100%;
  z-index: 999;
}

.navbar {
  background-image: url(~@/assets/images/navigator_bg.png);
  background-size: cover;
  display: flex;
  align-items: center;
  padding: 12px 16px;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back {
  width: 24px;
  height: 20px;
  cursor: pointer;
}

.brand {
  height: 24px;
  width: auto;
}

.logo-text {
  color: #fff;
  font-size: 14px;
  padding-left: 12px;
  border-left: 1px solid rgba(255,255,255,0.6);
}

.info {
  margin: 8px 12px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  font-size: 14px;
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
  align-items: center;
  justify-content: space-between;
}

.address { color: #666; font-size: 13px; }

.phone-link {
  color: #10B981;
  text-decoration: none;
  font-size: 13px;
}

.blank {
  height: 12px;
  background: #fff;
}
</style>
