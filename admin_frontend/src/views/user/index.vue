<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addUserAPI,
  closeUserMemberAPI,
  deleteUserAPI,
  getUserByIdAPI,
  getUserPageAPI,
  openUserMemberAPI,
  updateUserAPI,
  updateUserMemberAPI,
  updateUserStatusAPI,
} from '@/api/user'

interface UserRow {
  id: number
  username: string
  name: string
  phone: string
  gender: number
  status: number
  isMember: number
  memberLevel: number
  memberExpireTime: string
  createTime: string
  memberStatusText: string
}

const userList = ref<UserRow[]>([])
const loading = ref(false)

const pageData = reactive({
  keyword: '',
  phone: '',
  status: undefined as number | undefined,
  isMember: undefined as number | undefined,
  page: 1,
  pageSize: 10,
  total: 0,
})

const userDialogVisible = ref(false)
const userDialogMode = ref<'add' | 'edit' | 'view'>('add')
const userFormLoading = ref(false)
const userForm = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  name: '',
  phone: '',
  gender: 1,
  pic: '',
})

const memberDialogVisible = ref(false)
const memberDialogMode = ref<'open' | 'update'>('open')
const memberFormLoading = ref(false)
const memberForm = reactive({
  userId: undefined as number | undefined,
  memberLevel: 1,
  memberExpireTime: '',
})

const statusType = (status?: number) => (status === 1 ? 'success' : 'danger')
const statusText = (status?: number) => (status === 1 ? '启用' : '禁用')
const genderText = (gender?: number) => (gender === 0 ? '女' : '男')
const memberText = (row: UserRow) => (row.memberStatusText === '是' ? '是' : '否')
const memberLevelText = (level?: number) => {
  if (level === 2) return '高级会员'
  if (level === 1) return '普通会员'
  return '普通用户'
}
const userDialogTitle = computed(() => {
  if (userDialogMode.value === 'add') return '新增用户'
  if (userDialogMode.value === 'edit') return '编辑用户'
  return '查看用户'
})
const userReadOnly = computed(() => userDialogMode.value === 'view')

const resetQuery = () => {
  pageData.page = 1
  loadPage()
}

