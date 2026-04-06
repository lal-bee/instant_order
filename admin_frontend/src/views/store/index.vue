<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addStoreAPI, getStoreListAPI, updateStoreAPI, updateStoreStatusAPI } from '@/api/store'
import { getEmployeePageListAPI, getManagerOptionsAPI } from '@/api/employee'

interface StoreItem {
  id: number
  name: string
  managerEmployeeId?: number
  managerName?: string
  status: number
  createTime: string
}
interface ManagerOption {
  id: number
  name: string
  account: string
  storeName?: string
}

const list = ref<StoreItem[]>([])
const managerList = ref<ManagerOption[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: 0,
  name: '',
  managerEmployeeId: undefined as number | undefined,
})
const rules = {
  name: [{ required: true, message: '请输入分店名称', trigger: 'blur' }],
  managerEmployeeId: [{ required: true, message: '请选择关联店长', trigger: 'change' }],
}

// 分店新增/编辑时，关联店长必须来自员工表中 role=1 的可用员工
const loadManagerOptions = async () => {
  try {
    const { data: res } = await getManagerOptionsAPI()
    if (res.code === 0 && Array.isArray(res.data) && res.data.length > 0) {
      managerList.value = res.data
      return
    }
  } catch (_error) {
    // ignore and use fallback below
  }

  // 兼容兜底：若专用接口不可用，回退到分页接口按 role=1 拉取店长列表
  try {
    const { data: pageRes } = await getEmployeePageListAPI({
      page: 1,
      pageSize: 200,
      role: '1',
    })
    if (pageRes.code === 0) {
      managerList.value = pageRes.data?.records || []
      return
    }
  } catch (_error) {
    // continue
  }

  managerList.value = []
  ElMessage.error('加载店长列表失败，请稍后重试')
}

const managerLabel = (manager: ManagerOption) => {
  const namePart = manager.name || `员工${manager.id}`
  const accountPart = manager.account ? `（${manager.account}）` : ''
  const storePart = manager.storeName ? ` - ${manager.storeName}` : ''
  return `${namePart}${accountPart}${storePart}`
}

const normalizeManagerValue = (value: string | number | undefined) => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  return typeof value === 'number' ? value : Number(value)
}

const init = async () => {
  const { data: storeRes } = await getStoreListAPI()
  list.value = storeRes.data || []
  await loadManagerOptions()
}

const openAdd = async () => {
  await loadManagerOptions()
  isEdit.value = false
  form.id = 0
  form.name = ''
  form.managerEmployeeId = managerList.value[0]?.id
  dialogVisible.value = true
}

const openEdit = async (row: StoreItem) => {
  // 先拉取选项，再回显 value，避免 value 有值但 options 为空导致下拉空白
  await loadManagerOptions()
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.managerEmployeeId = normalizeManagerValue(row.managerEmployeeId)
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  if (isEdit.value) {
    await updateStoreAPI(form)
    ElMessage.success('分店更新成功')
  } else {
    await addStoreAPI(form)
    ElMessage.success('分店新增成功')
  }
  dialogVisible.value = false
  init()
}

const toggleStatus = async (row: StoreItem) => {
  await updateStoreStatusAPI(row.id)
  ElMessage.success('状态更新成功')
  init()
}

init()
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增分店</el-button>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="name" label="分店名称" align="center" />
      <el-table-column prop="managerName" label="当前店长" align="center" />
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" align="center" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button :type="scope.row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分店' : '新增分店'" width="500px">
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item label="分店名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="关联店长" prop="managerEmployeeId">
        <el-select v-model="form.managerEmployeeId" style="width: 100%" filterable>
          <el-option
            v-for="item in managerList"
            :key="item.id"
            :label="managerLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.toolbar {
  margin-bottom: 16px;
}
</style>
