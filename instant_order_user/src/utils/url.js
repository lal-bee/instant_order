/**
 * 扫码点餐：门店编号、餐桌编号
 * - 从当前 URL 解析：?storeId=1&tableId=A12（支持 hash 路由后 ?storeId=1&tableId=A12）
 * - 首次带参进入后持久化到 sessionStorage，登录跳转、页面跳转后仍可从 storage 读取
 */
const SCAN_STORE_KEY = 'scan_store_id'
const SCAN_TABLE_KEY = 'scan_table_id'

export function getQuery() {
  const str = window.location.search || (window.location.hash && window.location.hash.indexOf('?') > -1 ? window.location.hash.slice(window.location.hash.indexOf('?')) : '')
  const params = new URLSearchParams(str)
  const o = {}
  params.forEach((v, k) => { o[k] = v })
  return o
}

/** 当 URL 或传入 query 中有门店/桌号时，持久化到 sessionStorage，供后续页面（如提交订单）使用 */
export function persistScanParams(query) {
  const q = query || getQuery()
  if (q.storeId) try { sessionStorage.setItem(SCAN_STORE_KEY, String(q.storeId)) } catch (_) {}
  if (q.tableId) try { sessionStorage.setItem(SCAN_TABLE_KEY, String(q.tableId)) } catch (_) {}
}

/** 优先从当前 URL 取，没有则从 sessionStorage 取（扫码后登录再进提交页时用） */
export function getStoreId() {
  const q = getQuery()
  if (q.storeId) return q.storeId
  try {
    return sessionStorage.getItem(SCAN_STORE_KEY) || ''
  } catch (_) {
    return ''
  }
}

/** 优先从当前 URL 取，没有则从 sessionStorage 取 */
export function getTableId() {
  const q = getQuery()
  if (q.tableId) return q.tableId
  try {
    return sessionStorage.getItem(SCAN_TABLE_KEY) || ''
  } catch (_) {
    return ''
  }
}