const loadPage = async () => {
  loading.value = true
  try {
    const { data: res } = await getUserPageAPI({
      page: pageData.page,
      pageSize: pageData.pageSize,
      keyword: pageData.keyword,
      phone: pageData.phone,
      status: pageData.status,
      isMember: pageData.isMember,
    })
    userList.value = res.data.records || []
    pageData.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetUserForm = () => {
  userForm.id = undefined
  userForm.username = ''
  userForm.password = ''
  userForm.name = ''
  userForm.phone = ''
  userForm.gender = 1
  userForm.pic = ''
}

const openAddDialog = () => {
  userDialogMode.value = 'add'
  resetUserForm()
  userDialogVisible.value = true
}

const openEditDialog = async (id: number, mode: 'edit' | 'view') => {
  userDialogMode.value = mode
  userFormLoading.value = true
  userDialogVisible.value = true
  try {
    const { data: res } = await getUserByIdAPI(id)
    const data = res.data || {}
    userForm.id = data.id
    userForm.username = data.username || ''
    userForm.password = ''
    userForm.name = data.name || ''
    userForm.phone = data.phone || ''
    userForm.gender = data.gender ?? 1
    userForm.pic = data.pic || ''
  } finally {
    userFormLoading.value = false
  }
}

const submitUser = async () => {
  if (userDialogMode.value === 'view') {
    userDialogVisible.value = false
    return
  }
  if (!userForm.username && userDialogMode.value === 'add') {
    ElMessage.error('用户名不能为空')
    return
  }
  if (!userForm.password && userDialogMode.value === 'add') {
    ElMessage.error('密码不能为空')
    return
  }
  const payload = {
    id: userForm.id,
    username: userForm.username,
    password: userForm.password,
    name: userForm.name,
    phone: userForm.phone,
    gender: userForm.gender,
    pic: userForm.pic,
  }
  if (userDialogMode.value === 'add') {
    await addUserAPI(payload)
    ElMessage.success('新增用户成功')
  } else {
    await updateUserAPI(payload)
    ElMessage.success('更新用户成功')
  }
  userDialogVisible.value = false
  loadPage()
}

const changeStatus = async (row: UserRow) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateUserStatusAPI({ id: row.id, status: nextStatus })
  ElMessage.success('状态更新成功')
  loadPage()
}

const deleteUser = async (row: UserRow) => {
  await ElMessageBox.confirm(`确认删除用户【${row.name || row.username || row.id}】？系统将按禁用处理`, '提示', {
    type: 'warning',
  })
  await deleteUserAPI(row.id)
  ElMessage.success('已禁用该用户')
  loadPage()
}

const openMemberDialog = (mode: 'open' | 'update', row: UserRow) => {
  memberDialogMode.value = mode
  memberForm.userId = row.id
  memberForm.memberLevel = row.memberLevel && row.memberLevel > 0 ? row.memberLevel : 1
  memberForm.memberExpireTime = row.memberExpireTime || ''
  memberDialogVisible.value = true
}

const submitMember = async () => {
  if (!memberForm.userId) {
    ElMessage.error('用户ID不能为空')
    return
  }
  const payload = {
    userId: memberForm.userId,
    memberLevel: memberForm.memberLevel,
    memberExpireTime: memberForm.memberExpireTime || null,
  }
  memberFormLoading.value = true
  try {
    if (memberDialogMode.value === 'open') {
      await openUserMemberAPI(payload)
      ElMessage.success('开通会员成功')
    } else {
      await updateUserMemberAPI(payload)
      ElMessage.success('更新会员成功')
    }
    memberDialogVisible.value = false
    loadPage()
  } finally {
    memberFormLoading.value = false
  }
}

const closeMember = async (row: UserRow) => {
  await ElMessageBox.confirm(`确认取消用户【${row.name || row.username || row.id}】会员资格？`, '提示', {
    type: 'warning',
  })
  await closeUserMemberAPI({ userId: row.id })
  ElMessage.success('已取消会员资格')
  loadPage()
}

const handleCurrentChange = (val: number) => {
  pageData.page = val
  loadPage()
}

const handleSizeChange = (val: number) => {
  pageData.pageSize = val
  pageData.page = 1
  loadPage()
}

loadPage()
</script>

<template>
  <el-card class="user-card">
    <div class="horizontal">
      <el-input v-model="pageData.keyword" size="large" class="input" placeholder="请输入昵称/用户名" clearable />
      <el-input v-model="pageData.phone" size="large" class="input" placeholder="请输入手机号" clearable />
      <el-select v-model="pageData.status" size="large" class="input" clearable placeholder="用户状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select v-model="pageData.isMember" size="large" class="input" clearable placeholder="是否会员">
        <el-option label="是" :value="1" />
        <el-option label="否" :value="0" />
      </el-select>
      <el-button size="large" class="btn" round type="success" @click="resetQuery">查询</el-button>
      <el-button size="large" class="btn" type="primary" @click="openAddDialog">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="userList" stripe height="calc(100vh - 340px)">
      <el-table-column prop="id" label="用户ID" width="90" align="center" />
      <el-table-column prop="name" label="昵称" min-width="120" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" align="center" />
      <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
      <el-table-column label="用户状态" min-width="90" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)" round>{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否会员" min-width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.memberStatusText === '是' ? 'success' : 'info'" round>{{ memberText(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="会员等级" min-width="110" align="center">
        <template #default="scope">{{ memberLevelText(scope.row.memberLevel) }}</template>
      </el-table-column>
      <el-table-column prop="memberExpireTime" label="会员到期时间" min-width="170" align="center" />
      <el-table-column prop="createTime" label="创建时间" min-width="170" align="center" />
      <el-table-column label="操作" width="420" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" plain @click="openEditDialog(scope.row.id, 'view')">查看</el-button>
          <el-button size="small" type="primary" plain @click="openEditDialog(scope.row.id, 'edit')">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'danger' : 'success'" plain @click="changeStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          <el-button
            v-if="scope.row.memberStatusText !== '是'"
            size="small"
            type="success"
            @click="openMemberDialog('open', scope.row)"
          >
            开通会员
          </el-button>
          <el-button
            v-if="scope.row.memberStatusText === '是'"
            size="small"
            type="warning"
            @click="openMemberDialog('update', scope.row)"
          >
            修改会员
          </el-button>
          <el-button
            v-if="scope.row.memberStatusText === '是'"
            size="small"
            type="info"
            @click="closeMember(scope.row)"
          >
            取消会员
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="没有数据" />
      </template>
    </el-table>

    <el-pagination
      class="page"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="pageData.total"
      :page-sizes="[10, 20, 30, 50]"
      v-model:current-page="pageData.page"
      v-model:page-size="pageData.pageSize"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </el-card>

  <el-dialog v-model="userDialogVisible" :title="userDialogTitle" width="560px" destroy-on-close>
    <el-form v-loading="userFormLoading" label-width="95px">
      <el-form-item label="用户名">
        <el-input v-model="userForm.username" :disabled="userReadOnly || userDialogMode === 'edit'" />
      </el-form-item>
      <el-form-item label="密码" v-if="userDialogMode === 'add'">
        <el-input v-model="userForm.password" type="password" show-password :disabled="userReadOnly" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="userForm.name" :disabled="userReadOnly" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="userForm.phone" :disabled="userReadOnly" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="userForm.gender" :disabled="userReadOnly">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="头像URL">
        <el-input v-model="userForm.pic" :disabled="userReadOnly" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button v-if="!userReadOnly" type="primary" @click="submitUser">确定</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="memberDialogVisible" :title="memberDialogMode === 'open' ? '开通会员' : '修改会员'" width="560px">
    <el-form label-width="100px">
      <el-form-item label="会员等级">
        <el-radio-group v-model="memberForm.memberLevel">
          <el-radio :value="1">普通会员</el-radio>
          <el-radio :value="2">高级会员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="到期时间">
        <el-date-picker
          v-model="memberForm.memberExpireTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="不填则长期有效"
          clearable
          style="width: 100%;"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="memberFormLoading" @click="submitMember">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.user-card {
  max-width: 1600px;
  margin: 0 auto;
}

.horizontal {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.input {
  width: 220px;
}

.btn {
  min-width: 100px;
}

.el-table {
  width: 100%;
  margin: 16px 0;
}

.page {
  justify-content: center;
}
</style>

