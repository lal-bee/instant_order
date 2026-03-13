/**
 * 简单 Toast（替代 uni.showToast），无依赖
 */
let timer = null

export function showToast(options) {
  const msg = typeof options === 'string' ? options : (options && options.title) || ''
  const duration = (options && options.duration) || 2000
  let el = document.getElementById('app-toast')
  if (!el) {
    el = document.createElement('div')
    el.id = 'app-toast'
    el.style.cssText = 'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);padding:12px 20px;background:rgba(0,0,0,0.7);color:#fff;border-radius:8px;font-size:14px;z-index:9999;max-width:80%;pointer-events:none;'
    document.body.appendChild(el)
  }
  el.textContent = msg
  el.style.display = 'block'
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    el.style.display = 'none'
    timer = null
  }, duration)
}
