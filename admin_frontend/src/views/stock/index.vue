<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStoreListAPI } from '@/api/store'
import { getStockPageAPI, getStockWarningAPI, updateStockAPI } from '@/api/stock'
import { useUserInfoStore } from '@/store'
import { isChairman, isEmployee } from '@/utils/permission'

interface StoreItem {
  id: number
  name: string
}

interface StockRow {
  storeId: number
  storeName: string
  dishId: number
  dishName: string
  dishStatus: number
  onShelfStatus: number
  stock: number
  warningStock: number
  warning: number
}

const userInfoStore = useUserInfoStore()
const canEdit = computed(() => !isEmployee(userInfoStore.userInfo?.role))
const chairman = computed(() => isChairman(userInfoStore.userInfo?.role))

const loading = ref(false)
const warningCount = ref(0)
const storeList = ref<StoreItem[]>([])
const tableData = ref<StockRow[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  storeId: undefined as number | undefined,
  dishName: '',
  warningOnly: 0,
})
const total = ref(0)

const editDialogVisible = ref(false)
const editForm = reactive({
  storeId: 0,
  dishId: 0,
  dishName: '',
  stock: 0,
  warningStock: 0,
})

const ensureSuccess = <T>(res: any, fallback: string): T => {
  if (!res || res.code !== 0) {
    throw new Error(res?.msg || fallback)
  }
  return res.data as T
}

const loadStores = async () => {
  const { data } = await getStoreListAPI()
  const stores = ensureSuccess<StoreItem[]>(data, '加载门店失败') || []
  if (chairman.value) {
    storeList.value = [{ id: 0, name: '全部门店' }, ...stores]
    query.storeId = 0
  } else {
    storeList.value = stores
    const myStoreId = Number(userInfoStore.userInfo?.storeId || stores[0]?.id || 0)
    query.storeId = myStoreId || undefined
  }
}

const queryStoreId = () => {
  if (chairman.value && query.storeId === 0) {
    return undefined
  }
  return query.storeId
}

const loadPage = async () => {
  loading.value = true
  try {
    const { data } = await getStockPageAPI({
      page: query.page,
      pageSize: query.pageSize,
      storeId: queryStoreId(),
      dishName: query.dishName || undefined,
      warningOnly: query.warningOnly || undefined,
    })
    const pageData = ensureSuccess<{ total: number; records: StockRow[] }>(data, '加载库存列表失败')
    total.value = Number(pageData?.total || 0)
    tableData.value = pageData?.records || []
  } finally {
    loading.value = false
  }
}

const loadWarningCount = async () => {
  const { data } = await getStockWarningAPI({
    storeId: queryStoreId(),
    dishName: query.dishName || undefined,
  })
  const warningList = ensureSuccess<StockRow[]>(data, '加载预警数据失败') || []
  warningCount.value = warningList.length
}

const loadData = async () => {
  await Promise.all([loadPage(), loadWarningCount()])
}

const onSearch = async () => {
  query.page = 1
  await loadData()
}

const onReset = async () => {
  query.page = 1
  query.pageSize = 10
  query.dishName = ''
  query.warningOnly = 0
  query.storeId = chairman.value ? 0 : Number(userInfoStore.userInfo?.storeId || undefined)
  await loadData()
}

const onPageChange = async (page: number) => {
  query.page = page
  await loadData()
}

const onPageSizeChange = async (pageSize: number) => {
  query.pageSize = pageSize
  query.page = 1
  await loadData()
}

const openEditDialog = (row: StockRow) => {
  if (!canEdit.value) {
    ElMessage.error('普通店员无编辑权限')
    return
  }
  editForm.storeId = row.storeId
  editForm.dishId = row.dishId
  editForm.dishName = row.dishName
  editForm.stock = row.stock
  editForm.warningStock = row.warningStock
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (editForm.stock < 0 || editForm.warningStock < 0) {
    ElMessage.error('库存和预警库存不能小于0')
    return
  }
  const { data } = await updateStockAPI({
    storeId: editForm.storeId,
    dishId: editForm.dishId,
    stock: Number(editForm.stock),
    warningStock: Number(editForm.warningStock),
  })
  ensureSuccess(data, '保存库存失败')
  ElMessage.success('库存更新成功')
  editDialogVisible.value = false
  await loadData()
}

loadStores().then(loadData)
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.storeId" placeholder="门店" @change="onSearch">
        <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-input v-model.trim="query.dishName" placeholder="请输入菜品名称" clearable @keyup.enter="onSearch" />
      <el-select v-model="query.warningOnly" placeholder="预警筛选" @change="onSearch">
        <el-option label="全部" :value="0" />
        <el-option label="仅预警" :value="1" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-tag type="danger">当前预警数：{{ warningCount }}</el-tag>
    </div>

    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="storeName" label="门店" min-width="140" />
      <el-table-column prop="dishName" label="菜品" min-width="160" />
      <el-table-column label="菜品状态" width="110" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.dishStatus === 1 ? 'success' : 'info'">{{ scope.row.dishStatus === 1 ? '起售' : '停售' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="当前库存" width="120" align="center" />
      <el-table-column prop="warningStock" label="预警库存" width="120" align="center" />
      <el-table-column label="预警状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.warning === 1 ? 'danger' : 'success'">{{ scope.row.warning === 1 ? '预警' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center">
        <template #default="scope">
          <el-button v-if="canEdit" type="primary" link @click="openEditDialog(scope.row)">修改</el-button>
          <span v-else class="readonly-text">仅查看</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="page"
      :current-page="query.page"
      :page-size="query.pageSize"
      :page-sizes="[10, 20, 30]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="onPageSizeChange"
      @current-change="onPageChange"
    />
  </el-card>

  <el-dialog v-model="editDialogVisible" title="修改库存" width="500px">
    <el-form label-width="90px">
      <el-form-item label="菜品">
        <el-input :model-value="editForm.dishName" disabled />
      </el-form-item>
      <el-form-item label="当前库存">
        <el-input-number v-model="editForm.stock" :min="0" :step="1" />
      </el-form-item>
      <el-form-item label="预警库存">
        <el-input-number v-model="editForm.warningStock" :min="0" :step="1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar .el-input,
.toolbar .el-select {
  width: 180px;
}

.page {
  margin-top: 16px;
  justify-content: center;
}

.readonly-text {
  color: #909399;
}
</style>
