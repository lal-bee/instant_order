<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserInfoStore } from '@/store'
import { isChairman, isStoreManager } from '@/utils/permission'
import { getStoreListAPI } from '@/api/store'
import {
  addStoreSpecialDishAPI,
  deleteStoreSpecialDishAPI,
  getStoreMenuCategoryListAPI,
  getStoreMenuDishListAPI,
  updateStoreMenuDishStatusAPI,
  updateStoreSpecialDishAPI,
} from '@/api/store-menu'

interface StoreItem {
  id: number
  name: string
}

interface CategoryItem {
  id: number
  name: string
}

interface StoreDishItem {
  dishId: number
  dishName: string
  name?: string
  pic: string
  price: number
  detail: string
  categoryId: number
  categoryName?: string
  status: number
  storeId: number
  storeName: string
  dishType: '标准菜品' | '特色菜品'
  editable?: boolean
}

interface StoreDishGroup {
  storeId: number
  storeName: string
  dishes: StoreDishItem[]
}

type ApiResp<T> = { code: number; msg?: string; data: T }

const userInfoStore = useUserInfoStore()
const userRole = computed(() => userInfoStore.userInfo?.role)
const chairman = computed(() => isChairman(userRole.value))
const manager = computed(() => isStoreManager(userRole.value))
const userStoreId = computed(() => Number(userInfoStore.userInfo?.storeId || 0))

const storeList = ref<StoreItem[]>([])
const categoryList = ref<CategoryItem[]>([])
const selectedStoreId = ref<number>()
const dishList = ref<StoreDishItem[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加特色菜')
const editingDishId = ref<number | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)
const formRef = ref()

