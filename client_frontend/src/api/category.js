import request from './request'

/**
 * 分类列表（含 type：1 菜品 2 套餐；sort 用于判断）
 */
export function getCategoryAPI(params) {
  return request({
    method: 'GET',
    url: '/user/category/list',
    params,
  })
}
