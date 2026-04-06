<template>
  <div class="page-update-my">
    <header class="detail-header">
      <img class="back" src="/icon/back.png" alt="返回" @click="goBack" />
      <span class="title">信息设置</span>
    </header>

    <form class="form" @submit.prevent="submit">
      <div class="pic-box" @click="picChange">
        <img :src="user.pic || '/images/login.png'" class="avatar" alt="头像" />
        <span class="pic-tip">点击上传头像</span>
      </div>
      <div class="form-item radio-item">
        <span class="label">性别</span>
        <label class="radio">
          <input v-model="user.gender" type="radio" :value="1" />
          <span>男士</span>
        </label>
        <label class="radio">
          <input v-model="user.gender" type="radio" :value="0" />
          <span>女士</span>
        </label>
      </div>
      <div class="form-item">
        <label class="label">昵称</label>
        <input v-model="user.name" class="input" placeholder="请输入昵称" />
      </div>
      <div class="form-item">
        <label class="label">手机号</label>
        <input v-model="user.phone" class="input" type="tel" placeholder="请输入手机号" maxlength="11" />
      </div>
      <button type="submit" class="submit-btn">确认修改</button>
    </form>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      class="hidden-input"
      @change="onFileChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfoAPI, updateUserAPI } from '@/api/user'
import { showToast } from '@/utils/toast'

const router = useRouter()
const userStore = useUserStore()
const fileInputRef = ref(null)

const user = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: '',
  pic: '',
})

async function loadUser() {
  const id = userStore.profile?.id
  if (!id) return
  user.id = id
  try {
    const res = await getUserInfoAPI(id)
    user.name = res.data?.name ?? ''
    user.gender = res.data?.gender ?? 1
    user.phone = res.data?.phone ?? ''
    user.pic = res.data?.pic ?? ''
  } catch (e) {}
}

function picChange() {
  fileInputRef.value?.click()
}

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file || !file.type.startsWith('image/')) return
  const reader = new FileReader()
  reader.onload = () => {
    user.pic = reader.result
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

function validate() {
  if (!user.name?.trim()) {
    showToast('昵称不能为空')
    return false
  }
  if (!user.phone?.trim()) {
    showToast('手机号不能为空')
    return false
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(user.phone)) {
    showToast('手机号格式不正确')
    return false
  }
  return true
}

async function submit() {
  if (!validate()) return
  try {
    await updateUserAPI(user)
    showToast('修改成功')
    router.replace('/my')
  } catch (e) {}
}

function goBack() {
  router.replace('/my')
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.page-update-my {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  background: #fff;
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

.form {
  padding: 24px 16px;
}
.pic-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}
.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  background: #f0f0f0;
}
.pic-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  cursor: pointer;
}
.hidden-input {
  position: absolute;
  left: -9999px;
  opacity: 0;
  pointer-events: none;
}
.form-item {
  display: flex;
  align-items: center;
  min-height: 48px;
  border-bottom: 1px solid #efefef;
  padding: 10px 0;
}
.form-item.radio-item {
  flex-wrap: wrap;
}
.label {
  width: 80px;
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
.radio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  margin-right: 16px;
}
.submit-btn {
  width: 100%;
  max-width: 280px;
  margin: 32px auto 0;
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
