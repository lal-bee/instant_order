const SCAN_TABLE_ID_KEY = 'scan_table_id'
const SCAN_STORE_ID_KEY = 'scan_store_id'
const SCAN_TABLE_NO_KEY = 'scan_table_no'
const SCAN_STORE_NAME_KEY = 'scan_store_name'

export function getQuery() {
  const str = window.location.search || (window.location.hash && window.location.hash.indexOf('?') > -1
    ? window.location.hash.slice(window.location.hash.indexOf('?'))
    : '')
  const params = new URLSearchParams(str)
  const o = {}
  params.forEach((v, k) => { o[k] = v })
  return o
}

export function persistTableIdFromQuery(query) {
  const q = query || getQuery()
  if (q.tableId) {
    try {
      sessionStorage.setItem(SCAN_TABLE_ID_KEY, String(q.tableId))
    } catch (_) {}
  }
}

export function persistTableContext(ctx) {
  if (!ctx) return
  try {
    if (ctx.tableId !== undefined && ctx.tableId !== null) {
      sessionStorage.setItem(SCAN_TABLE_ID_KEY, String(ctx.tableId))
    }
    if (ctx.storeId !== undefined && ctx.storeId !== null) {
      sessionStorage.setItem(SCAN_STORE_ID_KEY, String(ctx.storeId))
    }
    if (ctx.tableNo !== undefined && ctx.tableNo !== null) {
      sessionStorage.setItem(SCAN_TABLE_NO_KEY, String(ctx.tableNo))
    }
    if (ctx.storeName !== undefined && ctx.storeName !== null) {
      sessionStorage.setItem(SCAN_STORE_NAME_KEY, String(ctx.storeName))
    }
  } catch (_) {}
}

function getStorageItem(key) {
  try {
    return sessionStorage.getItem(key) || ''
  } catch (_) {
    return ''
  }
}

export function getTableId() {
  const q = getQuery()
  if (q.tableId) return q.tableId
  return getStorageItem(SCAN_TABLE_ID_KEY)
}

export function getStoreId() {
  return getStorageItem(SCAN_STORE_ID_KEY)
}

export function getTableNo() {
  return getStorageItem(SCAN_TABLE_NO_KEY)
}

export function getStoreName() {
  return getStorageItem(SCAN_STORE_NAME_KEY)
}
