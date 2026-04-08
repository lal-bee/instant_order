<template>
  <div class="page-my">
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">我的</span>
    </header>

    <div class="my-info">
      <div class="head">
        <img :src="user.pic || '/images/login.png'" class="head-image" alt="头像" />
      </div>
      <div class="phone-name">
        <div class="name">
          <span class="name-text">{{ user.name || '未设置' }}</span>
          <span class="gender-tag">{{ user.gender === 0 ? '女士' : '男士' }}</span>
        </div>
        <div class="phone">{{ user.phone || '未设置' }}</div>
      </div>
    </div>

    <div class="white-box">
      <div class="menu-row" @click="goHistory">
        <span class="menu-text">历史订单</span>
        <span class="arrow">›</span>
      </div>
      <div class="menu-row" @click="goCouponCenter">
        <span class="menu-text">领券中心</span>
        <span class="arrow">›</span>
      </div>
      <div class="menu-row" @click="goMyCoupon">
        <span class="menu-text">我的优惠券</span>
        <span class="arrow">›</span>
      </div>
      <div class="menu-row" @click="goUpdateMy">
        <span class="menu-text">信息设置</span>
        <span class="arrow">›</span>
      </div>
    </div>

    <div class="history-section">
      <div class="section-title">最近订单</div>
      <div v-if="recentLoading" class="loading-tip">加载中...</div>
      <div v-else-if="recentOrders.length === 0" class="empty-tip">暂无订单</div>
      <div
        v-else
        v-for="(item, i) in recentOrders"
        :key="item.id || i"
        class="order-card"
        @click="toOrderDetail(item.id)"
      >
        <div class="card-row">
          <div class="card-left">
            <div class="order-no">订单号：{{ item.number }}</div>
            <div class="dish-scroll">
              <img
                v-for="(d, di) in (item.orderDetailList || []).slice(0, 5)"
                :key="di"
                :src="d.pic"
                class="dish-thumb"
                alt=""
              />
            </div>
            <div class="order-time">{{ item.orderTime }}</div>
          </div>
          <div class="card-right">
            <div class="status-tag">{{ statusMap[item.status]?.name || '未知' }}</div>
            <div class="price">￥{{ item.amount }}</div>
          </div>
        </div>
        <div class="card-actions">
          <button type="button" class="btn-reorder" @click.stop="reOrder(item.id)">再来一单</button>
          <button
            v-if="item.status === 2"
            type="button"
            class="btn-urge"
            @click.stop="pushOrder(item.id)"
          >
            催单
          </button>
        </div>
      </div>
      <div v-if="recentOrders.length > 0 && recentHasMore" class="load-more" @click="loadMoreRecent">
        {{ recentLoadMore ? '加载中...' : '加载更多' }}
      </div>
      <div v-else-if="recentOrders.length > 0" class="no-more">更多订单请到历史订单查看</div>
    </div>

    <PushMsg ref="pushMsgRef" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfoAPI } from '@/api/user'
import { getOrderPageAPI, reOrderAPI, urgeOrderAPI } from '@/api/order'
import { cleanCartAPI } from '@/api/cart'
import { showToast } from '@/utils/toast'
import PushMsg from '@/components/PushMsg.vue'

const router = useRouter()
const userStore = useUserStore()

const statusMap = {
  0: { name: '全部订单' },
  1: { name: '待付款' },
  2: { name: '已支付（待制作）' },
  3: { name: '制作中' },
  4: { name: '待取餐' },
  5: { name: '已完成' },
  6: { name: '已取消' },
}

const user = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: '未设置',
  pic: '',
})
const recentOrders = ref([])
const recentDTO = ref({ page: 1, pageSize: 6 })
const recentTotal = ref(0)
const recentLoading = ref(true)
const recentLoadMore = ref(false)
const recentHasMore = ref(true)
const pushMsgRef = ref(null)
const backIconError = ref(false)

