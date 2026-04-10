<template>
  <div class="page-login">
    <div class="viewport">
      <div class="login-card">
        <h2 class="title">账号登录</h2>
        <div v-if="tableTip" class="table-tip">当前桌号：{{ tableTip }}，请登录后继续点餐</div>
        <input v-model.trim="form.username" class="input" type="text" maxlength="50" placeholder="请输入用户名" />
        <input v-model="form.password" class="input" type="password" maxlength="50" placeholder="请输入密码" />
        <button class="button" type="button" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <div class="link-row">
          <span>还没有账号？</span>
          <span class="link" @click="goRegister">去注册</span>
        </div>
        <div class="tips">登录即视为同意《服务条款》和《隐私协议》</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { loginAPI } from '@/api/login'
import { showToast } from '@/utils/toast'
import {
  getTableId,
  getTableNo,
  buildRegisterRouteQuery,
  resolveRedirectPath,
  persistTableIdFromQuery,
} from '@/utils/url'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})
const tableTip = computed(() => {
  const tableNo = getTableNo()
  if (tableNo) return `${tableNo}号桌`
  const tableIdFromQuery = typeof route.query.tableId === 'string' ? route.query.tableId : ''
  const tableId = tableIdFromQuery || getTableId()
  if (!tableId) return ''
  return `${tableId}号桌`
})

onMounted(() => {
  persistTableIdFromQuery(route.query)
  const q = route.query.username
  if (typeof q === 'string') form.username = q
})

async function handleLogin() {
  if (!form.username) return showToast('请输入用户名')
  if (!form.password) return showToast('请输入密码')
  loading.value = true
  try {
    const res = await loginAPI({
      username: form.username,
      password: form.password,
    })
    const profile = res.data
    if (profile && profile.token) {
      userStore.setProfile(profile)
      showToast('登录成功')
      const redirect = resolveRedirectPath(route.query.redirect, '/')
      router.replace(redirect)
    } else {
      showToast('登录失败')
    }
  } catch (e) {
    showToast(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function goRegister() {
  const query = buildRegisterRouteQuery({
    tableId: route.query.tableId,
    redirect: route.query.redirect,
  })
  router.push({ path: '/register', query })
}
</script>

<style scoped>
.page-login {
  min-height: 100vh;
  background: #f6f7f9;
  max-width: 480px;
  margin: 0 auto;
}

.viewport {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 100vh;
  padding: calc(20px + env(safe-area-inset-top, 0px)) 16px calc(20px + env(safe-area-inset-bottom, 0px));
  box-sizing: border-box;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.title {
  margin: 0 0 14px;
  font-size: 18px;
  color: #333;
}

.table-tip {
  margin: 0 0 12px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #ecfdf5;
  border: 1px solid #10B981;
  color: #065f46;
  font-size: 13px;
}

.input {
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
}

.button {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  font-size: 16px;
  color: #fff;
  background: #10B981;
}

.button:disabled {
  background: #a7f3d0;
}

.link-row {
  margin-top: 12px;
  font-size: 13px;
  color: #666;
  text-align: center;
}

.link {
  color: #10B981;
  margin-left: 4px;
}

.tips {
  margin-top: 14px;
  font-size: 12px;
  color: #999;
  text-align: center;
}
</style>
