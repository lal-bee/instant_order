import request from './request'

/**
 * 保存扫码参数到后端 Redis（正式发布后，下单时从 Redis 取 storeId、tableId）
 */
export function saveScanParamsAPI(storeId, tableId) {
  return request({
    method: 'POST',
    url: '/user/scan-params',
    params: { storeId, tableId },
  })
}

/**
 * 根据 id 查询用户信息
 */
export function getUserInfoAPI(id) {
  return request({
    method: 'GET',
    url: `/user/user/${id}`,
  })
}

/**
 * 更新用户信息
 */
export function updateUserAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/user',
    data,
  })
}