async function getUserInfo() {
  const id = userStore.profile?.id
  if (!id) return
  user.id = id
  try {
    const res = await getUserInfoAPI(id)
    user.name = res.data?.name ?? ''
    user.gender = res.data?.gender ?? 1
    user.phone = res.data?.phone ?? '未设置'
    user.pic = res.data?.pic ?? ''
  } catch (e) {}
}

async function fetchRecent(isLoadMore) {
  if (isLoadMore) recentLoadMore.value = true
  else recentLoading.value = true
  try {
    const res = await getOrderPageAPI({
      page: recentDTO.value.page,
      pageSize: recentDTO.value.pageSize,
    })
    const records = res.data?.records || []
    recentTotal.value = res.data?.total ?? 0
    if (isLoadMore) {
      recentOrders.value = recentOrders.value.concat(records)
    } else {
      recentOrders.value = records
    }
    recentHasMore.value = recentOrders.value.length < recentTotal.value && recentOrders.value.length < 12
  } catch (e) {
    if (!isLoadMore) recentOrders.value = []
  }
  recentLoading.value = false
  recentLoadMore.value = false
}

function loadMoreRecent() {
  if (recentLoadMore.value || !recentHasMore.value) return
  if (recentOrders.value.length >= 12) {
    showToast('更多订单请到历史订单查看')
    return
  }
  recentDTO.value.page += 1
  fetchRecent(true)
}

function goHistory() {
  router.push('/order-list')
}
function goUpdateMy() {
  router.push('/update-my')
}
function goCouponCenter() {
  router.push('/coupon-center')
}
function goMyCoupon() {
  router.push('/my-coupon')
}
function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.replace('/order')
}
function toOrderDetail(id) {
  router.push({ path: '/order-detail', query: { orderId: id } })
}

async function reOrder(id) {
  try {
    await cleanCartAPI()
    await reOrderAPI(id)
    showToast('已加入购物车')
    router.replace({ path: '/order' })
  } catch (e) {}
}

async function pushOrder(id) {
  try {
    await urgeOrderAPI(id)
    pushMsgRef.value?.openPopup()
  } catch (e) {}
}

onMounted(() => {
  getUserInfo()
  fetchRecent(false)
})
</script>

<style scoped>
.page-my {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  background: #f8f8f8;
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

.my-info {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background: linear-gradient(180deg, #cceeff 0%, #e6f7ff 100%);
}
.head {
  width: 60px;
  height: 60px;
  margin-right: 16px;
}
.head-image {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  background: #fff;
}
.phone-name {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  min-width: 0;
}
.name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
}
.gender-tag {
  font-size: 12px;
  color: #666;
  margin-left: 8px;
}
.phone {
  font-size: 14px;
  color: #666;
}

.white-box {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}
.menu-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.menu-row:last-child {
  border-bottom: none;
}
.menu-text {
  font-size: 15px;
  color: #333;
}
.arrow {
  font-size: 18px;
  color: #999;
}

.history-section {
  padding: 0 12px 24px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.loading-tip,
.empty-tip {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 14px;
}
.order-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.card-row {
  display: flex;
  justify-content: space-between;
}
.card-left {
  flex: 1;
  min-width: 0;
}
.order-no {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}
.dish-scroll {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}
.dish-scroll::-webkit-scrollbar { display: none; }
.dish-thumb {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.order-time {
  font-size: 12px;
  color: #666;
}
.card-right {
  text-align: right;
}
.status-tag {
  font-size: 14px;
  color: #00aaff;
  margin-bottom: 8px;
}
.price {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.pack {
  font-size: 12px;
  color: #666;
}
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.btn-reorder {
  padding: 6px 14px;
  font-size: 13px;
  color: #00aaff;
  background: transparent;
  border: 1px solid #00aaff;
  border-radius: 16px;
  cursor: pointer;
}
.btn-urge {
  padding: 6px 14px;
  font-size: 13px;
  color: #fff;
  background: #00aaff;
  border: none;
  border-radius: 16px;
  cursor: pointer;
}
.load-more,
.no-more {
  text-align: center;
  padding: 12px;
  font-size: 13px;
  color: #999;
}
.load-more {
  cursor: pointer;
}
</style>