const form = reactive({
  storeId: undefined as number | undefined,
  name: '',
  pic: '',
  detail: '',
  price: undefined as number | undefined,
  categoryId: undefined as number | undefined,
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const canAddSpecial = computed(() => chairman.value || manager.value)
const storeSelectorDisabled = computed(() => !chairman.value)
const showFormStoreSelector = computed(() => chairman.value && selectedStoreId.value === 0)
const groupedDishList = computed<StoreDishGroup[]>(() => {
  const groups = new Map<number, StoreDishGroup>()
  for (const dish of dishList.value) {
    const key = dish.storeId
    const existing = groups.get(key)
    if (existing) {
      existing.dishes.push(dish)
      continue
    }
    groups.set(key, {
      storeId: dish.storeId,
      storeName: dish.storeName,
      dishes: [dish],
    })
  }
  return Array.from(groups.values()).sort((a, b) => a.storeId - b.storeId)
})

const ensureSuccess = <T>(res: ApiResp<T> | undefined, fallbackMsg: string): T => {
  if (!res || res.code !== 0) {
    throw new Error(res?.msg || fallbackMsg)
  }
  return res.data
}

const resolveQueryStoreId = () => {
  if (chairman.value && selectedStoreId.value === 0) {
    return undefined
  }
  return selectedStoreId.value
}

const resolveOperateStoreId = (fallbackStoreId?: number): number | undefined => {
  if (chairman.value) {
    const targetStoreId = fallbackStoreId || form.storeId || selectedStoreId.value
    if (!targetStoreId || targetStoreId === 0) {
      ElMessage.warning('请先选择具体门店')
      return undefined
    }
    return targetStoreId
  }
  if (manager.value) {
    return userStoreId.value || undefined
  }
  return undefined
}

const canEditSpecialDish = (row: StoreDishItem) => {
  if (chairman.value) return true
  if (row.dishType !== '特色菜品') return false
  if (manager.value) return row.storeId === userStoreId.value && row.editable !== false
  return false
}

const canToggleStatus = (row: StoreDishItem) => {
  if (chairman.value) return true
  if (manager.value) {
    return row.storeId === userStoreId.value && row.dishType === '特色菜品' && row.editable !== false
  }
  return false
}

const getReadonlyHint = (row: StoreDishItem) => {
  if (canToggleStatus(row)) {
    return '可上下架'
  }
  return '仅查看'
}

const formatDishName = (row: StoreDishItem) => row.dishName || row.name || ''

const initStoreList = async () => {
  const { data } = await getStoreListAPI()
  const stores = ensureSuccess<StoreItem[]>(data, '加载门店列表失败') || []
  if (chairman.value) {
    storeList.value = [{ id: 0, name: '全部门店' }, ...stores]
    selectedStoreId.value = 0
  } else {
    storeList.value = stores
    selectedStoreId.value = Number(userInfoStore.userInfo?.storeId || stores[0]?.id)
  }
  await Promise.all([loadStoreDishes(), loadCategoryList()])
}

const loadCategoryList = async () => {
  const { data } = await getStoreMenuCategoryListAPI()
  categoryList.value = ensureSuccess<CategoryItem[]>(data, '加载分类失败') || []
}

const loadStoreDishes = async () => {
  if (!selectedStoreId.value && selectedStoreId.value !== 0) return
  const { data } = await getStoreMenuDishListAPI(resolveQueryStoreId())
  dishList.value = ensureSuccess<StoreDishItem[]>(data, '加载门店菜单失败') || []
}

const onStoreChange = async () => {
  await loadStoreDishes()
}

const onDishStatusChange = async (row: StoreDishItem, value: boolean) => {
  if (!canToggleStatus(row)) {
    ElMessage.error('当前无权限修改该菜品状态')
    return
  }
  const nextStatus = value ? 1 : 0
  const { data } = await updateStoreMenuDishStatusAPI({
    storeId: row.storeId,
    dishId: row.dishId,
    status: nextStatus,
  })
  ensureSuccess<null>(data, '修改上下架状态失败')
  ElMessage.success('菜品状态更新成功')
  row.status = nextStatus
}

const resetForm = () => {
  editingDishId.value = null
  form.storeId = undefined
  form.name = ''
  form.pic = ''
  form.detail = ''
  form.price = undefined
  form.categoryId = undefined
}

const openAddDialog = () => {
  if (!canAddSpecial.value) {
    ElMessage.error('当前账号无编辑权限')
    return
  }
  dialogTitle.value = '添加特色菜'
  resetForm()
  if (chairman.value && selectedStoreId.value && selectedStoreId.value !== 0) {
    form.storeId = selectedStoreId.value
  }
  dialogVisible.value = true
}

const openEditDialog = (row: StoreDishItem) => {
  if (!canEditSpecialDish(row)) return
  dialogTitle.value = row.dishType === '标准菜品' ? '编辑标准菜' : '编辑特色菜'
  editingDishId.value = row.dishId
  form.name = formatDishName(row)
  form.pic = row.pic
  form.detail = row.detail
  form.price = Number(row.price)
  form.categoryId = row.categoryId
  form.storeId = row.storeId
  dialogVisible.value = true
}

const submitDialog = async () => {
  const targetStoreId = resolveOperateStoreId(form.storeId)
  if (!targetStoreId) return
  if (chairman.value && (!targetStoreId || targetStoreId === 0)) {
    ElMessage.warning('请选择目标门店')
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) return
  const payload = {
    storeId: targetStoreId,
    name: form.name,
    pic: form.pic,
    detail: form.detail,
    price: Number(form.price),
    categoryId: Number(form.categoryId),
  }
  if (editingDishId.value) {
    const { data } = await updateStoreSpecialDishAPI(editingDishId.value, payload)
    ensureSuccess<null>(data, '菜品修改失败')
    ElMessage.success('菜品修改成功')
  } else {
    const { data } = await addStoreSpecialDishAPI(payload)
    ensureSuccess<null>(data, '特色菜新增失败')
    ElMessage.success('特色菜新增成功')
  }
  dialogVisible.value = false
  await loadStoreDishes()
}

const removeSpecialDish = async (row: StoreDishItem) => {
  if (!canEditSpecialDish(row)) return
  await ElMessageBox.confirm(`确认删除菜品「${formatDishName(row)}」吗？`, '提示', { type: 'warning' })
  const { data } = await deleteStoreSpecialDishAPI(row.dishId)
  ensureSuccess<null>(data, '菜品删除失败')
  ElMessage.success('菜品删除成功')
  await loadStoreDishes()
}

const chooseImg = () => {
  if (imageInputRef.value) {
    imageInputRef.value.click()
  }
}

const onFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return
  const fr = new FileReader()
  fr.readAsDataURL(files[0])
  fr.onload = () => {
    form.pic = (fr.result as string) || ''
  }
}

initStoreList()
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="selectedStoreId" placeholder="请选择门店" :disabled="storeSelectorDisabled" @change="onStoreChange">
        <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button v-if="canAddSpecial" type="success" @click="openAddDialog">添加特色菜</el-button>
    </div>

    <div v-for="group in groupedDishList" :key="group.storeId" class="store-group">
      <div class="store-title">{{ group.storeName }}</div>
      <el-table :data="group.dishes" stripe>
        <el-table-column prop="dishName" label="菜品名称" min-width="140" />
        <el-table-column prop="pic" label="图片" width="100" align="center">
          <template #default="scope">
            <img v-if="scope.row.pic" :src="scope.row.pic" alt="" />
            <span v-else class="readonly-text">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="140" align="center" />
        <el-table-column label="菜品类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.dishType === '特色菜品' ? 'warning' : 'info'">
              {{ scope.row.dishType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="center" />
        <el-table-column label="上下架状态" width="140" align="center">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status === 1"
              :disabled="!canToggleStatus(scope.row)"
              @change="(value:boolean) => onDishStatusChange(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="描述" min-width="220" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button v-if="canEditSpecialDish(scope.row)" link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="canEditSpecialDish(scope.row)" link type="danger" @click="removeSpecialDish(scope.row)">删除</el-button>
            <span v-if="!canEditSpecialDish(scope.row)" class="readonly-text">{{ getReadonlyHint(scope.row) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item v-if="showFormStoreSelector" label="门店">
        <el-select v-model="form.storeId" placeholder="请选择门店">
          <el-option v-for="item in storeList.filter(s => s.id !== 0)" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="图片" prop="pic">
        <div class="dialog-image">
          <img v-if="form.pic" :src="form.pic" alt="" />
          <el-button type="primary" @click="chooseImg">选择图片</el-button>
          <input ref="imageInputRef" type="file" accept="image/*" class="hidden-file" @change="onFileChange" />
        </div>
      </el-form-item>
      <el-form-item label="描述" prop="detail">
        <el-input v-model="form.detail" type="textarea" />
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitDialog">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.toolbar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.store-group {
  margin-bottom: 20px;
}

.store-title {
  font-size: 14px;
  font-weight: 600;
  margin: 6px 0 10px;
}

img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
}

.readonly-text {
  color: #909399;
}

.dialog-image {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hidden-file {
  display: none;
}
</style>
