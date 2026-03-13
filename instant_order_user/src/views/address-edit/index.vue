<template>
  <div class="page-address-edit">
    <header class="detail-header">
      <img class="back" src="/icon/back.png" alt="返回" @click="goBack" />
      <span class="title">{{ isEdit ? '编辑收货地址' : '新增收货地址' }}</span>
    </header>

    <form class="form" @submit.prevent="save">
      <div class="form-item">
        <label class="label">联系人</label>
        <input v-model="form.consignee" class="input" placeholder="请输入联系人" maxlength="5" />
      </div>
      <div class="form-item">
        <label class="label">性别</label>
        <div class="radio-group">
          <label class="radio">
            <input v-model="form.gender" type="radio" :value="1" />
            <span>男士</span>
          </label>
          <label class="radio">
            <input v-model="form.gender" type="radio" :value="0" />
            <span>女士</span>
          </label>
        </div>
      </div>
      <div class="form-item">
        <label class="label">手机号</label>
        <input
          v-model="form.phone"
          type="tel"
          class="input"
          placeholder="请输入手机号"
          maxlength="11"
        />
      </div>
      <div class="form-item">
        <label class="label">所在地区</label>
        <input
          v-model="regionText"
          class="input"
          placeholder="省、市、区，如：北京市 市辖区 西城区"
          @focus="showRegionTip = true"
        />
      </div>
      <div class="form-item">
        <label class="label">详细地址</label>
        <input v-model="form.detail" class="input" placeholder="精确到门牌号" />
      </div>
      <div class="form-item tag-item">
        <label class="label">标签</label>
        <span
          v-for="opt in tagOptions"
          :key="opt"
          class="tag"
          :class="{ active: form.label === opt }"
          @click="form.label = opt"
        >
          {{ opt }}
        </span>
      </div>
      <div class="btn-row">
        <button type="submit" class="btn btn-primary">保存地址</button>
        <button
          v-if="isEdit"
          type="button"
          class="btn btn-del"
          @click="deleteAddress"
        >
          删除地址
        </button>
      </div>
    </form>

    <div v-if="showRegionTip" class="tip-mask" @click="showRegionTip = false">
      <div class="tip-box">
        <p>请手动输入省市区，例如：北京市 市辖区 西城区</p>
        <button type="button" class="tip-btn" @click="showRegionTip = false">知道了</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAddressByIdAPI, addAddressAPI, updateAddressAPI, deleteAddressAPI } from '@/api/address'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const tagOptions = ['公司', '家', '学校', '其他']
const showRegionTip = ref(false)

const isEdit = computed(() => route.query.type === 'edit' && route.query.id)
const editId = computed(() => (route.query.id ? Number(route.query.id) : null))

const form = reactive({
  id: null,
  consignee: '',
  phone: '',
  label: '',
  gender: 1,
  provinceCode: '110000',
  provinceName: '',
  cityCode: '110100',
  cityName: '',
  districtCode: '110102',
  districtName: '',
  detail: '',
})

const regionText = computed({
  get() {
    const parts = [form.provinceName, form.cityName, form.districtName].filter(Boolean)
    return parts.join(' ') || ''
  },
  set(val) {
    const parts = (val || '').trim().split(/\s+/)
    form.provinceName = parts[0] || ''
    form.cityName = parts[1] || ''
    form.districtName = parts[2] || ''
  },
})

async function loadDetail() {
  if (!editId.value) return
  try {
    const res = await getAddressByIdAPI(editId.value)
    const d = res.data || {}
    form.id = d.id
    form.consignee = d.consignee ?? ''
    form.phone = d.phone ?? ''
    form.gender = d.gender ?? 1
    form.label = d.label ?? ''
    form.detail = d.detail ?? ''
    form.provinceCode = d.provinceCode ?? '110000'
    form.cityCode = d.cityCode ?? '110100'
    form.districtCode = d.districtCode ?? '110102'
    form.provinceName = d.provinceName ?? ''
    form.cityName = d.cityName ?? ''
    form.districtName = d.districtName ?? ''
  } catch (e) {}
}

function goBack() {
  router.replace('/address')
}

function validate() {
  if (!form.consignee?.trim()) {
    showToast('联系人不能为空')
    return false
  }
  if (!form.phone?.trim()) {
    showToast('手机号不能为空')
    return false
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(form.phone)) {
    showToast('手机号格式不正确')
    return false
  }
  if (!form.label) {
    showToast('请选择标签')
    return false
  }
  if (!form.provinceName || !form.cityName || !form.districtName) {
    showToast('请填写所在地区')
    return false
  }
  return true
}

async function save() {
  if (!validate()) return
  const payload = {
    ...form,
    provinceName: form.provinceName,
    cityName: form.cityName,
    districtName: form.districtName,
  }
  if (isEdit.value) {
    try {
      await updateAddressAPI(payload)
      showToast('修改成功')
      router.replace('/address')
    } catch (e) {}
  } else {
    delete payload.id
    try {
      await addAddressAPI(payload)
      showToast('添加成功')
      router.replace('/address')
    } catch (e) {}
  }
}

async function deleteAddress() {
  if (!editId.value) return
  if (!window.confirm('确定删除该地址？')) return
  try {
    await deleteAddressAPI(editId.value)
    showToast('删除成功')
    router.replace('/address')
  } catch (e) {}
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page-address-edit {
  min-height: 100vh;
  padding-top: 52px;
  padding-bottom: 24px;
  background: #fff;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #eee;
  z-index: 100;
}
.detail-header .back {
  position: absolute;
  left: 12px;
  width: 24px;
  height: 24px;
  cursor: pointer;
}
.detail-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.form {
  padding: 16px;
}
.form-item {
  display: flex;
  align-items: center;
  min-height: 48px;
  border-bottom: 1px solid #efefef;
  padding: 10px 0;
}
.label {
  width: 90px;
  flex-shrink: 0;
  font-size: 14px;
  color: #333;
}
.input {
  flex: 1;
  border: none;
  font-size: 14px;
  outline: none;
}
.input::placeholder {
  color: #999;
}
.radio-group {
  display: flex;
  gap: 24px;
}
.radio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.tag-item {
  flex-wrap: wrap;
}
.tag {
  display: inline-block;
  padding: 6px 12px;
  margin-right: 10px;
  margin-top: 6px;
  font-size: 13px;
  border: 1px solid #e5e4e4;
  border-radius: 6px;
  cursor: pointer;
  color: #333;
}
.tag.active {
  background: #e1f1fe;
  border-color: #e1f1fe;
}
.btn-row {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.btn {
  height: 44px;
  line-height: 44px;
  font-size: 15px;
  border-radius: 22px;
  cursor: pointer;
  border: none;
}
.btn-primary {
  background: #22ccff;
  color: #fff;
}
.btn-del {
  background: #f6f6f6;
  color: #333;
}

.tip-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.tip-box {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  max-width: 320px;
}
.tip-box p {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}
.tip-btn {
  width: 100%;
  height: 40px;
  font-size: 15px;
  color: #22ccff;
  background: transparent;
  border: none;
  cursor: pointer;
}
</style>
