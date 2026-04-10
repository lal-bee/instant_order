/**
 * axios 封装：baseURL、请求头、401 跳登录、业务错误 toast、统一返回 { code, msg, data }
 */
import axios from 'axios'
import { showToast } from '@/utils/toast'
import { buildLoginRouteQuery } from '@/utils/url'

// 开发环境用空 baseURL，走 devServer 代理到后端，避免跨域；生产环境用环境变量或默认 8081
const baseURL = process.env.NODE_ENV === 'development'
  ? ''
  : (process.env.VUE_APP_BASE_URL || 'http://localhost:8081')

const request = axios.create({
  baseURL,
  timeout: 30000, // 30 秒，避免后端或网络较慢时过早超时
  headers: {
    'source-client': 'miniapp',
  },
})

// 请求拦截：追加 Authorization
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = token
  return config
})

// 响应拦截：统一返回 body；401 清 token 跳登录；业务/网络错误 toast
request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (res.status >= 200 && res.status < 300) {
      if (body && body.code !== 0) {
        showToast(body.msg || '请求失败')
        return Promise.reject(new Error(body.msg || '请求失败'))
      }
      return body
    }
    showToast((body && body.msg) || '请求失败')
    return Promise.reject(new Error((body && body.msg) || '请求失败'))
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('token')
      try {
        localStorage.removeItem('profile')
      } catch (e) {}
      showToast('请先登录')
      const current = window.location.pathname + (window.location.search || '')
      const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register')
      if (!isAuthPage) {
        const query = buildLoginRouteQuery({ redirect: current })
        const search = new URLSearchParams(query).toString()
        window.location.replace(search ? `/login?${search}` : '/login')
      }
      return Promise.reject(err)
    }
    let msg = '网络异常，请重试'
    if (err.code === 'ECONNABORTED') msg = '请求超时，请检查网络或稍后重试'
    else if (err.response && err.response.data && err.response.data.msg) msg = err.response.data.msg
    else if (err.message) msg = err.message
    showToast(msg)
    return Promise.reject(err)
  }
)

export default request
