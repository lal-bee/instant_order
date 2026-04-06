<template>
  <div class="page-order-list">
    <header class="detail-header">
      <img class="back" src="/icon/back.png" alt="返回" @click="goBack" />
      <span class="title">历史订单</span>
    </header>

    <div class="tabs">
      <div
        v-for="(item, index) in statusOptions"
        :key="index"
        class="tab"
        :class="{ active: index === activeIndex }"
        @click="switchTab(index)"
      >
        {{ item.name }}
      </div>
    </div>

    <div class="list-wrap">
      <div v-if="loading && historyOrders.length === 0" class="loading-tip">加载中...</div>
      <div v-else-if="historyOrders.length === 0" class="empty-tip">暂无订单</div>
      <div
        v-else
        v-for="(item, i) in historyOrders"
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
            <div class="pack">共{{ item.packAmount ?? 0 }}份</div>
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
      <div v-if="hasMore && historyOrders.length > 0" class="load-more" @click="loadMore">
        {{ loadMoreLoading ? '加载中...' : '加载更多' }}
      </div>
      <div v-else-if="historyOrders.length > 0 && !hasMore" class="no-more">没有更多了</div>
    </div>

    <PushMsg ref="pushMsgRef" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderPageAPI, reOrderAPI, urgeOrderAPI } from '@/api/order'
import { cleanCartAPI } from '@/api/cart'
import { showToast } from '@/utils/toast'
import PushMsg from '@/components/PushMsg.vue'

const router = useRouter()
const statusOptions = [
  { status: undefined, name: '全部订单' },
  { status: 1, name: '待付款' },
  { status: 5, name: '已完成' },
  { status: 6, name: '已取消' },
]
const statusMap = {
  0: { name: '全部订单' },
  1: { name: '待付款' },
  2: { name: '待接单' },
  3: { name: '已接单' },
  4: { name: '派送中' },
  5: { name: '已完成' },
  6: { name: '已取消' },
}

const activeIndex = ref(0)
const historyOrders = ref([])
const orderDTO = ref({ page: 1, pageSize: 6 })
const total = ref(0)
const loading = ref(true)
const loadMoreLoading = ref(false)
const pushMsgRef = ref(null)

const hasMore = ref(true)

function goBack() {
  router.replace('/my')
}

async function fetchList(isLoadMore) {
  if (isLoadMore) loadMoreLoading.value = true
  else loading.value = true
  const params = { page: orderDTO.value.page, pageSize: orderDTO.value.pageSize }
  if (statusOptions[activeIndex.value].status != null) {
    params.status = statusOptions[activeIndex.value].status
  }
  try {
    const res = await getOrderPageAPI(params)
    const records = res.data?.records || []
    const tot = res.data?.total ?? 0
    total.value = tot
    if (isLoadMore) {
      historyOrders.value = historyOrders.value.concat(records)
    } else {
      historyOrders.value = records
    }
    hasMore.value = historyOrders.value.length < tot
  } catch (e) {
    if (!isLoadMore) historyOrders.value = []
  }
  loading.value = false
  loadMoreLoading.value = false
}

function switchTab(index) {
  activeIndex.value = index
  orderDTO.value.page = 1
  fetchList(false)
}

function loadMore() {
  if (loadMoreLoading.value || !hasMore.value) return
  if (orderDTO.value.page * orderDTO.value.pageSize >= total.value) {
    showToast('没有更多了')
    return
  }
  orderDTO.value.page += 1
  fetchList(true)
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
  } catch (e) {
    // toast in request
  }
}

async function pushOrder(id) {
  try {
    await urgeOrderAPI(id)
    pushMsgRef.value?.openPopup()
  } catch (e) {}
}

onMounted(() => {
  fetchList(false)
})
</script>

<style scoped>
.page-order-list {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  background: #eee;
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
.detail-header .back {
  position: absolute;
  left: 12px;
  top: calc(env(safe-area-inset-top, 0px) + 10px);
  width: 24px;
  height: 24px;
  cursor: pointer;
}
.detail-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.tabs {
  position: sticky;
  top: calc(44px + env(safe-area-inset-top, 0px));
  display: flex;
  background: #fff;
  padding: 10px 0;
  z-index: 99;
}
.tab {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: #333;
}
.tab.active {
  color: #00aaff;
  font-weight: 600;
}

.list-wrap {
  padding: 12px;
}
.loading-tip,
.empty-tip {
  text-align: center;
  padding: 40px 20px;
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
  padding: 16px;
  font-size: 13px;
  color: #999;
}
.load-more {
  cursor: pointer;
}
</style>
