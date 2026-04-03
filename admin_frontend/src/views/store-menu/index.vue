<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStoreListAPI } from '@/api/store'
import { getStoreMenuDishListAPI, saveStoreMenuConfigAPI } from '@/api/store-menu'

interface StoreItem {
  id: number
  name: string
}

interface StoreDishItem {
  dishId: number
  name: string
  pic: string
  price: number
  detail: string
  categoryId: number
  onShelf: number
}

const storeList = ref<StoreItem[]>([])
const selectedStoreId = ref<number>()
const dishList = ref<StoreDishItem[]>([])
const checkedDishIds = ref<number[]>([])

const initStoreList = async () => {
  const { data: res } = await getStoreListAPI()
  storeList.value = res.data || []
  if (storeList.value.length > 0) {
    selectedStoreId.value = storeList.value[0].id
    await loadStoreDishes()
  }
}

const loadStoreDishes = async () => {
  if (!selectedStoreId.value) return
  const { data: res } = await getStoreMenuDishListAPI(selectedStoreId.value)
  dishList.value = res.data || []
  checkedDishIds.value = dishList.value.filter((d) => d.onShelf === 1).map((d) => d.dishId)
}

const saveConfig = async () => {
  if (!selectedStoreId.value) return
  await saveStoreMenuConfigAPI({
    storeId: selectedStoreId.value,
    dishIds: checkedDishIds.value,
  })
  ElMessage.success('门店菜单配置保存成功')
  await loadStoreDishes()
}

initStoreList()
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="selectedStoreId" placeholder="请选择门店" @change="loadStoreDishes">
        <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="saveConfig">保存上架配置</el-button>
    </div>

    <el-table :data="dishList" stripe>
      <el-table-column label="上架" width="100" align="center">
        <template #default="scope">
          <el-checkbox :label="scope.row.dishId" v-model="checkedDishIds" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="菜品名称" align="center" />
      <el-table-column prop="pic" label="图片" align="center">
        <template #default="scope">
          <img v-if="scope.row.pic" :src="scope.row.pic" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" align="center" />
      <el-table-column prop="detail" label="描述" align="center" />
      <el-table-column label="当前状态" align="center">
        <template #default="scope">
          <el-tag :type="checkedDishIds.includes(scope.row.dishId) ? 'success' : 'info'">
            {{ checkedDishIds.includes(scope.row.dishId) ? '已上架' : '未上架' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped lang="less">
.toolbar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
}
</style>
