import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 从 localStorage 恢复登录态（刷新后 Pinia 会丢失）
try {
  const profileStr = localStorage.getItem('profile')
  if (profileStr) {
    const profile = JSON.parse(profileStr)
    const userStore = useUserStore()
    userStore.setProfile(profile)
  }
} catch (e) {}

app.mount('#app')
