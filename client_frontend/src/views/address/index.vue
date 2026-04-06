<template>
  <div class="page-address">
    <header class="detail-header">
      <img class="back" src="/icon/back.png" alt="返回" @click="goBack" />
      <span class="title">地址管理</span>
    </header>

    <div class="list-wrap">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="!addressList.length" class="empty-tip">暂无地址</div>
      <div v-else class="address-list">
        <div
          v-for="(item, index) in addressList"
          :key="item.id"
          class="address-card"
          @click="selectAddress(item)"
        >
          <div class="card-top">
            <div class="card-left">
              <span class="tag" :class="'tag-' + tagClass(item.label)">{{ item.label || '其他' }}</span>
              <span class="address-text">{{ fullAddress(item) }}</span>
            </div>
            <div class="card-right" @click.stop="editAddress(item)">
              <span class="edit-btn">编辑</span>
            </div>
          </div>
          <div class="card-bottom">
            <label class="radio-wrap" @click.stop="setDefault(item)">
              <input
                type="radio"
                name="defaultAddr"
                :checked="item.isDefault === 1"
                @click.stop="setDefault(item)"
              />
              <span>设为默认地址</span>
            </label>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-btn">
      <button type="button" class="add-btn" @click="addAddress">+ 添加收货地址</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAddressStore } from '@/stores/address'
import { getAddressListAPI, updateDefaultAddressAPI } from '@/api/address'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const addressStore = useAddressStore()
const addressList = ref([])
const loading = ref(true)

function tagClass(label) {
  if (label === '公司') return '1'
  if (label === '家') return '2'
  if (label === '学校') return '3'
  return '4'
}

function fullAddress(item) {
  const parts = [item.provinceName, item.cityName, item.districtName, item.detail].filter(Boolean)
  return parts.join('') || '-'
}

async function loadList() {
  loading.value = true
  try {
    const res = await getAddressListAPI()
    addressList.value = res.data || []
  } catch (e) {
    addressList.value = []
  }
  loading.value = false
}

function goBack() {
  const backUrl = addressStore.addressBackUrl
  if (backUrl) {
    router.replace(backUrl)
  } else {
    router.replace('/my')
  }
}

function selectAddress(item) {
  const backUrl = addressStore.addressBackUrl
  if (backUrl === '/submit') {
    router.replace({
      path: '/submit',
      query: { ...route.query, address: JSON.stringify(item) },
    })
  }
}

function editAddress(item) {
  router.push({ path: '/address-edit', query: { type: 'edit', id: item.id } })
}

function addAddress() {
  router.push({ path: '/address-edit', query: { type: 'add' } })
}

async function setDefault(item) {
  try {
    await updateDefaultAddressAPI({ id: item.id })
    showToast('默认地址设置成功')
    loadList()
  } catch (e) {}
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-address {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  background: #f5f5f5;
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
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.address-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.card-left {
  flex: 1;
  min-width: 0;
}
.tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  margin-right: 8px;
  background: #e1f1fe;
  color: #333;
}
.tag-2 {
  background: #fef8e7;
}
.tag-3 {
  background: #e7fef8;
}
.tag-4 {
  background: #fee7e7;
}
.address-text {
  font-size: 14px;
  color: #333;
  display: block;
  line-height: 1.4;
  margin-top: 6px;
  word-break: break-all;
}
.card-right .edit-btn {
  font-size: 14px;
  color: #22ccff;
  cursor: pointer;
}
.card-bottom {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #333;
}
.radio-wrap {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.radio-wrap input {
  margin: 0;
}

.footer-btn {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 12px 14px calc(10px + env(safe-area-inset-bottom, 0px));
  background: #fff;
  border-top: 1px solid #eee;
  z-index: 99;
}
.add-btn {
  width: 100%;
  max-width: 320px;
  margin: 0 auto;
  display: block;
  height: 44px;
  line-height: 44px;
  font-size: 15px;
  color: #fff;
  background: #22ccff;
  border: none;
  border-radius: 22px;
  cursor: pointer;
}
</style>
