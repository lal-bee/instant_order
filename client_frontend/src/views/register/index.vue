<template>
  <div class="page-register">
    <div class="viewport">
      <div class="card">
        <h2 class="title">账号注册</h2>
        <input v-model.trim="form.username" class="input" type="text" maxlength="50" placeholder="请输入用户名" />
        <input v-model="form.password" class="input" type="password" maxlength="50" placeholder="请输入密码" />
        <input v-model="form.confirmPassword" class="input" type="password" maxlength="50" placeholder="请再次输入密码" />
        <input v-model.trim="form.nickname" class="input" type="text" maxlength="50" placeholder="昵称（可选）" />
        <button class="button" type="button" :disabled="loading" @click="handleRegister">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <div class="link-row">
          <span>已有账号？</span>
          <span class="link" @click="goLogin">去登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { registerAPI, loginAPI } from '@/api/login'
import { useUserStore } from '@/stores/user'
import { showToast } from '@/utils/toast'
import { buildLoginRouteQuery, resolveRedirectPath, persistTableIdFromQuery } from '@/utils/url'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

onMounted(() => {
  persistTableIdFromQuery(route.query)
})

async function handleRegister() {
  if (!form.username) return showToast('请输入用户名')
  if (!form.password) return showToast('请输入密码')
  if (!form.confirmPassword) return showToast('请确认密码')
  if (form.password !== form.confirmPassword) return showToast('两次密码不一致')
  loading.value = true
  try {
    await registerAPI({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname,
    })
    try {
      const loginRes = await loginAPI({
        username: form.username,
        password: form.password,
      })
      const profile = loginRes.data
      if (profile && profile.token) {
        userStore.setProfile(profile)
        showToast('注册成功')
        const redirect = resolveRedirectPath(route.query.redirect, '/')
        router.replace(redirect)
        return
      }
    } catch (_) {}
    showToast('注册成功，请登录')
    const query = {
      ...buildLoginRouteQuery({
        tableId: route.query.tableId,
        redirect: route.query.redirect,
      }),
      username: form.username,
    }
    router.replace({ path: '/login', query })
  } catch (e) {
    showToast(e?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function goLogin() {
  const query = buildLoginRouteQuery({
    tableId: route.query.tableId,
    redirect: route.query.redirect,
  })
  router.replace({ path: '/login', query })
}
</script>

<style scoped>
.page-register {
  min-height: 100vh;
  background: #f6f7f9;
  max-width: 480px;
  margin: 0 auto;
}

.viewport {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
}

.card {
  width: 100%;
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
</style>
