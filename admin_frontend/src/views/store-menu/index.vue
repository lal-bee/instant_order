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
  saveStoreMenuConfigAPI,
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
  name: string
  pic: string
  price: number
  detail: string
  categoryId: number
  categoryName?: string
  onShelf: number
  dishType: 'STANDARD' | 'SPECIAL'
}

const userInfoStore = useUserInfoStore()
const canWrite = computed(() => isChairman(userInfoStore.userInfo?.role) || isStoreManager(userInfoStore.userInfo?.role))

const storeList = ref<StoreItem[]>([])
const categoryList = ref<CategoryItem[]>([])
const selectedStoreId = ref<number>()
const dishList = ref<StoreDishItem[]>([])
const checkedDishIds = ref<number[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加特色菜')
const editingDishId = ref<number | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)
const formRef = ref()

const form = reactive({
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

const initStoreList = async () => {
  const { data: res } = await getStoreListAPI()
  storeList.value = res.data || []
  if (storeList.value.length > 0) {
    selectedStoreId.value = storeList.value[0].id
    await Promise.all([loadStoreDishes(), loadCategoryList()])
  }
}

const loadCategoryList = async () => {
  const { data: res } = await getStoreMenuCategoryListAPI()
  categoryList.value = res.data || []
}

const loadStoreDishes = async () => {
  if (!selectedStoreId.value) return
  const { data: res } = await getStoreMenuDishListAPI(selectedStoreId.value)
  dishList.value = res.data || []
  checkedDishIds.value = dishList.value.filter((d) => d.onShelf === 1).map((d) => d.dishId)
}

const onStoreChange = async () => {
  await Promise.all([loadStoreDishes(), loadCategoryList()])
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

const resetForm = () => {
  editingDishId.value = null
  form.name = ''
  form.pic = ''
  form.detail = ''
  form.price = undefined
  form.categoryId = undefined
}

const openAddDialog = () => {
  dialogTitle.value = '添加特色菜'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row: StoreDishItem) => {
  if (row.dishType !== 'SPECIAL') return
  dialogTitle.value = '编辑特色菜'
  editingDishId.value = row.dishId
  form.name = row.name
  form.pic = row.pic
  form.detail = row.detail
  form.price = Number(row.price)
  form.categoryId = row.categoryId
  dialogVisible.value = true
}

const submitDialog = async () => {
  if (!selectedStoreId.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  const payload = {
    storeId: selectedStoreId.value,
    name: form.name,
    pic: form.pic,
    detail: form.detail,
    price: Number(form.price),
    categoryId: Number(form.categoryId),
  }
  if (editingDishId.value) {
    await updateStoreSpecialDishAPI(editingDishId.value, payload)
    ElMessage.success('特色菜修改成功')
  } else {
    await addStoreSpecialDishAPI(payload)
    ElMessage.success('特色菜新增成功')
  }
  dialogVisible.value = false
  await loadStoreDishes()
}

const removeSpecialDish = async (row: StoreDishItem) => {
  if (row.dishType !== 'SPECIAL') return
  await ElMessageBox.confirm(`确认删除特色菜「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteStoreSpecialDishAPI(row.dishId)
  ElMessage.success('特色菜删除成功')
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
      <el-select v-model="selectedStoreId" placeholder="请选择门店" @change="onStoreChange">
        <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button v-if="canWrite" type="primary" @click="saveConfig">保存上架配置</el-button>
      <el-button v-if="canWrite" type="success" @click="openAddDialog">添加特色菜</el-button>
    </div>

    <el-table :data="dishList" stripe>
      <el-table-column label="上架" width="100" align="center">
        <template #default="scope">
          <el-checkbox v-model="checkedDishIds" :label="scope.row.dishId" :disabled="!canWrite" />
        </template>
      </el-table-column>
      <el-table-column label="类型" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.dishType === 'SPECIAL' ? 'warning' : 'info'">
            {{ scope.row.dishType }}
          </el-tag>
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
      <el-table-column v-if="canWrite" label="操作" width="180" align="center">
        <template #default="scope">
          <el-button
            v-if="scope.row.dishType === 'SPECIAL'"
            link
            type="primary"
            @click="openEditDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="scope.row.dishType === 'SPECIAL'"
            link
            type="danger"
            @click="removeSpecialDish(scope.row)"
          >
            删除
          </el-button>
          <span v-if="scope.row.dishType === 'STANDARD'" class="readonly-text">标准菜只读</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
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
