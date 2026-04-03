<template>
  <div class="page-login">
    <div class="viewport">
      <div class="logo">
        <img src="/images/login.png" alt="扫码点餐" />
      </div>
      <div class="login">
        <button class="button" type="button" @click="handleMockLogin">模拟登录</button>
        <div class="extra">
          <div class="caption"><span>其他说明</span></div>
          <p class="desc">H5 无微信环境，使用模拟 code 登录。若后端未兼容模拟 code，请在后端配置后使用。</p>
        </div>
        <div class="tips">登录即视为同意《服务条款》和《隐私协议》</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { loginAPI } from '@/api/login'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

async function handleMockLogin() {
  const code = 'H5_MOCK'
  try {
    const res = await loginAPI(code)
    const profile = res.data
    if (profile && profile.token) {
      userStore.setProfile(profile)
      showToast('登录成功')
      const redirect = route.query.redirect
      setTimeout(() => {
        if (redirect && typeof redirect === 'string' && redirect.startsWith('/')) {
          router.replace(redirect)
        } else {
          router.replace('/my')
        }
      }, 500)
    } else {
      showToast(res.msg || '登录失败')
    }
  } catch (e) {
    showToast(e.message || '登录失败，请检查后端是否支持模拟 code')
  }
}
</script>

<style scoped>
.page-login {
  min-height: 100vh;
  background: #fff;
}

.viewport {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 20px 24px;
  box-sizing: border-box;
}

.logo {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 15vh;
}

.logo img {
  width: 120px;
  height: 120px;
  display: block;
}

.login {
  padding: 24px 0 80px;
}

.button {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  color: #fff;
  background: #10B981;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.extra {
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.caption {
  text-align: center;
  margin-bottom: 12px;
}

.caption span {
  font-size: 14px;
  color: #999;
  padding: 0 12px;
  background: #fff;
}

.desc {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
  text-align: center;
  padding: 0 16px;
}

.tips {
  position: absolute;
  bottom: 32px;
  left: 20px;
  right: 20px;
  font-size: 12px;
  color: #999;
  text-align: center;
}
</style>
