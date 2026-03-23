<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addHeadquartersAPI,
  getHeadquartersListAPI,
  updateHeadquartersAPI,
  updateHeadquartersStatusAPI,
} from '@/api/headquarters'

interface HeadquartersItem {
  id: number
  name: string
  status: number
  createTime: string
}

const list = ref<HeadquartersItem[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: 0,
  name: '',
})
const rules = {
  name: [{ required: true, message: '请输入总店名称', trigger: 'blur' }],
}

const init = async () => {
  const { data: res } = await getHeadquartersListAPI()
  list.value = res.data || []
}

const openAdd = () => {
  isEdit.value = false
  form.id = 0
  form.name = ''
  dialogVisible.value = true
}

const openEdit = (row: HeadquartersItem) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  if (isEdit.value) {
    await updateHeadquartersAPI(form)
    ElMessage.success('总店更新成功')
  } else {
    await addHeadquartersAPI(form)
    ElMessage.success('总店新增成功')
  }
  dialogVisible.value = false
  init()
}

const toggleStatus = async (row: HeadquartersItem) => {
  await updateHeadquartersStatusAPI(row.id)
  ElMessage.success('状态更新成功')
  init()
}

init()
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增总店</el-button>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="name" label="总店名称" align="center" />
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

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑总店' : '新增总店'" width="500px">
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item label="总店名称" prop="name">
        <el-input v-model="form.name" />
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
