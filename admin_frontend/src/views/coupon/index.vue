<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/store'
import { isChairman, isStoreManager, isEmployee } from '@/utils/permission'
import { getStoreListAPI } from '@/api/store'
import {
  addCouponAPI,
  getCouponByIdAPI,
  getCouponPageAPI,
  getCouponRecordsAPI,
  updateCouponAPI,
  updateCouponStatusAPI,
} from '@/api/coupon'

interface StoreItem {
  id: number
  name: string
}

const userInfoStore = useUserInfoStore()
const canEdit = computed(() => isChairman(userInfoStore.userInfo?.role) || isStoreManager(userInfoStore.userInfo?.role))
const isManager = computed(() => isStoreManager(userInfoStore.userInfo?.role))
const isStaff = computed(() => isEmployee(userInfoStore.userInfo?.role))

const pageData = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  name: '',
  couponType: undefined as number | undefined,
  status: undefined as number | undefined,
  publishType: undefined as number | undefined,
  storeId: undefined as number | undefined,
})
const list = ref<any[]>([])
const storeList = ref<StoreItem[]>([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive<any>({
  id: undefined,
  name: '',
  couponType: 1,
  publishType: 1,
  storeId: undefined,
  receiveType: 1,
  thresholdAmount: 0,
  discountAmount: 0,
  discountRate: 9,
  totalCount: 100,
  perUserLimit: 1,
  startTime: '',
  endTime: '',
  status: 1,
  remark: '',
})

const recordVisible = ref(false)
const recordTitle = ref('领取/使用记录')
const recordCouponId = ref<number | null>(null)
const recordPage = reactive({ page: 1, pageSize: 10, total: 0, recordType: 1 })
const recordList = ref<any[]>([])

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  publishType: [{ required: true, message: '请选择发布类型', trigger: 'change' }],
  receiveType: [{ required: true, message: '请选择领取类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入总发行量', trigger: 'blur' }],
  perUserLimit: [{ required: true, message: '请输入每人限领数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择生效开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择生效结束时间', trigger: 'change' }],
}

const couponTypeText = (val: number) => (val === 1 ? '满减券' : '折扣券')
const publishTypeText = (val: number) => (val === 1 ? '全局券' : '门店券')
const receiveTypeText = (val: number) => (val === 1 ? '全员可领' : '会员可领')

const initStore = async () => {
  const { data: res } = await getStoreListAPI()
  storeList.value = res.data || []
}

const resetQueryByRole = () => {
  if (isManager.value || isStaff.value) {
    pageData.publishType = 2
    pageData.storeId = Number(userInfoStore.userInfo?.storeId)
  }
}

const init = async () => {
  resetQueryByRole()
  const { data: res } = await getCouponPageAPI({ ...pageData })
  list.value = res.data.records || []
  pageData.total = res.data.total || 0
}

const openAdd = () => {
  form.id = undefined
  form.name = ''
  form.couponType = 1
  form.publishType = isManager.value ? 2 : 1
  form.storeId = isManager.value ? Number(userInfoStore.userInfo?.storeId) : undefined
  form.receiveType = 1
  form.thresholdAmount = 0
  form.discountAmount = 0
  form.discountRate = 9
  form.totalCount = 100
  form.perUserLimit = 1
  form.startTime = ''
  form.endTime = ''
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
}

const openEdit = async (id: number) => {
  const { data: res } = await getCouponByIdAPI(id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (isManager.value) {
    form.publishType = 2
    form.storeId = Number(userInfoStore.userInfo?.storeId)
  }
  if (form.id) {
    await updateCouponAPI({ ...form })
    ElMessage.success('修改成功')
  } else {
    await addCouponAPI({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  init()
}

const toggleStatus = async (row: any) => {
  await updateCouponStatusAPI(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  init()
}

const canRowEdit = (row: any) => {
  if (!canEdit.value) return false
  if (isManager.value) return row.publishType === 2 && row.storeId === Number(userInfoStore.userInfo?.storeId)
  return true
}

const openRecords = async (row: any) => {
  recordCouponId.value = row.id
  recordTitle.value = `${row.name} - 领取/使用记录`
  recordPage.page = 1
  recordVisible.value = true
  await loadRecords()
}

const loadRecords = async () => {
  if (!recordCouponId.value) return
  const { data: res } = await getCouponRecordsAPI(recordCouponId.value, {
    page: recordPage.page,
    pageSize: recordPage.pageSize,
    recordType: recordPage.recordType,
  })
  recordList.value = res.data.records || []
  recordPage.total = res.data.total || 0
}

initStore()
init()
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="pageData.name" placeholder="按名称筛选" style="width: 200px" clearable />
      <el-select v-model="pageData.couponType" placeholder="类型" clearable style="width: 120px">
        <el-option label="满减券" :value="1" />
        <el-option label="折扣券" :value="2" />
      </el-select>
      <el-select v-model="pageData.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-select v-if="!isManager && !isStaff" v-model="pageData.publishType" placeholder="发布类型" clearable style="width: 140px">
        <el-option label="全局券" :value="1" />
        <el-option label="门店券" :value="2" />
      </el-select>
      <el-select v-if="!isManager && !isStaff" v-model="pageData.storeId" placeholder="门店" clearable style="width: 180px">
        <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="init">查询</el-button>
      <el-button v-if="canEdit" type="success" @click="openAdd">新建优惠券</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="couponType" label="类型" width="100">
        <template #default="scope">{{ couponTypeText(scope.row.couponType) }}</template>
      </el-table-column>
      <el-table-column prop="publishType" label="发布类型" width="100">
        <template #default="scope">{{ publishTypeText(scope.row.publishType) }}</template>
      </el-table-column>
      <el-table-column prop="storeName" label="所属门店" min-width="120" />
      <el-table-column prop="receiveType" label="领取类型" width="100">
        <template #default="scope">{{ receiveTypeText(scope.row.receiveType) }}</template>
      </el-table-column>
      <el-table-column prop="thresholdAmount" label="门槛" width="90" />
      <el-table-column prop="discountAmount" label="优惠金额" width="100" />
      <el-table-column prop="discountRate" label="折扣率" width="90" />
      <el-table-column prop="totalCount" label="发行量" width="90" />
      <el-table-column prop="receiveCount" label="已领取" width="90" />
      <el-table-column prop="usedCount" label="已使用" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="openRecords(scope.row)">记录</el-button>
          <el-button v-if="canRowEdit(scope.row)" type="primary" link @click="openEdit(scope.row.id)">编辑</el-button>
          <el-button v-if="canRowEdit(scope.row)" :type="scope.row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="page"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="pageData.total"
      v-model:current-page="pageData.page"
      v-model:page-size="pageData.pageSize"
      @current-change="init"
      @size-change="init"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '新建优惠券'" width="720px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="优惠券类型" prop="couponType">
        <el-radio-group v-model="form.couponType">
          <el-radio :value="1">满减券</el-radio>
          <el-radio :value="2">折扣券</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="发布类型" prop="publishType">
        <el-radio-group v-model="form.publishType" :disabled="isManager">
          <el-radio :value="1">全局券</el-radio>
          <el-radio :value="2">门店券</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="所属门店" v-if="form.publishType === 2" prop="storeId">
        <el-select v-model="form.storeId" style="width: 100%" :disabled="isManager">
          <el-option v-for="item in storeList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="领取类型" prop="receiveType">
        <el-radio-group v-model="form.receiveType">
          <el-radio :value="1">全员可领</el-radio>
          <el-radio :value="2">会员可领</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="使用门槛" prop="thresholdAmount"><el-input-number v-model="form.thresholdAmount" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="优惠金额" v-if="form.couponType === 1" prop="discountAmount"><el-input-number v-model="form.discountAmount" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="折扣率" v-if="form.couponType === 2" prop="discountRate"><el-input-number v-model="form.discountRate" :min="0.1" :max="9.9" :precision="2" /></el-form-item>
      <el-form-item label="总发行量" prop="totalCount"><el-input-number v-model="form.totalCount" :min="1" /></el-form-item>
      <el-form-item label="每人限领" prop="perUserLimit"><el-input-number v-model="form.perUserLimit" :min="1" /></el-form-item>
      <el-form-item label="生效开始" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
      <el-form-item label="生效结束" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
      <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="recordVisible" :title="recordTitle" width="900px">
    <div class="toolbar">
      <el-radio-group v-model="recordPage.recordType" @change="loadRecords">
        <el-radio-button :value="1">领取记录</el-radio-button>
        <el-radio-button :value="2">使用记录</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="recordList" stripe>
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="userName" label="昵称" min-width="120" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="receiveTime" label="领取时间" min-width="160" />
      <el-table-column prop="useTime" label="使用时间" min-width="160" />
      <el-table-column prop="orderId" label="订单ID" min-width="100" />
    </el-table>
    <el-pagination
      class="page"
      background
      layout="total, prev, pager, next"
      :total="recordPage.total"
      v-model:current-page="recordPage.page"
      v-model:page-size="recordPage.pageSize"
      @current-change="loadRecords"
    />
  </el-dialog>
</template>

<style scoped lang="less">
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.page {
  margin-top: 16px;
  justify-content: center;
}
</style>
