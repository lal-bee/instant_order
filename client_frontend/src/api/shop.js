import request from './request'

/**
 * 查询店铺营业状态（1 营业 0 打烊）
 */
export function getStatusAPI() {
  return request({
    method: 'GET',
    url: '/user/shop/status',
  })
}
